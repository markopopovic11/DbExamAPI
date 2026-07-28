# Reset is claim-with-fallback behind a stable connection descriptor

How "start from scratch" works at hundreds of concurrent students (#26),
building on the isolation unit (ADR 0002) and the provisioning benchmark
(#24). Three ideas compose: a single reset path with the pool as an optional
buffer, eager provisioning before the exam window, and a swap that never
changes what the student typed into their SQL client.

## One reset path: claim from the pool, clone on the spot if it is empty

Every reset runs the same sequence: claim a ready database from the
environment's pool; if the pool is empty, clone one synchronously from the
template (`CREATE DATABASE … TEMPLATE` with `STRATEGY WAL_LOG`). The pool's
**target size is a per-environment, admin-set integer, and 0 is legal** —
pure on-demand is simply a pool of size zero. Replenishment is one background
loop topping the pool back to target.

This collapses the pool-swap vs on-demand dichotomy instead of picking a
side. The benchmark showed on-demand suffices for realistic templates
(~70 ms/clone at 21 MB, ~129k rows), so small exams run with target 0–2 and
no meaningful idle inventory. But clone cost is I/O-dominated and scales
with template size — a future multi-hundred-MB template on the slower target
VM could push a synchronous clone into tens of seconds, and that is exactly
when an admin raises the target so resets stay instant while replenishment
trickles in the background (measured as invisible under full live load).
Pool starvation is never an error, just a slower reset. Rejected: automatic
pool sizing derived from template size or measured clone times (speculative
machinery for a knob an admin can set), and snapshot/filesystem-level reset
mechanisms (solve the same latency problem with operational exotica outside
Postgres).

## Eager, admin-triggered sitting prep

Participants' first databases are not claimed from the pool. An explicit
admin action before the exam window opens creates and assigns one database
per (participant × per-student environment) from the enrollment listing —
~22 s for a 300-cohort, entirely off the critical path — so "all databases
provisioned and connectable" is verifiable before any student arrives.
Claim-at-start was rejected: it would demand a pool pre-sized to cohort
scale at T=0, quietly reinventing the always-on pool. Late enrollments and
walk-ins are just "add participant, then the standard claim-with-fallback
path" — there is no separate bonus-database concept, because an unassigned
ready database is what the pool already holds. Credentials are delivered
in-app only: a logged-in participant sees their connection descriptor in the
exam UI. No out-of-band lists (they leak and go stale). Authentication
itself (FINKI CAS) is outside this map.

## The swap is a rename dance; the student changes nothing

The connection descriptor — host, port, database name, role, password — is
fixed per (participant × environment) for the whole exam. A swap rotates
physical databases behind that stable name using catalog-only renames:

1. revoke `CONNECT` from the student role, then terminate its backends
   (revoke first, so a racing reconnect cannot slip in);
2. **pull-on-discard**: pull every pullable artifact from the ruined
   database and record it as swap-captured pulled submissions;
3. `ALTER DATABASE <name> RENAME TO discard_<id>`;
4. rename the fresh database (pooled or fallback-cloned) to `<name>`;
5. re-grant `CONNECT`.

The student's only experience is a dropped session and a reconnect — same
credentials, same database name. Drop-and-recreate under the same name was
rejected because every `DROP DATABASE` forces a checkpoint (1.8 s cluster
spike in the benchmark); with the rename dance, mid-exam resets involve
**zero** drops — discarded databases wait under `discard_` names for the
between-sittings batch drop ADR 0002 already mandates.

Pull-on-discard closes the external-tool gap: typed submissions are captured
on send, but DataGrip work exists only inside the database until pulled, and
would otherwise die with the swap. An empty or failed capture is recorded
too — it is the receipt that the database contained nothing pullable at
reset time, which is what graders need in empty-answer disputes.
Submissions and Student Task state are anchored to the participant, not the
database, so they carry across swaps untouched.

## Guard rails

No hard cap on how many times a participant may swap (it burns their own
exam time); one in-flight swap per participant plus a short cooldown
protects the server from a swap-storm. A failed fallback clone surfaces to
the admin/proctor as an incident rather than retrying silently forever.

## Consequences

- The pool is now purely a reset buffer; sizing it is one admin decision
  per environment, made for big templates and expected walk-ins, not a
  system requirement.
- A Managed Database's physical name changes once in its life (at discard);
  the stable student-facing name belongs to the (participant × environment)
  slot, not to any one physical database. The record itself remains
  never-reused, never-forgotten history.
- The swap sequence is the *only* mid-exam provisioning write path that
  touches a live student; everything else (prep, replenishment) runs against
  unassigned databases.
- Provisioning must still apply ADR 0002's `REVOKE`/`GRANT CONNECT` triple
  to every clone, pooled or fallback.
- Benchmark numbers came from a laptop SSD; re-running the harness on the
  target VM (#30) validates the fallback-latency envelope that makes
  target-0 pools safe for small templates.
- When else the engine pulls (deadlines, cadence, post-pull mutation) is
  deliberately not decided here — only the swap edge is fixed.
