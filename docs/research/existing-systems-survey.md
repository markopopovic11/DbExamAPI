# Existing-systems survey + adopt kill-check

Research for [#22](https://github.com/MarkoPopovikj/DbExamAPI/issues/22), part of the
wayfinder map [#21](https://github.com/MarkoPopovikj/DbExamAPI/issues/21).

**Question.** What do existing systems in this space do that DbExam should steal
for its provisioning/grading core — and, answered once, does any of them cover
the core requirement (per-student *mutable* PostgreSQL databases, procedures and
triggers, external-tool-native workflow with pulled submissions, instructor-defined
checks with partial credit) well enough that building DbExam is a mistake?

Sources are cited inline. Claims that could not be traced to a primary source are
marked **[unverified]** rather than asserted.

---

## Part 1 — Educational and commercial SQL assessment systems

### 1.1 The dominant model: type SQL into a box, execute server-side, diff output

Almost every documented system in this space shares one shape. The student types
SQL into a web textarea; the platform executes it server-side against a shared
instructional dataset or a throwaway sandbox; the platform diffs the output
against a reference. The student never holds credentials, and nothing they do
persists.

**Moodle CodeRunner**'s built-in SQL question type is, mechanically, a Python3
question that pipes the answer text to `sqlite3` and compares stdout
([CodeRunner question type](https://docs.moodle.org/31/en/CodeRunner_question_type),
[SQL and CodeRunner forum thread](https://coderunner.org.nz/mod/forum/discuss.php?d=14)).
Execution is delegated to **Jobe**, a standalone job-execution server that runs
each job under `runguard` (borrowed from DOMjudge) as a low-privileged user with
CPU/memory/process/output limits and then wipes the working directory — Jobe's own
documentation describes it as unsuitable for database servers or other stateful
applications ([trampgeek/jobe](https://github.com/trampgeek/jobe)). SQLite,
ephemeral, textarea-only.

The same shape recurs across the academic line: **SQLator** (SIGCSE Bulletin 2004)
evaluates submitted query text with "complex heuristic algorithms" against
instructor-loaded schemas
([ACM](https://dl.acm.org/doi/10.1145/1026487.1008055)); **AsseSQL** (ITiCSE 2014)
compares student output against an expert reference query
([ACM](https://dl.acm.org/doi/10.1145/2591708.2602682)); **aSQLg** (Koli Calling
2013) executes submitted SQL and scores "different aspects" of it, plugged into an
LMS ([ACM](https://dl.acm.org/doi/10.1145/2526968.2526986)). A SIGCSE 2015 abstract
in that lineage claims extension to DML and DDL statements
([ACM](https://dl.acm.org/doi/10.1145/2676723.2691896)) — **[unverified]**, the
body text was not accessible, so its actual state-grading mechanism is unknown.

Practice sites are weaker still for our purposes. **pgexercises** forwards browser
queries through a Java `SQLForwarder` to one shared PostgreSQL instance and does
not grade at all — the student eyeballs their output against a displayed model
answer ([pgexercises](https://pgexercises.com/),
[SQLForwarder](https://github.com/AlisdairO/pgexercises/tree/master/SQLForwarder)).
**SQLZoo** runs queries against a shared MariaDB dataset; no primary-source
description of its checking algorithm was found
([SQLZoo wiki](https://sqlzoo.net/wiki/Sql)) — **[unverified]**. For **LeetCode**
and **HackerRank** SQL, no official architecture documentation was found at all;
third-party system-design explainers describe queue + ephemeral worker + verdict,
but that is **[unverified]** inference and is not cited here as fact.

**Gradescope** is the generic case: a fresh isolated Docker container per
submission, destroyed after grading, with "no connection between submissions"
([Gradescope autograder docs](https://gradescope-autograders.readthedocs.io/en/latest/tech/)).
SQL support is whatever an instructor scripts inside that container; submissions
are uploaded files, not live connections.

### 1.2 The exceptions: systems that hand students real credentials

Two systems break the textarea pattern, and they are the ones that matter for the
kill-check.

**pg4e (PostgreSQL for Everybody, csev/Tsugi)** is the closest published analogue
to DbExam's external-tool-native workflow. The course states plainly: "When you
take this course we will provide you with a small PostgreSQL server with limited
capabilities", and points students at `psql` as the "least common denominator"
client ([pg4e.com](https://www.pg4e.com/)). Its setup autograder source shows the
provisioning shape we independently arrived at — a superuser admin connection
(`$admin_user = 'postgres'`) creating a per-student role and database with
`CREATE ROLE $user NOSUPERUSER NOCREATEDB NOCREATEROLE`, `GRANT connect ON
DATABASE $db TO $user`, and `REVOKE all ON DATABASE $db FROM public`, then
displaying host/port/database/user/password and a ready-made `psql` command line
([tools/sql/00setup.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/00setup.php)).
Crucially, its assignment graders then **connect back into the student's own
database** using the student's stored connection —
`pg4e_get_user_connection($LAUNCH, $pdo_connection, ...)` — and inspect it,
requiring a `pg4e_debug` table to exist before grading proceeds
(`pg4e_check_debug_table`)
([tools/sql/00first.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/00first.php)).

Where pg4e stops: grading is all-or-nothing. Both graders read end with a flat
`$gradetosend = 1.0;` and bail out early on any mismatch — there is no per-check
partial credit, no check taxonomy, no points arithmetic
([00first.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/00first.php),
[03regexp.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/03regexp.php)).
Some assignments do not use the student's database at all but inject the student's
answer into a query over a central read-only dataset and compare rows positionally
(`SELECT purpose FROM taxdata WHERE purpose ~ '$query' ORDER BY purpose DESC LIMIT
3`, then `if ( $solution[$i] != $student[$i] )`)
([03regexp.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/03regexp.php)).
And there is no pool, no swap, no exam timing, no invigilated sitting, no
procedure/trigger grading. It is a self-paced MOOC's homework grader built on
Tsugi/LTI in PHP.

**Vocareum** is the strongest commercial candidate: "MySQL, PostgreSQL, and
MongoDB environments", where "[e]ach learner receives an isolated database
environment, so their work remains separate from others", supporting "migrations,
schema changes, tuning, and other database administration tasks", with assignments
"assessed automatically by running validation scripts against the learner's
environment ... checking query output, schema updates, expected values"
([Vocareum database labs](https://www.vocareum.com/databases/)). That is a real
mutable per-learner database plus instructor-authored validation scripts. But
learners connect via "web-based administration tools like Adminer or pgAdmin, or
access the databases programmatically from Vocareum IDEs and Notebooks" — no
documented external-client (DataGrip/JDBC-from-the-student's-laptop) path — and the
page documents no self-hosting option. **[unverified]** whether external client
access or on-prem deployment can be bought; both would need a sales conversation.

**CodeRunner + client-server DB extension (SIGCSE 2025, Wojtowicz et al.)** is the
closest research artifact: an open-source CodeRunner extension that gives each
exercise "temporary, isolated Docker containers, providing each student with a
safe, fully privileged environment" running MySQL, PostgreSQL or MS SQL Server,
with grading that checks output correctness *and* validates the submitted
statement against the parse tree of the dialect's grammar
([ACM](https://dl.acm.org/doi/10.1145/3641554.3701830),
[archived repo](https://github.com/andre-wojtowicz/coderunner-sql-sigcse2025)).
Whether students can point an external client at those containers, or whether the
container is only an internal execution target behind the Moodle textarea, could
**not** be confirmed — both the ACM and ResearchGate pages returned 403.
**[unverified]**, and the single most important open question in this survey.

### 1.3 Grading and partial-credit mechanisms worth knowing

**CodeRunner** exposes partial credit as an explicit author-facing switch: per-test-case
`Mark` values summed, or an "All-or-nothing" mode where any failed test scores
zero, with the `Mark` field documented as meaningful only when all-or-nothing is
off ([lang strings](https://github.com/trampgeek/moodle-qtype_coderunner/blob/master/lang/en/qtype_coderunner.php)).
When per-test marks cannot express real partial correctness, the maintainer's own
advice is to split into more test cases or "switch to using a template grader and
write your own code to grade the submission" with custom fractional marks — and the
same thread flags the denominator footgun: adding a 50%-weighted test among three
tests yields 50/150, not 50/100
([partial grading thread](https://coderunner.org.nz/mod/forum/discuss.php?d=576)).

**XData / XDa-TA** (IIT Bombay) attacks a different problem: instead of one
hand-written dataset per task, it *generates* test data specifically designed to
kill likely-wrong query mutants, so that a passing result actually discriminates
correct queries from near-misses. Instructors assign "weightage given to partial
marking parameters" per assignment
([XData project](http://www.cse.iitb.ac.in/infolab/xdata/),
[student docs](http://www.cse.iitb.ac.in/infolab/xdata/Docs/Readme_Student.html),
[VLDBJ/arXiv](https://arxiv.org/pdf/1411.6704)).

**Chandra, Joseph & Sudarshan (PVLDB 9(13), 2016)** go further: partial marks for a
*wrong* query, scaled by the cost of the minimal transformation that would make it
equivalent to a correct one, using equivalence reasoning rather than test-case
counting ([paper](http://www.vldb.org/pvldb/vol9/p1541-chandra.pdf)) — summarized,
not quoted; read directly before relying on specifics. **Cosette** is the adjacent
formal tool: it proves two SQL queries equivalent or returns a counterexample
database instance ([CIDR 2017](https://www.cidrdb.org/cidr2017/papers/p51-chu-cidr17.pdf)).

### 1.4 What nobody documented

Three absences, stated as absence-of-evidence rather than proof:

- **Pull-based submission capture.** No surveyed system was found that extracts a
  submission by introspecting a database the student freely mutated with their own
  tools (e.g. reading `pg_proc`/`information_schema.routines` for procedure
  source). Every documented system treats "the submission" as the text entered in
  its own UI. DbExam's `SubmissionSource.PULLED` appears to be a genuine
  difference, not something to copy.
- **Grading procedures and triggers by side effect.** No primary source was found
  describing a system that invokes a student's stored procedure or fires a trigger
  and then checks the resulting state. This is DbExam's `STATE_MATCH` territory,
  and it is unoccupied in the documented literature.
- **Hundreds of concurrent *mutable* databases.** The only systems publishing
  concurrency architecture (Jobe, Gradescope) are stateless execution tiers. None
  is a proof point for the stateful side.

---

## Part 2 — Database sandbox / provisioning technology

### 2.1 Postgres-native cloning

**`CREATE DATABASE ... TEMPLATE`** clones an existing database at the
catalog+storage level. The binding constraint: "no other sessions can be
connected to the template database while it is being copied. `CREATE DATABASE`
will fail if any other connection exists when it starts; otherwise, new
connections to the template database are locked out until `CREATE DATABASE`
completes"
([CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html)).
For a pool design this means the physical template database must be permanently
quiesced — never handed to a human, never probed by monitoring — or pool refills
will fail intermittently rather than queue.

**`STRATEGY`** (new in PG15) picks how the copy happens. `WAL_LOG` (the new
default) copies "block by block and each block ... separately written to the
write-ahead log ... most efficient ... where the template database is small".
`FILE_COPY` "writes a small record to the write-ahead log for each tablespace ...
While this does reduce the write-ahead log volume substantially, especially if
the template database is large, it also forces the system to perform a checkpoint
both before and after the creation of the new database. In some situations, this
may have a noticeable negative impact on overall system performance"
([CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html);
introduced in [PG15 release notes](https://www.postgresql.org/docs/15/release-15.html)).
Which strategy wins for DbExam depends on the real template size and refill
burstiness — that is a benchmark question, not a documented answer.

**`DROP DATABASE ... WITH (FORCE)`** (PG13) "attempt[s] to terminate all existing
connections to the target database. It doesn't terminate if prepared
transactions, active logical replication slots or subscriptions are present"
([DROP DATABASE](https://www.postgresql.org/docs/current/sql-dropdatabase.html);
[PG13 release notes](https://www.postgresql.org/docs/13/release-13.html)). This is
exactly the primitive a *swap* needs: reclaim a ruined database out from under a
student who still has DataGrip connected, without hand-rolling
`pg_terminate_backend` loops.

**`pg_dump`/`pg_restore`** is the wrong tool for same-cluster cloning: it replays
a logical dump instead of copying blocks. `--clean` "only drops objects that are
present in the dump and are about to be restored"
([pg_restore](https://www.postgresql.org/docs/current/app-pgrestore.html)), so a
stray table a student created survives a restore-based "reset" unless it collides
by name. Drop+recreate-from-template is the only guaranteed-clean reset.

No PostgreSQL-native fast per-schema reset primitive exists beyond
`DROP SCHEMA ... CASCADE` (absence noted from the docs, not a positive claim).

### 2.2 Isolation and abuse containment

By default "PostgreSQL grants privileges on some types of objects to PUBLIC ...
CONNECT and TEMPORARY ... for databases"
([Privileges](https://www.postgresql.org/docs/current/ddl-priv.html)) — i.e. every
login role can connect to every database unless revoked. `REVOKE CONNECT ON
DATABASE ... FROM PUBLIC` plus a grant to the one owning student role closes
cross-student access *at the connection layer*, before table grants matter. Since
a connection reaches exactly one database, this is a strong boundary — provided
`dblink`/`postgres_fdw` are not installable by students.

PG15 additionally removed "PUBLIC creation permission on the public schema" and
changed the public schema's owner to the `pg_database_owner` role, but
"[u]pgrading a cluster or restoring a database dump will preserve public's
existing permissions" ([PG15 release notes](https://www.postgresql.org/docs/15/release-15.html);
[predefined roles](https://www.postgresql.org/docs/current/predefined-roles.html)).
Template lineage that crosses a PG15 upgrade silently keeps the old permissive
grants.

Per-role guardrails, set at login time via `ALTER ROLE ... SET`
([ALTER ROLE](https://www.postgresql.org/docs/current/sql-alterrole.html)):

- `statement_timeout` — aborts runaway statements; the docs warn against setting
  it globally in `postgresql.conf` "because it would affect all sessions".
- `idle_in_transaction_session_timeout` — kills sessions idling inside an open
  transaction, "to ensure that idle sessions do not hold locks for an
  unreasonable amount of time". This is the DataGrip-left-open-mid-transaction
  case exactly.
- `idle_session_timeout` (PG14) — kills idle sessions outside transactions.
  (All three: [client connection defaults](https://www.postgresql.org/docs/current/runtime-config-client.html).)
- `temp_file_limit` — "A transaction attempting to exceed this limit will be
  canceled"; explicit temp *tables* do not count
  ([resource consumption](https://www.postgresql.org/docs/current/runtime-config-resource.html)).

`CONNECTION LIMIT` (per database and per role) is a soft cap only: "The
CONNECTION LIMIT option is only enforced approximately; if two new sessions start
at about the same time when just one connection 'slot' remains ... it is possible
that both will fail. Also, the limit is not enforced against superusers or
background worker processes"
([CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html);
[CREATE ROLE](https://www.postgresql.org/docs/current/sql-createrole.html)).

`pg_cancel_backend`/`pg_terminate_backend` are superuser-restricted by default but
grantable, and membership in `pg_signal_backend` lets a non-superuser ops role
signal other backends
([server signaling functions](https://www.postgresql.org/docs/current/functions-admin.html)) —
so an invigilator tool can kill one student's runaway query without superuser.

Escape hatches that must never reach a student role: `pg_read_server_files`,
`pg_write_server_files`, `pg_execute_server_program` — the docs warn these
"bypass all database-level permission checks when accessing files directly and
could be used to gain superuser-level access"
([predefined roles](https://www.postgresql.org/docs/current/predefined-roles.html)) —
plus untrusted procedural languages, where a PL/PerlU function "will be able to do
anything that could be done by a user logged in as the database administrator"
([PL/Perl trusted/untrusted](https://www.postgresql.org/docs/current/plperl-trusted.html)).

### 2.3 Thin clones and branching

**Database Lab Engine (postgres.ai)** keeps "a single, full-size copy of
production data on the specialized ZFS filesystem and uses the copy-on-write
capabilities of ZFS to generate full-size replicas ... in seconds", with ZFS or
LVM2 as pluggable backends, and claims "[c]loning of 10 TiB PostgreSQL database
takes less than 2 seconds when a single user is using the DBLab Engine instance,
and up to 30 seconds when 15 users are working with it at the same time"
([DBLab docs](https://postgres.ai/docs/database-lab)). Its shape — one gold
dataset, N cheap clones checked out and returned — is DbExam's pool model at a
different layer. Cost: a new control plane plus a ZFS/LVM commitment for an
on-prem team, and the fan-out numbers quoted are for tens of concurrent users,
not hundreds.

**Neon** branches by pointing "to an existing point in history and begin[ning] to
diverge from there using copy-on-write semantics ... a branch of a 100GB database
costs almost nothing until you start writing new data to it"
([Neon branching](https://neon.com/docs/introduction/branching)). This is
implemented inside Neon's storage/compute-separated engine, so it is a reference
architecture, not an adoptable component for self-managed PostgreSQL.

**Aurora fast cloning** likewise uses "a copy-on-write model, where only new data
is changed in the Aurora shared storage layer", and is capped at "a maximum of 15
clones for a cluster"
([AWS blog](https://aws.amazon.com/blogs/aws/amazon-aurora-fast-database-cloning)) —
a useful data point that even hyperscale CoW implementations cap clone fan-out.

**DIY ZFS snapshot+clone** for on-prem (PGDATA on a ZFS dataset, `zfs snapshot`,
`zfs clone`, clear `postmaster.pid`, start a second postmaster on another port) is
documented in community write-ups and a deck on the PostgreSQL wiki
([dbi-services](https://www.dbi-services.com/blog/running-postgresql-on-zfs-on-linux-fun-with-snapshots-and-clones/),
[PostgresOnZFS](https://wiki.postgresql.org/images/c/c5/PostgresOnZFS-PGO.pdf)).
The catch is architectural: each clone is a *separate postmaster on its own port*,
not a database inside one cluster — hundreds of students would mean hundreds of
postmasters, ports, and independent connection budgets. Strongest reset
primitive, heaviest ops model.

### 2.4 Pooling, containers, connections at scale

**Testcontainers reusable containers** (`withReuse(true)`,
`TESTCONTAINERS_REUSE_ENABLE=true`) are "an experimental capability ... by keeping
containers running and allowing multiple tests to reuse the same container, it's
possible to shave off container start times", and are explicitly "not suited for
CI usage" ([Testcontainers reuse](https://java.testcontainers.org/features/reuse/)).
The transferable part is not the container reuse but the pattern underneath it:
bake a template once, `CREATE DATABASE` per test against a long-lived cluster.

**`pg_tmp`/ephemeralpg** "reduces the wait time for a new database to less than
one second by initializing a database in the background that is used by
subsequent invocations" ([ephemeralpg](https://github.com/eradman/ephemeralpg)) —
independent confirmation that the warm-spare-replenished-in-background pattern is
the standard answer to "handing out a fresh database must feel instant".

**pgTAP** wraps every test script in a transaction: "All TAP scripts must be run
inside of a transaction and then get rolled back at the end so that any changes
you may have made to the data get reversed"
([pgTAP](https://pgtap.org/documentation.html)).

**PgBouncer** pooling modes matter more than they look. Session pooling assigns a
server connection "for the whole duration it stays connected"; transaction and
statement pooling recycle it per transaction/statement, and the feature matrix
marks `SET`/`RESET`, `LISTEN`, `WITH HOLD` cursors, `PREPARE`/`DEALLOCATE`,
session-level advisory locks and `LOAD` as never supported outside session mode
([PgBouncer features](https://www.pgbouncer.org/features.html)). DataGrip and psql
are long-lived stateful sessions that use exactly those features, so DbExam's
student-facing endpoint is confined to session pooling — which smooths reconnect
churn but does **not** reduce live backend count the way transaction pooling does
for a stateless web app.

`max_connections` is not free: "PostgreSQL sizes certain resources based directly
on the value of max_connections. Increasing its value leads to higher allocation
of those resources, including shared memory"
([connections and authentication](https://www.postgresql.org/docs/current/runtime-config-connection.html)).
PG14 reduced the per-connection cost of snapshot acquisition — `GetSnapshotData()`
was described in the development thread as "the largest reason for connection
scalability issues"
([pgsql-hackers](https://www.postgresql.org/message-id/20200301083601.ews6hz5dduc3w2se@alap3.anarazel.de);
explanatory write-up: [Citus](https://www.citusdata.com/blog/2020/10/25/improving-postgres-connection-scalability-snapshots/)).
**[unverified]** against the official PG14 release-notes wording; the mechanism is
sourced from the commit thread. Practical read: run PG14+ deliberately, and treat
"hundreds of held interactive sessions" as a sizing problem no pooler removes.

No citable primary-source figures were found for per-student Docker Postgres
containers; any resource-cost number for that path would have to come from our own
benchmark. **[unverified]**

### 2.5 Provisioning gotchas worth writing down

1. `CREATE DATABASE ... TEMPLATE` **fails**, it does not degrade, if anything is
   connected to the template.
2. `FILE_COPY` forces a checkpoint before *and* after each clone — a burst of pool
   refills becomes a burst of checkpoints competing with student I/O.
3. `DROP DATABASE ... WITH (FORCE)` still fails on prepared transactions or active
   logical replication slots — a stuck 2PC in a student database blocks the swap.
4. `CONNECTION LIMIT` is racy and ignores superusers/background workers; it needs
   an application-level backstop to be a real quota.
5. Pre-PG15 template lineage keeps the old permissive public-schema grants through
   `pg_upgrade`/restore.
6. PgBouncer transaction/statement mode silently breaks the session features
   DataGrip relies on.
7. ZFS-clone-per-postmaster is a different isolation model; adopting it halfway
   (assuming single-cluster connection accounting still applies) is the trap.
8. `pg_restore --clean` does not remove student-created objects absent from the
   dump.

---

## Part 3 — Ideas worth stealing, per area of the risky core

### Provisioning (environments and templates)

1. **Keep the physical template permanently unconnectable.** `CREATE DATABASE ...
   TEMPLATE` fails hard if any session is attached, so the template database must
   never be a place a human or a monitoring probe visits. Give it `REVOKE CONNECT
   ... FROM PUBLIC` and rebuild it out-of-band. This turns an intermittent
   race into a structural invariant.
   ([CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html))
2. **Treat `STRATEGY` as a tuning knob, not a default.** `WAL_LOG` is optimized for
   small templates; `FILE_COPY` trades WAL volume for two checkpoints per clone.
   Which one wins depends on our template size and refill burst rate — this belongs
   in the provisioning benchmark, not in an assumption.
   ([CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html))
3. **Steal pg4e's provisioning recipe verbatim.** `CREATE ROLE ... NOSUPERUSER
   NOCREATEDB NOCREATEROLE`, `GRANT connect ON DATABASE ... TO <student>`, `REVOKE
   all ON DATABASE ... FROM public`, then hand the student a ready-made `psql`
   command line alongside the raw host/port/db/user/password. It is a working,
   in-production shape for exactly our credential-handout step.
   ([00setup.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/00setup.php))

### Pooling and reset (managed databases, claim, swap)

4. **`DROP DATABASE ... WITH (FORCE)` is the swap primitive.** It terminates
   lingering student connections itself, so a swap does not need a
   `pg_terminate_backend` loop — but it still refuses on prepared transactions and
   logical replication slots, which the swap path must detect and report rather
   than retry forever.
   ([DROP DATABASE](https://www.postgresql.org/docs/current/sql-dropdatabase.html))
5. **Background-warmed spares are the established answer, not an invention.**
   `pg_tmp` "reduces the wait time for a new database to less than one second by
   initializing a database in the background", and DBLab keeps a pre-snapshotted
   pool for the same reason. Our pool/claim/replenish design is the standard shape.
   ([ephemeralpg](https://github.com/eradman/ephemeralpg),
   [DBLab](https://postgres.ai/docs/database-lab))
6. **Never reset by restoring over a dirty database.** `pg_restore --clean` leaves
   student-created objects that were not in the dump. Drop-and-clone-from-template
   is the only reset with a guarantee.
   ([pg_restore](https://www.postgresql.org/docs/current/app-pgrestore.html))
7. **Keep thin-cloning (ZFS/DBLab) on the shelf, priced.** If template sizes ever
   make `CREATE DATABASE` too slow, DBLab-style CoW cloning is the documented
   escalation — at the cost of a ZFS/LVM commitment and, in the DIY variant, one
   postmaster and port per clone rather than one cluster.
   ([DBLab](https://postgres.ai/docs/database-lab),
   [PostgresOnZFS](https://wiki.postgresql.org/images/c/c5/PostgresOnZFS-PGO.pdf))

### Isolation and abuse containment

8. **Defend at the connection layer first.** PostgreSQL grants CONNECT to PUBLIC by
   default, so every student role can reach every student database until revoked.
   Per-database `REVOKE CONNECT ... FROM PUBLIC` plus a single grant makes
   cross-student reads impossible before table grants are even considered — pg4e
   does exactly this.
   ([Privileges](https://www.postgresql.org/docs/current/ddl-priv.html))
9. **Push guardrails onto the role, not the server.** `ALTER ROLE student_x SET
   statement_timeout / idle_in_transaction_session_timeout / temp_file_limit`
   applies at login without touching other sessions — the docs specifically warn
   against setting `statement_timeout` globally. This covers the runaway query, the
   DataGrip window left open mid-transaction holding locks, and the disk-filling
   sort.
   ([client defaults](https://www.postgresql.org/docs/current/runtime-config-client.html),
   [ALTER ROLE](https://www.postgresql.org/docs/current/sql-alterrole.html))
10. **Give the invigilator `pg_signal_backend`, not superuser.** It is enough to
    cancel or kill one student's backend and nothing more.
    ([signaling functions](https://www.postgresql.org/docs/current/functions-admin.html))
11. **Maintain an explicit never-grant list.** `pg_read_server_files`,
    `pg_write_server_files`, `pg_execute_server_program`, untrusted PLs, `dblink`,
    `postgres_fdw` — the first three "could be used to gain superuser-level access"
    per the docs, and the last two would tunnel straight through the per-database
    connection boundary.
    ([predefined roles](https://www.postgresql.org/docs/current/predefined-roles.html),
    [PL/Perl](https://www.postgresql.org/docs/current/plperl-trusted.html))
12. **Verify the public-schema grants on every template.** The PG15 secure default
    does not propagate through `pg_upgrade` or a restored dump.
    ([PG15 release notes](https://www.postgresql.org/docs/15/release-15.html))

### Grading and checks

13. **Make the all-or-nothing option explicit per task.** CodeRunner ships partial
    credit and all-or-nothing as a deliberate author-facing switch; instructors
    wanted both. DbExam's "points = sum of passed checks" should either offer the
    same switch or reject it on the record.
    ([CodeRunner lang strings](https://github.com/trampgeek/moodle-qtype_coderunner/blob/master/lang/en/qtype_coderunner.php))
14. **Fix the denominator.** CodeRunner's own forum documents authors being
    surprised that adding a weighted test changes the total, not the share. Task
    points must be the declared denominator, with check weights normalized against
    it, not summed into it.
    ([partial grading thread](https://coderunner.org.nz/mod/forum/discuss.php?d=576))
15. **Run checks inside `BEGIN ... ROLLBACK` wherever a check can be expressed as
    an assertion.** pgTAP requires exactly this so that "any changes you may have
    made to the data get reversed" — zero-residue grading against a live mutable
    student database, no snapshot needed.
    ([pgTAP](https://pgtap.org/documentation.html))
16. **Consider a fifth check axis: static/parse-tree validation.** The SIGCSE 2025
    CodeRunner extension checks output correctness *and* validates the statement
    against the dialect's grammar parse tree. That expresses constraints
    (`RESULT_MATCH`, `SCHEMA_MATCH`, `STATE_MATCH`, `PERF_CHECK` cannot) such as
    "must use a window function" or "must not use a cursor".
    ([ACM](https://dl.acm.org/doi/10.1145/3641554.3701830))
17. **Discriminating seed data is a grading feature, not a content chore.** XData
    generates datasets specifically to kill wrong-query mutants; a `RESULT_MATCH`
    check over a weak dataset silently passes near-misses. At minimum, give
    instructors a way to test a check against known-wrong answers.
    ([XData](http://www.cse.iitb.ac.in/infolab/xdata/),
    [arXiv](https://arxiv.org/pdf/1411.6704))
18. **Park query-equivalence partial credit as a long-horizon idea.** Awarding
    credit *within* a failed check, scaled by distance to a correct query, is a
    published technique — a research investment, not a v1 feature, but the direction
    exists if per-check granularity proves too coarse.
    ([PVLDB 2016](http://www.vldb.org/pvldb/vol9/p1541-chandra.pdf),
    [Cosette](https://www.cidrdb.org/cidr2017/papers/p51-chu-cidr17.pdf))

### Submission capture

19. **The grader-connects-back pattern is proven.** pg4e's autograders open a
    connection into the student's own database using the student's stored
    credentials and inspect it, rather than only reading typed text
    (`pg4e_get_user_connection`). That is the mechanism DbExam's pulled submissions
    need, already running in a real course.
    ([00first.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/00first.php))
20. **A convention table inside the student's database is a usable capture
    channel.** pg4e requires a `pg4e_debug` table in the student's database and
    refuses to grade without it (`pg4e_check_debug_table`), plus a `pg4e_meta`
    table for grader bookkeeping. An analogous engine-owned table (or schema) in
    each managed database is a cheap way to record what the engine saw, in the same
    place the evidence lives — worth weighing against keeping all such state in the
    API database.
    ([00first.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/00first.php),
    [00setup.php](https://raw.githubusercontent.com/csev/pg4e/master/tools/sql/00setup.php))
21. **Separate the execution/check tier from the app tier.** CodeRunner addresses
    Jobe servers over HTTP and can pin a question to a specific Jobe instance
    "allowing for load balancing and specialist Jobe servers". DbExam's check
    execution is stateful in a way Jobe is not, but the topology — a separately
    addressable, separately scalable grading tier — is the right answer for
    hundreds of students and connects directly to map issue #21's open
    "check-execution fairness under load" question.
    ([jobe](https://github.com/trampgeek/jobe))

### Connections at scale

22. **Session pooling or nothing.** PgBouncer's own feature matrix marks `SET`,
    `LISTEN`, `WITH HOLD` cursors, `PREPARE`/`DEALLOCATE` and session advisory
    locks as unsupported outside session mode — exactly what DataGrip and psql use.
    Transaction pooling, the mode that would actually reduce backend count, is off
    the table for student traffic.
    ([PgBouncer features](https://www.pgbouncer.org/features.html))
23. **Run PG14+ deliberately, and size `max_connections` as a real cost.** Shared
    memory allocation scales with `max_connections`, and PG14's `GetSnapshotData`
    rework targeted precisely the many-concurrent-sessions case.
    ([connections](https://www.postgresql.org/docs/current/runtime-config-connection.html),
    [pgsql-hackers](https://www.postgresql.org/message-id/20200301083601.ews6hz5dduc3w2se@alap3.anarazel.de))
24. **Do not sell `CONNECTION LIMIT` as a quota.** It is documented as approximate,
    racy under simultaneous connects, and unenforced against superusers — a hint,
    with the application as the real backstop.
    ([CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html))

---

## Part 4 — The kill-check

> Does any existing system cover the core requirement — per-student *mutable*
> PostgreSQL databases, procedures and triggers, an external-tool-native workflow
> (DataGrip/psql plus pulled submissions), and instructor-defined checks with
> partial credit — well enough that building DbExam is a mistake?

### Verdict: **No. Build DbExam.** Adoption does not return to this map.

The requirement is a conjunction of four properties. Every surveyed system
satisfies some and structurally fails at least one, and the closest three fail on
different ones:

| Candidate | Mutable per-student PG | Procedures / triggers graded | External client + pulled submissions | Instructor checks with partial credit | Fits our axioms |
|---|---|---|---|---|---|
| Moodle CodeRunner (built-in SQL) | No — ephemeral SQLite | No | No — textarea, Jobe wipes the workdir | Yes — per-test marks or all-or-nothing | No |
| CodeRunner + SIGCSE 2025 DB extension | Yes — per-exercise Docker PG container | Unverified | **Unverified** (ACM/RG 403) | Inherits CodeRunner's | No — requires Moodle + Jobe |
| pg4e | **Yes** — real per-student PG database | No | **Yes** for connect + grader-connects-back | No — flat `$gradetosend = 1.0` | No — PHP/Tsugi MOOC grader |
| Vocareum | Yes — isolated per-learner PG | Unverified | Partly — web clients/notebooks; no documented external client | Validation scripts; partial credit undocumented | No — SaaS, no documented self-hosting |
| XData / VLDB partial marking | No | No | No | Yes — sophisticated | Technique, not a system |
| Gradescope | Only if instructor-scripted | Only if instructor-scripted | No — file upload | Instructor-scripted | Generic, no DB provisioning |
| SQLZoo / pgexercises / LeetCode / HackerRank | No — shared or ephemeral | No | No | Varies / none | No |

Three findings drive the verdict:

1. **The external-tool-native workflow is essentially unoccupied.** Only pg4e was
   confirmed to hand a student real PostgreSQL credentials for a persistent
   database they own, and pg4e's grading is binary with no check taxonomy, no exam
   sitting, no pool, and no swap. Everything else is a textarea.
2. **Pulled submissions and side-effect grading of procedures/triggers are
   undocumented anywhere.** No primary source was found for extracting a submission
   by introspecting a student-mutated database, nor for invoking a student
   procedure and diffing resulting state. These are DbExam's `PULLED` and
   `STATE_MATCH` respectively — the two capabilities that most define the product,
   and the two with no prior art to adopt.
3. **The nearest candidates conflict with the map's fixed axioms, not merely with
   our preferences.** The SIGCSE 2025 extension is a Moodle plugin driving Jobe;
   adopting it means adopting Moodle as the platform, which the Java/Spring +
   Next.js axiom forecloses. Vocareum is cloud SaaS with no documented self-hosted
   deployment, against an on-prem, team-administered PostgreSQL operating envelope.
   pg4e is a PHP/Tsugi MOOC grader, adoptable as a *pattern* (and it is stolen from
   above, in Part 3) but not as a system.

What genuinely exists to reuse is **technology, not products**: PostgreSQL's own
`CREATE DATABASE ... TEMPLATE` / `DROP DATABASE ... WITH (FORCE)` / role-scoped
timeouts / connection-layer privilege revocation, plus pgTAP's rollback-wrapped
assertion discipline, plus the pooling and check-design patterns catalogued in
Part 3. That is a build with strong borrowed foundations, not a rebuild of
something that already exists.

### Residual uncertainty, recorded and bounded

Two items could not be verified and are worth a cheap follow-up — but neither can
flip this verdict, only change how much we borrow:

- The **SIGCSE 2025 CodeRunner extension**'s external-client capability
  ([archived repo](https://github.com/andre-wojtowicz/coderunner-sql-sigcse2025)).
  Even if it allows external clients, it remains a Moodle plugin; its value to us
  is its container provisioning and cleanup scripts, which the repo exposes without
  the paywalled paper.
- Whether **Vocareum** sells external-client access or on-prem deployment. Even a
  yes leaves the partial-credit check model undocumented and the platform outside
  our stack; the realistic outcome is a comparison data point, not an adoption.

Per map issue #21's alternatives policy, the kill-check is now answered and
adoption is closed as a topic. These two follow-ups, if anyone runs them, are
information-gathering about *mechanisms to steal*, not a reopening of the question.
