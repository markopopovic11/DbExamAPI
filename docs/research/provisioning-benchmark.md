# Provisioning throughput benchmark

Resolves wayfinder ticket [#24](https://github.com/MarkoPopovikj/DbExamAPI/issues/24).
Harness: `bench/` on this branch — one script (`run.sh`) against a stock
`postgres:17` Docker container; each phase re-runnable independently.

## Test rig — read this first

| | |
|---|---|
| Host | **MacBook Air (Apple silicon, 16 GB) — not the target VM** |
| Container | Docker Desktop Linux VM, capped `--cpus=4 --memory=8g` |
| PostgreSQL | 17.10, `max_connections=800`, `shared_buffers=1536MB`, `max_wal_size=4GB`, otherwise defaults |
| Template | 21 MB exam environment: 10 tables, FKs, 8 indexes, 3 plpgsql functions, 2 triggers, ~129 k rows |

**Caveat:** the CPU/RAM cap approximates a modest VM, but the underlying Apple
SSD does not — clone cost is dominated by copying the template, so absolute
latencies here are a *lower bound*; assume a few multiples worse on typical
on-prem VM storage. What transfers: the scaling *shapes* (degradation curves,
WAL_LOG vs FILE_COPY behavior, storage ratios) and the database-vs-schema
comparison, which ran on identical hardware. **Re-run `run.sh` on the real VM
before locking any decision whose margin looks thin** — the harness is the
reusable half of this deliverable.

## Headline answer

**Pool-swap is not *needed* for provisioning throughput. On-demand
`CREATE DATABASE … TEMPLATE` suffices at hundreds of concurrent students by a
wide margin** (~70 ms per student on this rig; a full 300-student cohort in
~22 s serial; refill under full exam-room load is, if anything, *faster*).
Whether a warm pool is still wanted for other reasons (instant-swap UX,
absorbing a VM's slower disk, pre-exam warm-up) is #26's decision — but the
throughput numbers alone don't force it.

## Measurements

All timings are per-statement `\timing` inside a single in-container psql
session (no docker-exec or connection overhead in the numbers).

### 1. `CREATE DATABASE … TEMPLATE` latency vs cluster size

Provisioning unit = the real triple from #23: `CREATE ROLE` +
`CREATE DATABASE … OWNER … STRATEGY WAL_LOG` + `REVOKE CONNECT FROM PUBLIC` +
`GRANT CONNECT`. n=300, one after another, cluster growing 15 → 315 databases.

| metric | value |
|---|---|
| mean / p50 | 72 ms / 61 ms |
| p95 / p99 / max | 161 ms / 265 ms / 399 ms |
| mean, clones 1–50 (empty cluster) | 37 ms |
| mean, clones 251–300 (~300 DBs) | 100 ms |
| full 300-student cohort, serial | ≈ 22 s |

Degradation with cluster size exists (~2.7× from empty to 300 DBs) but is
mild and linear-ish — nothing resembling a cliff. `CREATE DATABASE`
dominates; role/revoke/grant are 1–3 ms each.

`STRATEGY FILE_COPY` comparison: ~30 ms on a quiet cluster (small template ⇒
cheap checkpoint), but **8.6 s** for the first clone after heavy WAL activity —
its two forced checkpoints flush whatever dirty data has accumulated,
so latency is unpredictable exactly when the server is busy. `WAL_LOG`
(the PG15+ default) is the right strategy at this template size; revisit only
for templates in the hundreds of MB.

### 2. Pool refill rate under live load

Load: 300 established student connections (6 × pgbench, 50 clients each, on 6
clone DBs) running an indexed lookup + aggregate join per ~3 s think-time loop
(~200 queries/s total — the mostly-idle exam-room profile from #23).

| metric | value |
|---|---|
| serial refill, mean per triple | **33 ms** (25 clones in 0.9 s) |
| parallel refill, 4 workers, per-op mean | 103 ms |
| parallel refill throughput | ≈ **38 clones/s** |
| student query latency during refill | unchanged (~5 ms; loop = 3009 ms vs 3000 ms scripted think time) |
| `DROP DATABASE … WITH (FORCE)` under load | ~9 ms (n=20) |

Refill under load was *faster* than the growth series (warm caches). Cloning
is invisible to connected students at this load profile. A depleted pool of
any plausible size refills in seconds.

### 3. Storage overhead per clone

| metric | value |
|---|---|
| template size | 21 MB |
| avg clone size (n=300) | 22.1 MB — 1:1 with template |
| catalog floor (empty database) | **7.5 MB** |
| 315 databases total | 6.6 GB data + WAL (capped 4 GB) |
| shared catalogs at 300+ DBs/roles | trivial (`pg_database` 172 kB, `pg_authid` 156 kB) |

Storage is `N × (template + ~7.5 MB catalog floor)`. No copy-on-write, no
surprises. At 21 MB templates, 300 students ≈ 6.6 GB — plan disk as
`cohort × template size` plus WAL headroom.

### 4. Schema-per-student comparison (same data, same hardware)

300 schemas in one database, each: `CREATE ROLE` + `CREATE SCHEMA` + same DDL
(script replay) + `INSERT … SELECT` of the same ~129 k rows + triggers.

| metric | database-per-student | schema-per-student |
|---|---|---|
| provision one student | **33–72 ms** | 514 ms (flat, 501→528 ms) |
| reset one student | drop 9 ms + re-clone ≈ **40–80 ms** | 614 ms (`DROP SCHEMA CASCADE` + rebuild) |
| storage for 300 | 6.6 GB | 4.4 GB (saves the 7.5 MB/DB catalog floor) |
| catalog weight | 467 `pg_class` rows × 300 DBs | 16,067 `pg_class` rows in one DB |

The schema model is ~7–15× slower to provision and ~10× slower to reset —
object creation and row copying can't compete with a file-level clone. Its
only measured win is the catalog-floor storage saving (~33 % at this template
size; irrelevant in absolute terms). Combined with #23's isolation findings,
the performance argument for schemas evaporates.

### 5. Where it falls over

Connection ramp — mostly-idle sessions (`SELECT 1` + 2 s think time), single
pgbench, `db_s_001`:

| established conns | initial connect time | query latency (minus think time) | container memory |
|---|---|---|---|
| 100 | 48 ms | ~3.6 ms | 2.06 GiB |
| 300 | 156 ms | ~4.9 ms | 2.34 GiB |
| 500 | 165 ms | ~4.7 ms | 2.62 GiB |
| 700 | 334 ms | ~11.8 ms | 2.90 GiB |

No errors or refusals up to 700; incremental cost ≈ **1.4 MiB per idle
connection** — consistent with #23's post-PG14 mostly-idle scaling facts. The
documented 300–500 *active*-connection wall was not probed (exam sessions are
mostly idle). Within an 8 GB envelope, connection count is not the binding
constraint; `max_connections` sizing + reserved slots (#29) is.

| probe | result |
|---|---|
| provisioning triple during 500 established conns | 25–112 ms — unchanged |
| first `DROP DATABASE` at 500 conns | **1.8 s** (drop forces a checkpoint; subsequent drops ~11 ms) — batch-retire pools between sittings, not mid-exam |
| autovacuum workers at ~350 DBs | 0 observed across samples — no measurable pressure (naptime division across N DBs remains a doc-sourced watch item, #23) |
| catalog bloat after 300 create/drop cycles | none measurable (shared catalogs < 200 kB) |

## Simplifications / limits of this run

- pgbench connected as `postgres`, not 300 distinct student roles (per-role
  auth adds per-connection cost not measured here; #23's PgBouncer finding
  makes per-role pooling moot anyway).
- Timings exclude client connection setup — they measure server-side
  provisioning cost only.
- Sequence `setval` after schema data copy omitted (would add a few ms/schema).
- Template is 21 MB; both clone and schema-copy costs scale roughly with
  template size — re-measure if real exam templates are 10× larger.
- Single run per scenario; treat numbers as order-of-magnitude, not ±5 %.
