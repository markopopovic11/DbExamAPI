# Per-student isolation is a database per environment, owned by the engine

The unit that separates one student from another is a PostgreSQL **database**,
not a schema — confirming the current design after weighing schema-per-student
and hybrid alternatives against the researched facts (#22, #23, #24). The
Postgres docs themselves prescribe separate databases for users who should be
"unaware of each other"; in the schema model a student can read the *shape* of
another student's answer (table and column names) from the catalogs without any
grant, and the docs concede that no secure schema usage pattern exists once an
untrusted user owns the database. The two classic counterarguments both failed:
connection pooling cannot multiplex per-student roles in either model (PgBouncer
pools per user/database pair), and provisioning benchmarked *faster* for
databases — ~70 ms per `CREATE DATABASE … TEMPLATE`, a 300-student cohort in
~22 s, with schema-based provisioning/reset 7–15× slower.

Within one student, one Managed Database spans one **per-student environment**:
one database per (participant × environment). The span is an authoring
decision, not a system rule — tasks sharing an environment continue on each
other's state in one database, and an environment referenced by a single task
is legal and behaves as per-task isolation. Per-exam consolidation was rejected
(swap would turn all-or-nothing and templates would need per-exam merging), as
was forced per-task isolation (it removes task continuity entirely).

The **engine role owns** every per-student database. The student role receives
`CREATE` and `USAGE` on its working schema and owns only the objects it
creates — full task DDL/DML and procedures work, while database ownership
stays out of reach. This closes off `ALTER DATABASE` self-service (e.g.
raising the database's own `CONNECTION LIMIT`), trusted-extension installation
(install scripts run as the bootstrap superuser), and the verified CVE class
whose precondition is database ownership. Extensions a template legitimately
needs are installed at template-build time by the engine.

Physical lifecycle is **admin-configurable**: either batch-drop a sitting's
databases after its exam window closes and pulled submissions are captured, or
keep them until manually purged. Drops are always batched between sittings
(each `DROP DATABASE` forces checkpoints). In both modes the Managed Database
record persists forever as history and submissions remain the source of truth,
so steady-state disk is bounded by the live sitting plus pools, never by
accumulated history.

## Consequences

- Provisioning must run `REVOKE CONNECT, TEMPORARY ON DATABASE … FROM PUBLIC`
  on every created database — `CREATE DATABASE` does not copy database-level
  grants from the template, so this cannot live in the template.
- Per-database `CONNECTION LIMIT` becomes a usable containment lever, since
  students cannot own-and-undo it.
- Students cannot install extensions, trusted or otherwise; template build is
  the only extension installation point.
- The retention choice (drop-after-sitting vs keep-until-purged) is one admin
  setting; whether it can be overridden per exam is left to implementation.
- No disk/CPU quota exists in core Postgres at any per-student scope; runaway
  writes remain a cluster-level risk in either isolation model — addressed
  separately by the resource-abuse containment decision (#28).
- Autovacuum cycles its workers across all live databases; database count per
  sitting is the operative variable, another reason lifecycle keeps old
  sittings' databases from lingering.
