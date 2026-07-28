# Resource abuse is contained by survivability and eviction, not impossibility

How DbExam contains a student who fills the disk or pins the CPU on the shared
server — deliberately or via a runaway `INSERT … SELECT` — given the researched
facts (#23): every timeout and memory GUC is `USERSET` (student-defeatable),
`temp_file_limit` is the only non-defeatable brake, and core Postgres has no
disk, CPU, or I/O quota at any scope below the whole cluster.

The chosen posture: **the sitting must survive any single student**. Abuse is
detected, attributed, and evicted — not made technically impossible. Every
student is authenticated and proctored with overwhelming incentive not to
sabotage their own exam; the realistic threat is the accidental runaway, and
no mechanism at any price closes the CPU hole anyway. Rejected as
disproportionate under this posture: per-student tablespaces on separate
filesystems (heavy ops, doesn't bound WAL), the third-party `diskquota`
extension (non-core code in the critical path), and an OS quota on the
`postgres` user (cluster-wide, so hitting it is a self-inflicted outage).

The containment stack, all levers engine-owned and student-unreachable under
the ADR 0002 ownership model:

- **Disk**: per-database `temp_file_limit` set at provisioning via
  `ALTER DATABASE … SET` (SUSET — students cannot raise it; hard-cancels the
  recursive-CTE / giant-sort class), plus the **watchdog** polling
  `pg_database_size()` per student database against thresholds — covering the
  explicit-temp-table and permanent-table growth that `temp_file_limit`
  exempts. Database-per-student makes every byte attributable.
- **Blast radius**: WAL (`pg_wal`) on a dedicated volume, so a full data disk
  degrades to write errors instead of the documented cluster panic; free-space
  alerting on both volumes feeds the same channel as the watchdog.
- **Queries, two layers**: defeatable per-database defaults
  (`statement_timeout`, `idle_in_transaction_session_timeout`, `lock_timeout`,
  modest `work_mem`) catch accidents, which never touch their GUCs; the
  watchdog enforces a generous admin-set hard runtime ceiling from *outside*
  the session — `pg_cancel_backend()`, escalating to
  `pg_terminate_backend()` — which no `SET` can escape. The engine role is a
  member of `pg_signal_backend`.
- **Connections**: per-database `CONNECTION LIMIT` at provisioning (bounds
  backend memory, advisory locks, LISTEN queues, and lock-table pressure in
  one cap), per-role `CONNECTION LIMIT` as approximate belt-and-braces, and
  reserved slots (`reserved_connections` + `pg_use_reserved_connections`) so
  the watchdog can always connect during the saturation it exists for.
- **Escalation**: the watchdog acts automatically only up to session
  termination, recording every action as an **incident** with the student's
  identity and evidence. **Eviction** — terminate all sessions plus
  `ALTER ROLE … NOLOGIN` — is human-only, one operator click: a false-positive
  auto-eviction mid-exam is worse than minutes of degradation, and operators
  are present during sittings.
- **Patch currency**: minor releases are applied within two weeks of upstream
  publication — next maintenance window if a CVE is reachable from the student
  privilege baseline (the recurring "create non-temporary objects" class:
  CVE-2020-25695, 2022-1552, 2022-2625, 2023-2454, 2026-6472). A pre-sitting
  currency gate enforces it: no sitting starts on a cluster missing a
  security-relevant minor release except by explicit operator override.
  Patching is a load-bearing control here, not hygiene — the permission model
  cannot close this CVE class.

## Consequences

- **PostgreSQL 16 is the minimum major version**: non-superuser reserved
  connection slots (`reserved_connections`) only exist from PG16, and the
  engine role must not be superuser.
- The watchdog is a first-class engine component with its own reserved
  connection budget, not an optional ops add-on; its ceilings
  (runtime, database size, thresholds) are admin configuration, not code.
- Restarts for minor upgrades are scheduled between sittings, alongside the
  batched drops from ADR 0002.
- Concrete threshold values (timeouts, size limits, connection caps) are
  deployment configuration and deliberately not fixed by this ADR.
