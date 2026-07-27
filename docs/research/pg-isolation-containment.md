# PostgreSQL isolation & abuse-containment facts

Research for [#23](https://github.com/MarkoPopovikj/DbExamAPI/issues/23) (map: [#21](https://github.com/MarkoPopovikj/DbExamAPI/issues/21)).
**Facts with sources, no verdict** — this feeds the isolation-unit decision.

**Envelope**: one shared, self-managed PostgreSQL instance (on-prem/VM), hundreds of concurrent
students in one sitting, each holding a real login role with DDL rights in their own database or
schema, connecting from DataGrip/psql. Hostile-or-clumsy students assumed.

**Version basis**: PG 15/16/17. `docs/current` currently resolves to PG 18; version-sensitive claims
cite a pinned version or a release note. Anything not groundable in a primary source is marked
**unverified** — do not build on those.

---

## 1. Database-per-student vs schema-per-student

### 1.1 What the docs themselves say about the choice

- The docs answer this question directly: *"If one PostgreSQL server cluster is planned to contain
  unrelated projects or users that should be, for the most part, unaware of each other, it is
  recommended to put them into separate databases and adjust authorizations and access controls
  accordingly. If the projects or users are interrelated, and thus should be able to use each
  other's resources, they should be put in the same database but probably into separate schemas."*
  — [22.1 Overview](https://www.postgresql.org/docs/current/manage-ag-overview.html)
- The sharpest constraint on the schema model, verbatim: the user-private-schema pattern *"is a
  secure schema usage pattern **unless an untrusted user is the database owner** or has been granted
  `ADMIN OPTION` on a relevant role, in which case **no secure schema usage pattern exists**."*
  — [5.9.6 Usage Patterns](https://www.postgresql.org/docs/current/ddl-schemas.html)
- Cross-database queries are structurally impossible in one session: *"It is not possible to access
  more than one database per connection."* Escape hatches are `postgres_fdw` and `dblink`, both of
  which require installing an extension.
  — [22.1 Overview](https://www.postgresql.org/docs/current/manage-ag-overview.html)

### 1.2 Default `PUBLIC` grants — the leak list

- Verbatim: *"No privileges are granted to `PUBLIC` by default on tables, table columns, sequences,
  foreign data wrappers, foreign servers, large objects, schemas, tablespaces, or configuration
  parameters. For other types of objects, the default privileges granted to `PUBLIC` are as follows:
  `CONNECT` and `TEMPORARY` (create temporary tables) privileges for databases; `EXECUTE` privilege
  for functions and procedures; and `USAGE` privilege for languages and data types (including
  domains)."* — [5.8 Privileges](https://www.postgresql.org/docs/current/ddl-priv.html)
- **`CONNECT` to PUBLIC on every new database is the default.** Without an explicit `REVOKE`, any
  student role can `psql -d otherstudent_db`. Same page.
- **`TEMPORARY` to PUBLIC is also default** — any role that can connect can create temp tables and
  consume temp space, and can populate `pg_temp` (see the `SECURITY DEFINER` trap, §1.5).
- **`EXECUTE` on functions to PUBLIC is default** — in a shared database every student's function is
  callable by every other student unless explicitly revoked or `ALTER DEFAULT PRIVILEGES` is set.
- Reading current state is a trap: *"If the 'Access privileges' column is empty for a given object,
  it means the object has default privileges."* An empty `datacl` means **CONNECT+TEMP to PUBLIC**,
  not "no access". Same page.
- Owners can undo your REVOKEs on their own objects: *"The right to modify or destroy an object is
  inherent in being the object's owner, and cannot be granted or revoked in itself"*; *"owners are
  always treated as holding all grant options, so they can always re-grant their own privileges."*
  Same page.
- `REVOKE ... FROM PUBLIC` is not absolute: *"any particular role will have the sum of privileges
  granted directly to it, privileges granted to any role it is presently a member of, and privileges
  granted to `PUBLIC`."* — [REVOKE, Notes](https://www.postgresql.org/docs/current/sql-revoke.html)

### 1.3 Closing the database boundary

- `CONNECT` is a **connection-time check only**: *"This privilege is checked at connection startup
  (in addition to checking any restrictions imposed by `pg_hba.conf`)."* It does not hide the
  database's existence or metadata. — [5.8 Privileges](https://www.postgresql.org/docs/current/ddl-priv.html)
- **Database ACLs are not inherited from the template**: *"`CREATE DATABASE` does not copy
  database-level `GRANT` permissions attached to the source database. The new database has default
  database-level permissions."* — [22.3 Template Databases](https://www.postgresql.org/docs/current/manage-ag-templatedbs.html)
  ⇒ revoking CONNECT on `template1` does **not** make new databases private; the REVOKE must run
  per created database, i.e. it must be part of provisioning.
- Stronger than a REVOKE: `ALLOW_CONNECTIONS` / `datallowconn` — *"If false then no one can connect
  to this database."* — [CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html)
- `pg_hba.conf` gates per database and **has no schema equivalent**: the database field accepts
  `sameuser` (*"the record matches if the requested database has the same name as the requested
  user"*) and `samerole`. That is a one-line, self-maintaining lockdown available only in the
  database model. — [21.1 pg_hba.conf](https://www.postgresql.org/docs/current/auth-pg-hba-conf.html)
- PG16 added regex matching on `pg_hba.conf` database/role entries (*"Regular expression patterns are
  prefixed with a slash"*) — [PG16 release notes](https://www.postgresql.org/docs/release/16.0/)
- The community baseline for shared clusters: `REVOKE ALL ON DATABASE template1 FROM public;`
  `REVOKE ALL ON SCHEMA public FROM public;` then per-user `GRANT USAGE ON SCHEMA public TO <user>;`
  `GRANT CONNECT,TEMPORARY ON DATABASE <db> TO <user>;`
  — [wiki: Shared Database Hosting](https://wiki.postgresql.org/wiki/Shared_Database_Hosting)

### 1.4 What leaks in BOTH models (unclosable)

- **Role names**: *"`pg_roles` is a publicly readable view on `pg_authid` that blanks out the password
  field."* Every student can enumerate every other role name plus attributes (CREATEDB, SUPERUSER,
  CONNECTION LIMIT, VALID UNTIL). — [pg_authid](https://www.postgresql.org/docs/current/catalog-pg-authid.html)
  Same for `pg_user` over `pg_shadow` — [pg_shadow](https://www.postgresql.org/docs/current/view-pg-shadow.html)
- **Password hashes are safe**: `pg_authid` *"contains passwords, it must not be publicly readable"*;
  `pg_shadow` likewise. Students cannot read `rolpassword`. Same two pages.
- **Database names + owners + ACLs**: `pg_database` is a shared catalog — *"there is only one copy of
  `pg_database` per cluster, not one per database."*
  — [pg_database](https://www.postgresql.org/docs/current/catalog-pg-database.html)
- The wiki states the ceiling flatly: *"pg_catalog allows access to database and user names for
  everyone, however **access to this schema cannot be disabled without breaking essential
  features**."* — [wiki: Shared Database Hosting](https://wiki.postgresql.org/wiki/Shared_Database_Hosting)
- **Session existence**: *"Ordinary users can only see all the information about their own sessions
  … In rows about other sessions, many columns will be null. **Note, however, that the existence of a
  session and its general properties such as its sessions user and database are visible to all
  users.**"* — [27.2 Cumulative Statistics System (PG17)](https://www.postgresql.org/docs/17/monitoring-stats.html)
  ⇒ in either model a student can see *that* student X is connected to database Y, but not their
  query text.
- That restriction covers `pg_stat_activity`, `pg_stat_replication`, `pg_stat_ssl`, `pg_stat_gssapi`,
  the `pg_stat_progress_*` views and others in Table 27.1. Same page.
- **`pg_stat_statements` is cluster-wide**: *"When `pg_stat_statements` is active, it tracks
  statistics across all databases of the server."* Query text is protected — *"only superusers and
  roles with privileges of the `pg_read_all_stats` role are allowed to see the SQL text and `queryid`
  of queries executed by other users"* — but **call counts, timings and row counts keyed by `userid`
  and `dbid` are visible to any user in a database where the view is installed.** This leak is
  identical in both models. — [F.32 pg_stat_statements](https://www.postgresql.org/docs/current/pgstatstatements.html)
- `pg_stat_database` has *"one row for each database in the cluster"* — another cluster-wide signal.
  — [27.2 Monitoring](https://www.postgresql.org/docs/17/monitoring-stats.html)
- Roles that defeat all of the above and **must not be granted to students**: `pg_read_all_stats`,
  `pg_monitor` (a member of `pg_read_all_settings` + `pg_read_all_stats` + `pg_stat_scan_tables`),
  `pg_read_all_settings`, `pg_read_all_data`, `pg_write_all_data`, `pg_read_server_files`,
  `pg_write_server_files`, `pg_execute_server_program`, `pg_signal_backend`.
  — [21.5 Predefined Roles](https://www.postgresql.org/docs/current/predefined-roles.html)
- The monitoring roles arrived in PG10: *"New roles `pg_monitor`, `pg_read_all_settings`,
  `pg_read_all_stats`, and `pg_stat_scan_tables`."* — [PG10 release notes](https://www.postgresql.org/docs/release/10.0/)

### 1.5 Schema-per-student specifics

- **PG15 changed the `public` schema default**: *"Remove `PUBLIC` creation permission on the `public`
  schema (Noah Misch) … The change applies to new database clusters and to newly-created databases in
  existing clusters. **Upgrading a cluster or restoring a database dump will preserve `public`'s
  existing permissions.**"* — [PG15 release notes](https://www.postgresql.org/docs/release/15.0/)
  ⇒ a PG15/16/17 cluster restored from an older dump still behaves like PG14. Echoed in the docs:
  *"In databases upgraded from PostgreSQL 14 or earlier, everyone has that privilege on the schema
  `public`."* — [5.9 Schemas](https://www.postgresql.org/docs/current/ddl-schemas.html)
- **`USAGE` on `public` is still granted to PUBLIC in PG15+** — PG15 removed CREATE, not USAGE:
  *"By default, everyone has that privilege on the schema `public`."* Same page.
- PG15 also moved ownership: *"Change the owner of the `public` schema to be the new
  `pg_database_owner` role."* That role *"always has exactly one implicit member: the current
  database owner."* ⇒ in database-per-student, making the student the DB owner hands them ownership
  of their own `public` schema. — [PG15 release notes](https://www.postgresql.org/docs/release/15.0/),
  [21.5 Predefined Roles](https://www.postgresql.org/docs/current/predefined-roles.html)
- **Metadata is readable without USAGE.** The load-bearing sentence: *"For schemas, allows access to
  objects contained in the schema … **Without this permission, it is still possible to see the object
  names, e.g., by querying system catalogs.**"* — [5.8 Privileges](https://www.postgresql.org/docs/current/ddl-priv.html)
  ⇒ `pg_class` / `pg_attribute` / `pg_namespace` expose other students' table and column names
  regardless of object privileges. A student cannot read another's *data* without SELECT, but can
  read the *shape of their answer*.
- `CREATE` on a **database** is broader than it looks: *"For databases, allows new schemas and
  publications to be created within the database, and allows **trusted extensions** to be installed
  within the database."* A student with CREATE on a shared database can create arbitrary additional
  schemas, not just their own. Same page.
- **Large objects have no schema at all**: *"All large objects are stored in a single system table
  named `pg_largeobject`."* The schema boundary does not partition them; the database boundary does.
  — [33.1 Large Objects](https://www.postgresql.org/docs/current/lo-intro.html)

### 1.6 `search_path` pitfalls (CVE-2018-1058 class)

- The threat model, verbatim: *"adding a schema to `search_path` effectively trusts all users having
  `CREATE` privilege on that schema. When you run an ordinary query, **a malicious user able to create
  objects in a schema of your search path can take control and execute arbitrary SQL functions as
  though you executed them**."* — [5.9.6 Usage Patterns](https://www.postgresql.org/docs/current/ddl-schemas.html)
- [CVE-2018-1058](https://www.postgresql.org/support/security/CVE-2018-1058/) — *"Uncontrolled search
  path element in pg_dump and other client applications"*, CVSS 3.0 **8.8**
  (`AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:H/A:H`), affected 9.3–10, fixed in 10.3/9.6.8/9.5.12/9.4.17/9.3.22.
- The worked attack: a user creates `lower(varchar)` in `public` to intercept calls intended for
  `pg_catalog.lower()`. The 2018 response was **documentation and client hardening, not a server
  behaviour change** — *"added documentation that described the issue and how to take steps to
  mitigate the impact."*
  — [wiki: A Guide to CVE-2018-1058](https://wiki.postgresql.org/wiki/A_Guide_to_CVE-2018-1058:_Protect_Your_Search_Path)
- Documented mitigations: `REVOKE CREATE ON SCHEMA public FROM PUBLIC;`,
  `ALTER ROLE username SET search_path = "$user";`, or a cluster-wide `search_path` in
  `postgresql.conf`. Same wiki page.
- The wiki also warns `pg_dump` *"does not preserve the `public` schema removal"* — dump/restore can
  silently reintroduce the insecure state (consistent with the PG15 release-note caveat above).
  **Directly relevant to template-based provisioning.**
- `$user` semantics: default is `"$user", public`; *"The first element specifies that a schema with
  the same name as the current user is to be searched. If no such schema exists, the entry is
  ignored"*; *"The first schema in the search path that exists is the default location for creating
  new objects."* — [5.9 Schemas](https://www.postgresql.org/docs/current/ddl-schemas.html)
- **`ALTER DATABASE ... SET search_path` is defeatable by the student**: *"Role-specific settings
  override database-specific ones if there is a conflict."*
  — [ALTER DATABASE](https://www.postgresql.org/docs/current/sql-alterdatabase.html) (see §2.5)
- `SECURITY DEFINER` + `pg_temp` trap: *"Particularly important in this regard is the temporary-table
  schema, which is searched first by default, and is normally writable by anyone. A secure arrangement
  can be obtained by forcing the temporary schema to be searched last. To do this, write `pg_temp` as
  the last entry in `search_path`."* Recommended form: `SET search_path = admin, pg_temp` attached to
  the function. — [CREATE FUNCTION, "Writing SECURITY DEFINER Functions Safely"](https://www.postgresql.org/docs/current/sql-createfunction.html)
  **This applies to any grading/checking function the engine runs against a student database.**
- PG17 hardened a related surface: *"Change functions to use a safe `search_path` during maintenance
  operations (Jeff Davis) — This prevents maintenance operations (`ANALYZE`, `CLUSTER`, `CREATE INDEX`,
  `CREATE MATERIALIZED VIEW`, `REFRESH MATERIALIZED VIEW`, `REINDEX`, or `VACUUM`) from performing
  unsafe access."* Previously a hostile student's expression index was a vector against whoever ran
  maintenance. — [PG17 release notes](https://www.postgresql.org/docs/release/17.0/)

### 1.7 Other database-boundary-only levers

- **Per-database GUC defaults**: `ALTER DATABASE ... SET`, *"Only the database owner or a superuser
  can change the session defaults for a database."*
  — [ALTER DATABASE](https://www.postgresql.org/docs/current/sql-alterdatabase.html)
- **Per-database `CONNECTION LIMIT`** — no schema equivalent (see §2.4).
- **`pg_dump` scope maps to the database boundary**: *"pg_dump only dumps a single database."*
  Per-student dump/restore/reset is a one-command operation in the database model.
  — [pg_dump](https://www.postgresql.org/docs/current/app-pgdump.html)
- **Encoding/collation is per-database, not per-schema**: *"New encoding and locale settings can be
  specified when copying `template0`, whereas a copy of `template1` must use the same settings it
  does."* — [22.3 Template Databases](https://www.postgresql.org/docs/current/manage-ag-templatedbs.html)
- **Extensions are per-database** (`CREATE EXTENSION` *"loads a new extension into the current
  database"*) — in schema-per-student one student's install is visible to all.
  — [CREATE EXTENSION](https://www.postgresql.org/docs/current/sql-createextension.html)
- **`template1` propagation works only for the database model**: objects added to `template1` are
  copied into subsequently created databases — *"if you install the procedural language PL/Perl in
  `template1`, it will automatically be available in user databases"* — but recall ACLs are **not**
  copied. — [22.3 Template Databases](https://www.postgresql.org/docs/current/manage-ag-templatedbs.html)
- Creating databases needs a grant: *"To create a database, you must be a superuser or have the
  special `CREATEDB` privilege."* — [CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html)
- **Role management escapes both boundaries.** PG15: *"Remove the default `ADMIN OPTION` privilege a
  login role has on its own role membership."* PG16: *"Restrict the privileges of `CREATEROLE` and its
  ability to modify other roles … Previously roles with `CREATEROLE` privileges could change many
  aspects of any non-superuser role."* ⇒ **do not grant students CREATEROLE, and especially not on
  PG ≤ 15.** — [PG15](https://www.postgresql.org/docs/release/15.0/) /
  [PG16 release notes](https://www.postgresql.org/docs/release/16.0/)
- PG17 added the `MAINTAIN` privilege and `pg_maintain` role (VACUUM, ANALYZE, REINDEX, REFRESH MV,
  CLUSTER, LOCK TABLE per table) — useful for a non-superuser ops role.
  — [PG17 release notes](https://www.postgresql.org/docs/release/17.0/)

### 1.8 Scale of many databases vs many schemas

- Hard limits are irrelevant at this size: databases = **4,294,950,911**, relations per database =
  **1,431,650,303**. — [Appendix K. PostgreSQL Limits](https://www.postgresql.org/docs/current/limits.html)
- **The one sourced operational cost of many databases is autovacuum scheduling**: *"The launcher will
  distribute the work across time, attempting to start one worker within each database every
  `autovacuum_naptime` seconds. (Therefore, if the installation has N databases, a new worker will be
  launched every `autovacuum_naptime`/N seconds.) A maximum of `autovacuum_max_workers` worker
  processes are allowed to run at the same time. If there are more than `autovacuum_max_workers`
  databases to be processed, the next database will be processed as soon as the first worker
  finishes."* — [25.1.6 The Autovacuum Daemon](https://www.postgresql.org/docs/current/routine-vacuuming.html)
  With default `autovacuum_naptime = 1min` and ~300 student databases, a worker launches roughly every
  200 ms and 3 default workers cycle 300 databases (*arithmetic ours; the formula is quoted*).
- Worker starvation, same page: *"If several large tables all become eligible for vacuuming in a short
  amount of time, all autovacuum workers might become occupied … This would result in other tables and
  databases not being vacuumed until a worker becomes available."*
- `pg_stat_statements` shared memory is fixed at load and cluster-global: *"The module requires
  additional shared memory proportional to `pg_stat_statements.max`. Note that this memory is consumed
  whenever the module is loaded."* With hundreds of students running distinct queries, entries will be
  evicted in either model. — [F.32](https://www.postgresql.org/docs/current/pgstatstatements.html)
- The wiki notes two operational gaps for shared hosting: *"no support for limiting database sizes"*
  and *"removing all rights granted to user (prerequisite to deleting that user) must be done for each
  single object."* — [wiki: Shared Database Hosting](https://wiki.postgresql.org/wiki/Shared_Database_Hosting)

### 1.9 Unverified in §1

Do not build on these — they were searched for and **not** found in primary sources:

- That psql `\l` still lists databases after `REVOKE CONNECT` (follows from `pg_database` being
  world-readable, but not directly documented).
- The exact per-column list of which `pg_stat_activity` columns are nulled for other users' sessions —
  the docs only guarantee session user + database remain visible.
- The pre-PG10 `<insufficient privilege>` string in `pg_stat_activity.query` and the release that
  changed it to NULL.
- Whether `information_schema` views filter by current-user privileges (**not** stated in the chapter
  intro; the `pg_catalog` route is documented as unfiltered either way).
- Whether hidden `pg_stat_statements` query text renders as NULL or a placeholder.
- `pg_locks` row visibility for non-superusers — no privilege statement on its page and it is not in
  Table 27.1, so the restriction paragraph does not cover it. Argument from silence. (Note §3.8: the
  page does say *"`pg_locks` provides a global view of all locks in the database cluster."*)
- All catalog-scale/performance claims about many databases vs many schemas other than the autovacuum
  launcher formula (catalog bloat, per-database disk/inode overhead, planning-time degradation at
  hundreds of schemas).

---

## 2. Resource-abuse containment

### 2.1 The rule that decides everything: GUC context

- `user` context (USERSET): *"These settings can be set from `postgresql.conf`, or within a session via
  the `SET` command. **Any user is allowed to change their session-local value.**"*
- `superuser` context (SUSET): *"only superusers and users with the appropriate `SET` privilege can
  change them via `SET`."*
- `postmaster`: *"can only be applied when the server starts, so any change requires restarting the
  server."*
- `backend`: settable in the connection request packet (libpq `PGOPTIONS`) — *"any user can make such
  a change for their session"*, but *"these settings never change in a session after it is started."*
- All four — [pg_settings, `context` column](https://www.postgresql.org/docs/17/view-pg-settings.html)

### 2.2 Classification table — can a student undo it?

Context constants verified against
[`guc_tables.c` @ REL_17_STABLE](https://raw.githubusercontent.com/postgres/postgres/REL_17_STABLE/src/backend/utils/misc/guc_tables.c);
defaults and the SUSET-implying doc sentences from the pages cited below.

| Parameter | Default | Context | Student can set it back? |
|---|---|---|---|
| `statement_timeout` | 0 (off) | USERSET | **Yes** |
| `lock_timeout` | 0 (off) | USERSET | **Yes** |
| `idle_in_transaction_session_timeout` | 0 (off) | USERSET | **Yes** |
| `idle_session_timeout` (PG14+) | 0 (off) | USERSET | **Yes** |
| `transaction_timeout` (PG17+) | 0 (off) | USERSET | **Yes** |
| `work_mem` | 4MB | USERSET | **Yes** |
| `hash_mem_multiplier` | 2.0 | USERSET | **Yes** |
| `maintenance_work_mem` | 64MB | USERSET | **Yes** |
| `temp_buffers` | 8MB | USERSET | Yes, but only before first temp-table use |
| **`temp_file_limit`** | **-1 (no limit)** | **SUSET** | **No** |
| `max_stack_depth` | 2MB | SUSET | No |
| `deadlock_timeout` | 1s | SUSET | No |
| `log_min_duration_statement` | -1 | SUSET | No |
| `log_temp_files` | -1 | SUSET | No |
| `log_lock_waits` | off | SUSET | No |
| `max_connections` | ~100 | POSTMASTER | No (restart only) |
| `superuser_reserved_connections` | 3 | POSTMASTER | No |
| `reserved_connections` (PG16+) | 0 | POSTMASTER | No |
| `max_locks_per_transaction` | 64 | POSTMASTER | No |

**The headline: every timeout and every memory knob a student would want to escape is USERSET.**
`temp_file_limit` is the only genuinely effective, non-defeatable resource brake in the list.

### 2.3 Timeouts

- `statement_timeout`: *"Abort any statement that takes more than the specified amount of time … **A
  value of zero (the default) disables the timeout.**"* Doc also notes *"Setting `statement_timeout`
  in `postgresql.conf` is not recommended because it would affect all sessions."*
  — [19.11.1](https://www.postgresql.org/docs/17/runtime-config-client.html#GUC-STATEMENT-TIMEOUT)
- Since PG13 the timeout is applied **per statement** within a multi-statement simple-query message:
  *"(PostgreSQL versions before 13 usually treated the timeout as applying to the whole query
  string.)"* Relevant to DataGrip scripts. Same page.
- `idle_in_transaction_session_timeout`: terminates sessions idle *"within an open transaction"*.
  Rationale given by the docs: idle transactions hold locks, and *"an open transaction prevents
  vacuuming away recently-dead tuples … so remaining idle for a long time can contribute to table
  bloat."* Default 0. — [19.11.1](https://www.postgresql.org/docs/17/runtime-config-client.html#GUC-IDLE-IN-TRANSACTION-SESSION-TIMEOUT)
  **This is the important one for DataGrip**, whose manual-commit modes leave transactions open.
- `idle_session_timeout` (**PG14+**, *"Add server parameter `idle_session_timeout` to close idle
  sessions (Li Japin)"*) terminates idle sessions **not** in a transaction. Default 0. The doc
  explicitly endorses the per-role pattern: *"It may be helpful to enable this timeout only for
  interactive sessions, **perhaps by applying it only to particular users**."* It also warns: *"Be
  wary of enforcing this timeout on connections made through connection-pooling software or other
  middleware."* — [PG14 release notes](https://www.postgresql.org/docs/release/14.0/),
  [19.11.1](https://www.postgresql.org/docs/17/runtime-config-client.html#GUC-IDLE-SESSION-TIMEOUT)
- `transaction_timeout` (**PG17+, confirmed absent from PG16**): *"The limit applies both to explicit
  transactions (started with `BEGIN`) and to an implicitly started transaction corresponding to a
  single statement."* Interaction rule: *"If `transaction_timeout` is shorter or equal to
  `idle_in_transaction_session_timeout` or `statement_timeout` then the longer timeout is ignored."*
  — [PG17 release notes](https://www.postgresql.org/docs/release/17.0/),
  [19.11.1](https://www.postgresql.org/docs/17/runtime-config-client.html#GUC-TRANSACTION-TIMEOUT)
- `lock_timeout`: default 0; *"if `statement_timeout` is nonzero, it is rather pointless to set
  `lock_timeout` to the same or larger value, since the statement timeout would always trigger
  first."* — [19.11.1](https://www.postgresql.org/docs/17/runtime-config-client.html#GUC-LOCK-TIMEOUT)
- `deadlock_timeout` (SUSET, default 1s) is a **detection-latency** knob, not a resource limit.
  — [19.13](https://www.postgresql.org/docs/17/runtime-config-locks.html#GUC-DEADLOCK-TIMEOUT)

### 2.4 Memory, connections, locks

- `work_mem` default `4MB`, and the decision-relevant sentence: *"a complex query might perform several
  sort and hash operations at the same time, **with each operation generally being allowed to use as
  much memory as this value specifies** … Also, several running sessions could be doing such operations
  concurrently. **Therefore, the total memory used could be many times the value of `work_mem`**."*
  — [19.4.1](https://www.postgresql.org/docs/17/runtime-config-resource.html#GUC-WORK-MEM)
  ⇒ `work_mem` is **not** a per-session or per-query cap. With hundreds of sessions this is the
  primary OOM risk, and it is USERSET.
- `hash_mem_multiplier` default **2.0** ⇒ a hash node's effective budget defaults to 2×`work_mem`;
  a student can raise both. Same page.
- `maintenance_work_mem` default `64MB`, **USERSET** — a student running `CREATE INDEX` can raise it.
  Autovacuum multiplies it: *"up to `autovacuum_max_workers` times this memory may be allocated"*
  (bounded separately by `autovacuum_work_mem`, SIGHUP). Same page.
- `temp_buffers` default `8MB` per session; *"can be changed within individual sessions, **but only
  before the first use of temporary tables within the session**."* Same page.
- **There is no per-backend total memory cap in core PostgreSQL.** (Negative established by the
  complete subsection list of [19.4 Resource Consumption](https://www.postgresql.org/docs/17/runtime-config-resource.html):
  Memory / Disk / Kernel Resource Usage / Cost-based Vacuum Delay / Background Writer / Asynchronous
  Behavior — no per-role or per-backend RSS limit exists.)
- Per-role `CONNECTION LIMIT`: *"-1 (the default) means no limit … only normal connections are
  counted."* Caveats, verbatim: *"The `CONNECTION LIMIT` option is **only enforced approximately**; if
  two new sessions start at about the same time when just one connection 'slot' remains for the role,
  it is possible that both will fail. Also, **the limit is never enforced for superusers**."*
  — [CREATE ROLE](https://www.postgresql.org/docs/17/sql-createrole.html)
- Per-database `CONNECTION LIMIT`: same "-1 = no limit" and same approximate-enforcement / superuser
  caveats. — [CREATE DATABASE](https://www.postgresql.org/docs/17/sql-createdatabase.html)
  Source-level: `if (dbform->datconnlimit >= 0 && AmRegularBackendProcess() && !am_superuser && CountDBConnections(MyDatabaseId) > dbform->datconnlimit)` → `"too many connections for database \"%s\""`
  — [`postinit.c` @ REL_17_STABLE](https://raw.githubusercontent.com/postgres/postgres/REL_17_STABLE/src/backend/utils/init/postinit.c)
- ⚠️ **A student who owns their database can raise its own `CONNECTION LIMIT`**: *"Only the database
  owner or a superuser can change these settings."*
  — [ALTER DATABASE](https://www.postgresql.org/docs/17/sql-alterdatabase.html)
  The per-**role** limit is a role attribute and is *not* self-alterable (see §2.5).
- `max_locks_per_transaction`: *"The **shared** lock table has space for `max_locks_per_transaction`
  objects … **per server process or prepared transaction** … This parameter limits the **average**
  number of object locks used by each transaction; individual transactions can lock more objects as
  long as the locks of all transactions fit in the lock table."* Default 64, POSTMASTER.
  — [19.13](https://www.postgresql.org/docs/17/runtime-config-locks.html#GUC-MAX-LOCKS-PER-TRANSACTION)
  ⇒ **one shared pool with only an average per-backend budget: one student locking thousands of
  objects can exhaust the lock table for everyone.** The resulting `out of shared memory` /
  `HINT: You might need to increase max_locks_per_transaction` text appears on the mailing lists
  ([pgsql-general, Tom Lane](https://www.postgresql.org/message-id/11069.1174966638@sss.pgh.pa.us)),
  not in the reference docs.
- `max_locks_per_transaction` **cannot be varied per role or per database at all** (POSTMASTER):
  *"some settings cannot be changed after server start, and so cannot be set with these commands."*
  — [Parameter Interaction via SQL](https://www.postgresql.org/docs/17/config-setting.html)

### 2.5 Precedence, and the three escape hatches

- The explicit precedence sentence: *"**If there is a conflict, database-role-specific settings
  override role-specific ones, which in turn override database-specific ones.**"*
  — [ALTER ROLE, Notes](https://www.postgresql.org/docs/17/sql-alterrole.html)
- Full chain: `ALTER ROLE ... IN DATABASE ... SET` > `ALTER ROLE ... SET` > `ALTER DATABASE ... SET` >
  `postgresql.conf` / `ALTER SYSTEM` — **and session-level `SET` beats all of them for USERSET params.**
- Role/database defaults apply *"only when starting a fresh database session"* and *"constitute
  defaults for the rest of the session"*.
  — [config-setting.html](https://www.postgresql.org/docs/17/config-setting.html)
  Also: *"Role-specific variable settings take effect only at login; `SET ROLE` and
  `SET SESSION AUTHORIZATION` do not process role-specific variable settings."*
  — [ALTER ROLE](https://www.postgresql.org/docs/17/sql-alterrole.html)
- **Escape hatch 1 — students can rewrite their own persistent defaults**: *"**Ordinary roles can only
  set defaults for themselves.** Certain configuration variables cannot be set this way, or can only be
  set if a superuser issues the command."* ⇒ `ALTER ROLE me SET statement_timeout = 0` run by the
  student is permitted for USERSET params. — [ALTER ROLE](https://www.postgresql.org/docs/17/sql-alterrole.html)
- **Escape hatch 2 — connection-string `options`**: libpq *"Specifies command-line options to send to
  the server at connection start. For example, setting this to `-c geqo=off`…"* Any USERSET GUC can be
  pre-set from DataGrip/psql without issuing a `SET`.
  — [libpq](https://www.postgresql.org/docs/17/libpq-connect.html#LIBPQ-CONNECT-OPTIONS)
- **Escape hatch 3 — `UPDATE pg_settings`**: *"Using `UPDATE` on this view, specifically updating the
  `setting` column, **is the equivalent of issuing `SET` commands**."* Blocking the literal `SET`
  keyword in a proxy therefore does not block the change.
  — [config-setting.html](https://www.postgresql.org/docs/17/config-setting.html)

### 2.6 Can anything be made to stick against a non-superuser?

- **For SUSET parameters: yes, by default.** A non-superuser cannot `SET` them, cannot
  `ALTER ROLE ... SET` them, and cannot pass them via `options`. That covers `temp_file_limit`,
  `max_stack_depth`, `deadlock_timeout`, `log_min_duration_statement`, `log_temp_files`,
  `log_lock_waits`. — [pg_settings](https://www.postgresql.org/docs/17/view-pg-settings.html)
- **For USERSET parameters: no. There is no documented mechanism to demote a USERSET GUC to
  superuser-only.** `GRANT ... ON PARAMETER` only *adds* capability: *"`SET` — Allows a server
  configuration parameter to be set to a new value within the current session. **(While this privilege
  can be granted on any parameter, it is meaningless except for parameters that would normally require
  superuser privilege to set.)**"* — [5.8 Privileges](https://www.postgresql.org/docs/17/ddl-priv.html#DDL-PRIV-SET)
  ⇒ `GRANT SET ON PARAMETER` (PG15+) **hurts, never helps** here: granting `SET` on `temp_file_limit`
  to a student hands them the ability to disable their own disk brake.
- `pg_parameter_acl` is cluster-shared — *"there is only one copy of `pg_parameter_acl` per cluster"* —
  so any such grant is cluster-wide, not scopable to one teaching database.
  — [pg_parameter_acl](https://www.postgresql.org/docs/17/catalog-pg-parameter-acl.html)
- Never grant `ALTER SYSTEM` on a parameter — it writes cluster-wide defaults.
  — [5.8 Privileges](https://www.postgresql.org/docs/17/ddl-priv.html#DDL-PRIV-ALTER-SYSTEM)

### 2.7 `temp_file_limit` — the one real brake

- Verbatim: *"Specifies the maximum amount of disk space that **a process** can use for temporary
  files, such as sort and hash temporary files, or the storage file for a held cursor. **A transaction
  attempting to exceed this limit will be canceled.** … `-1` (the default) means no limit. **Only
  superusers and users with the appropriate `SET` privilege can change this setting.**"*
  — [19.4.2](https://www.postgresql.org/docs/17/runtime-config-resource.html#GUC-TEMP-FILE-LIMIT)
- **The loophole, verbatim**: *"It should be noted that **disk space used for explicit temporary
  tables**, as opposed to temporary files used behind-the-scenes in query execution, **does *not* count
  against this limit**."* ⇒ `CREATE TEMP TABLE huge AS SELECT ...` is outside it. So is any permanent
  table. Same page.
- Same SUSET wording confirmed present in **PG15** — not a PG17 novelty.
  — [PG15 §20.4.2](https://www.postgresql.org/docs/15/runtime-config-resource.html)
- Enforcement is per backend process; on exceed the server raises `ERROR` with
  `"temporary file size exceeds temp_file_limit (%dkB)"`.
  — [`fd.c` @ REL_17_STABLE](https://raw.githubusercontent.com/postgres/postgres/REL_17_STABLE/src/backend/storage/file/fd.c)
- *Inference (not doc-stated)*: because the limit is per-*process*, a parallel query with N workers can
  consume roughly (N+1) × `temp_file_limit` in aggregate.
- `temp_tablespaces` is user-settable but bounded: *"specifying a nonexistent tablespace is an error,
  **as is specifying a tablespace for which the user does not have `CREATE` privilege**."*
  — [19.11.1](https://www.postgresql.org/docs/17/runtime-config-client.html#GUC-TEMP-TABLESPACES)

### 2.8 Recursive CTE runaway

- **PostgreSQL does not detect infinite recursion**: *"it is important to be sure that the recursive
  part of the query will eventually return no tuples, **or else the query will loop indefinitely**."*
  — [7.8.2 WITH Queries](https://www.postgresql.org/docs/17/queries-with.html)
- The documented mitigation is a client-side testing trick, explicitly not an enforcement mechanism:
  *"place a `LIMIT` in the parent query … **Using this trick in production is not recommended** …
  Also, it usually won't work if you make the outer query sort the recursive query's results or join
  them to some other table."* Same page.
- **There is no `max_recursion_depth` GUC** (negative from absence across
  [19.11](https://www.postgresql.org/docs/17/runtime-config-client.html) and
  [19.4](https://www.postgresql.org/docs/17/runtime-config-resource.html)).
- *Inference*: `max_stack_depth` does **not** bound recursive-CTE depth — CTE evaluation is iterative
  (working/intermediate tuplestore), and the doc frames `max_stack_depth` around *"a runaway recursive
  **function**"*.
- What actually stops one: (a) `statement_timeout` — USERSET, student-defeatable; (b)
  `temp_file_limit` — SUSET, cancels the transaction once the tuplestore spills past the cap;
  (c) nothing else in core.

### 2.9 Disk fill — the hard gap

- **There is no per-role, per-schema, or per-database disk quota in stock PostgreSQL.** The only quota
  the docs acknowledge is the OS one: *"If your system supports per-user disk quotas, then the database
  will naturally be subject to whatever quota is placed on the user the server runs as."* — that is a
  quota on the `postgres` OS user, i.e. **cluster-wide, not per student**.
  — [27.6.2 Disk Full Failure](https://www.postgresql.org/docs/current/diskusage.html)
- Community confirmation of the gap (2018 -hackers "Proposal for disk quota feature"): *"In a
  multi-tenant environment, there is a requirement to limit the disk quota that database/schema/table
  can be written or a user can consume … **other databases such as Oracle, Teradata, DB2 have already
  supported disk quota feature**."*
  — [pgsql-hackers](https://www.postgresql.org/message-id/CAB0yre=w_E2c=N6oz1t=hxtC85r0QrEBeFFrxJZw1Jw7bp2uWg@mail.gmail.com)
  No such feature appears in PG15/16/17 release notes.
- **Blast radius when the disk fills**: *"A filled data disk will not result in data corruption, but it
  might prevent useful activity from occurring. **If the disk holding the WAL files grows full, database
  server panic and consequent shutdown might occur.**"* ⇒ one student's runaway `INSERT ... SELECT` is a
  **cluster-wide outage risk**, not a per-student failure. Same page.
- The docs' own remedy is structural, not preventive: *"you can move some of the database files to
  other file systems by making use of tablespaces"*, plus *"Some file systems perform badly when they
  are almost full, so do not wait until the disk is completely full to take action."* Same page.
- **Tablespaces on separate filesystems are the only structural bound in core** — they turn "fills the
  cluster" into "fills one filesystem". The docs describe **no size limit within** a tablespace.
  — [22.6 Tablespaces](https://www.postgresql.org/docs/17/manage-ag-tablespaces.html)
- Third-party, non-core: the `diskquota` extension (background worker, quotas per schema and per role
  within a database), introduced on -hackers precisely because core lacks the feature.
  — [pgsql-hackers](https://www.postgresql.org/message-id/CAB0yre=RP_ho6Bq4cV23ELKxRcfhV2Yqrb1zHp0RfUPEWCnBRw@mail.gmail.com)
  *Its current maturity / PG17 support is unverified.*

### 2.10 CPU and I/O

- **No per-role CPU limit and no general per-session I/O throttling exists in core PostgreSQL.**
  (Negative from the complete subsection list of
  [19.4 Resource Consumption](https://www.postgresql.org/docs/17/runtime-config-resource.html).)
- The one I/O-throttling facility is vacuum-scoped: `vacuum_cost_delay`, *"The default value is zero,
  which disables the cost-based vacuum delay feature"* with `vacuum_cost_limit` default 200.
  — [19.4.4](https://www.postgresql.org/docs/17/runtime-config-resource.html#GUC-VACUUM-COST-DELAY)
- CPU/IO containment is therefore an OS-level (cgroups/ulimit) concern **outside** PostgreSQL — and
  since all backends are children of one `postgres` OS user, cgroup limits apply to the cluster as a
  whole, not per student. *(The per-student impossibility is inference from the process model.)*
- The only in-core "stop this session" lever is manual: `pg_signal_backend` — *"Signal another backend
  to cancel a query or terminate its session."* Lets a non-superuser ops role kill runaway sessions.
  — [21.5 Predefined Roles](https://www.postgresql.org/docs/17/predefined-roles.html)

### 2.11 Autovacuum, bloat, wraparound

- **A student who owns a table can disable autovacuum on it**: `ALTER TABLE ... SET (autovacuum_enabled = false)`;
  *"You must own the table to use `ALTER TABLE`"* is the only barrier, and students own their tables by
  construction. Lock taken is only `SHARE UPDATE EXCLUSIVE`, so it is cheap and repeatable.
  — [ALTER TABLE](https://www.postgresql.org/docs/17/sql-altertable.html)
- *"If false, this table will not be autovacuumed, **except to prevent transaction ID wraparound**."*
  — [CREATE TABLE, Storage Parameters](https://www.postgresql.org/docs/17/sql-createtable.html#RELOPTION-AUTOVACUUM-ENABLED)
- Anti-wraparound is unconditional: *"autovacuum is invoked on any table that might contain unfrozen
  rows with XIDs older than … `autovacuum_freeze_max_age`. **(This will happen even if autovacuum is
  disabled.)**"* — [24.1.5](https://www.postgresql.org/docs/17/routine-vacuuming.html#VACUUM-FOR-WRAPAROUND)
- Wraparound failure is a per-database, cluster-affecting emergency:
  `ERROR: database is not accepting commands that assign new XIDs to avoid wraparound data loss in database "mydb"`,
  and in that state *"any transactions already in progress can continue, but **only read-only
  transactions can be started**. Operations that modify database records or truncate relations will
  fail."* Same page.

### 2.12 Observability (all SUSET — students cannot silence it)

- `log_min_duration_statement` (default -1, SUSET), `log_temp_files` (default -1, SUSET, logs each temp
  file's name and size when deleted), `log_lock_waits` (default off, SUSET, fires when a session waits
  longer than `deadlock_timeout`), `log_min_duration_sample` + `log_statement_sample_rate` (sampled
  variant, useful when hundreds of sessions would flood the log).
  — [19.8 Error Reporting and Logging](https://www.postgresql.org/docs/17/runtime-config-logging.html)
- **Temp usage is counted regardless of logging**: `pg_stat_database.temp_files` — *"All temporary
  files are counted, regardless of why the temporary file was created … and **regardless of the
  `log_temp_files` setting**"* — plus `temp_bytes`. Per-**database** temp accounting is free and always
  on; there is **no per-role equivalent** in that view.
  — [28.2 Monitoring](https://www.postgresql.org/docs/17/monitoring-stats.html)
- `pg_stat_activity.state` distinguishes `active` / `idle` / `idle in transaction` /
  `idle in transaction (aborted)`, and `backend_type` includes `client backend` and `parallel worker` —
  the basis for any custom watchdog. Same page.
- `pg_monitor` gives a non-superuser ops role full read access
  (`pg_read_all_settings` + `pg_read_all_stats` + `pg_stat_scan_tables`).
  — [21.5 Predefined Roles](https://www.postgresql.org/docs/17/predefined-roles.html)

### 2.13 Unverified in §2

- Parallel-worker multiplication of `temp_file_limit` — inference from *"a given PostgreSQL process"*.
- `max_stack_depth` not bounding recursive-CTE depth — inference.
- `temp_tablespaces` GUC context (USERSET) — inferred from the absent superuser sentence.
- Whether `hash_mem_multiplier`'s default changed from 1.0 in an earlier release — not verified; only
  the PG17 default of 2.0 is sourced.
- "No per-role CPU/IO limit", "no per-role/database disk quota", "no max-recursion GUC", "no way to
  demote a USERSET GUC" — negatives established by absence across the fetched chapters plus the 2018
  -hackers quota proposal, not by an explicit doc sentence denying them.
- `diskquota` extension maturity / PG17 compatibility.
- The `out of shared memory` / `max_locks_per_transaction` hint text is from a **pgsql-general mailing
  list message**, not reference documentation.
- Whether DataGrip exposes libpq `options=-c ...` in its UI (the libpq capability itself is sourced).

---

## 3. Dangerous surface

### 3.1 Trusted extensions (PG13+)

- Definition: *"if the extension is marked *trusted* in its control file, then it can be installed by
  any user who has **`CREATE` privilege on the current database**."* Otherwise: *"Loading an extension
  ordinarily requires the same privileges that would be required to create its component objects. For
  many extensions this means superuser privileges are needed."*
  — [CREATE EXTENSION](https://www.postgresql.org/docs/current/sql-createextension.html)
- **The escalation detail that matters**: *"When the user executing `CREATE EXTENSION` is not a
  superuser but is allowed to install by virtue of this parameter, then the installation or update
  script is **run as the bootstrap superuser**, not as the calling user."* The same page warns *"a
  trusted extension is extremely exposed from a security standpoint."*
  — [38.17 Packaging Related Objects into an Extension](https://www.postgresql.org/docs/current/extend-extensions.html)
- Introduced in PG13: *"Allow extensions to be specified as trusted (Tom Lane) … Such extensions can be
  installed in a database by users with database-level `CREATE` privileges, even if they are not
  superusers."* — [PG13 release notes](https://www.postgresql.org/docs/13/release-13.html)

**The actual list** (read from every `*.control` file in the source tree; **identical in
REL_15_STABLE and REL_17_STABLE** — [contrib tree](https://github.com/postgres/postgres/tree/REL_17_STABLE/contrib)):

- **Trusted (installable by a student with database `CREATE`)** — `bool_plperl`, `btree_gin`,
  `btree_gist`, `citext`, `cube`, `dict_int`, `fuzzystrmatch`, `hstore`, `intarray`, `isn`,
  `jsonb_plperl`, `lo`, `ltree`, `pg_trgm`, `pgcrypto`, `seg`, `tablefunc`, `tcn`, `tsm_system_rows`,
  `tsm_system_time`, `unaccent`, `uuid-ossp`, **`plpgsql`, `plperl`, `pltcl`**.
- **NOT trusted** — `dblink`, `postgres_fdw`, `file_fdw`, `plpython3u`, `plperlu`, `pltclu`, `xml2`,
  `sslinfo`, `pageinspect`, `pg_stat_statements`, `pg_buffercache`, `pg_prewarm`, `pg_visibility`,
  `pg_surgery`, `pg_walinspect`, `amcheck`, `bloom`, `pgrowlocks`, `pgstattuple`, `earthdistance`,
  `intagg`, the `spi` trigger modules, `hstore_plperl(u)`, `*_plpython3u`.
- `adminpack` was untrusted in PG15/16 and **removed entirely in PG17**.
  — [PG17 release notes](https://www.postgresql.org/docs/17/release-17.html)
- Note the asymmetry: **`plperl` and `pltcl` (the trusted variants) *are* student-installable**;
  the `u` variants are not.

### 3.2 Preventing extension installation

- The gating privilege is `CREATE` **on the database**: *"For databases, allows new schemas and
  publications to be created within the database, and **allows trusted extensions to be installed**
  within the database."* — [5.8 Privileges](https://www.postgresql.org/docs/current/ddl-priv.html)
- Default `PUBLIC` privileges on a database are `Tc` (TEMPORARY + CONNECT). **`CREATE` on a database is
  not granted to PUBLIC by default**, so `REVOKE CREATE ON DATABASE ... FROM PUBLIC` is a no-op unless
  someone granted it. Same page.
- *Inference (flagged)*: if each student **owns** their own database they hold the owner's `CREATE` on
  it and can therefore install any trusted extension there. Only not owning the database (or not being
  granted `CREATE`) removes that. The privilege rule is quoted; the ownership consequence is inference.
- Extension availability is driven by control files on disk: extensions are *"found via the server's
  extension control path"*, and `pg_available_extensions` *"lists the extensions that are available for
  installation"*, with `comment` = *"Comment string from the extension's control file"*.
  — [CREATE EXTENSION](https://www.postgresql.org/docs/current/sql-createextension.html),
  [pg_available_extensions](https://www.postgresql.org/docs/current/view-pg-available-extensions.html)
  ⇒ the documented determinant is **whether the contrib package is installed on the server at all**.
  Whether deleting only the `.so` (leaving `.control`) blocks installation is **unverified**.
- `extension_control_path` is a **PG18** GUC (default `'$system'`, superuser/SET-privilege only).
  — [PG18 release notes](https://www.postgresql.org/docs/18/release-18.html)

### 3.3 dblink and postgres_fdw

- postgres_fdw: *"Non-superusers may connect to foreign servers using password authentication or with
  GSSAPI delegated credentials, so specify the `password` option for user mappings belonging to
  non-superusers where password authentication is required."*
  — [postgres_fdw](https://www.postgresql.org/docs/current/postgres-fdw.html)
- `password_required 'false'`: *"**To prevent unprivileged users from exploiting the authentication
  rights of the unix user the postgres server is running as to escalate to superuser rights, only the
  superuser may set this option on a user mapping.**"* Same page.
- The loopback-to-self warning, verbatim: *"Care is required to ensure that this does not allow the
  mapped user the ability to connect as superuser to the mapped database per CVE-2007-3278 and
  CVE-2007-6601 … Keep in mind that the mapped user can potentially use any client certificates,
  `.pgpass`, `.pg_service.conf` etc. in the unix home directory of the system user the postgres server
  runs as … They can also use any trust relationship granted by authentication modes like `peer` or
  `ident` authentication."* Same page.
- dblink: *"Only superusers may use `dblink_connect` to create connections that use neither password
  authentication, SCRAM pass-through, nor GSSAPI-authentication."* The student-facing error is
  `ERROR: password is required` / `DETAIL: Non-superuser cannot connect if the server does not request a password.`
  — [dblink_connect](https://www.postgresql.org/docs/current/contrib-dblink-connect.html)
- `dblink_connect_u` bypasses that but *"is initially installed with all privileges revoked from
  `PUBLIC`, making it un-callable except by superusers"*; if granted, *"impersonation and subsequent
  escalation of privileges can occur, because the session will appear to have originated from the user
  as which the local PostgreSQL server runs."*
  — [dblink_connect_u](https://www.postgresql.org/docs/current/contrib-dblink-connect-u.html)
- **Both are untrusted extensions** (§3.1), so a student cannot install them; the risk is only if an
  admin installs them into a student-reachable database.
- Version notes: `require_auth` is a **PG16 libpq client** option; `use_scram_passthrough` for
  postgres_fdw/dblink is **PG18**, not 16/17.
  — [PG16](https://www.postgresql.org/docs/16/release-16.html) /
  [PG18 release notes](https://www.postgresql.org/docs/18/release-18.html)

### 3.4 COPY, `\copy`, large objects, server files

- Verbatim: *"`COPY` naming a file or command is only allowed to database superusers or users who are
  granted one of the roles `pg_read_server_files`, `pg_write_server_files`, or
  `pg_execute_server_program`, since it allows reading or writing any file or running a program that
  the server has privileges to access."* — [COPY](https://www.postgresql.org/docs/current/sql-copy.html)
- ⇒ **a plain student role cannot run `COPY ... TO/FROM '/path'` or `COPY ... PROGRAM`.** This closes
  itself by default; the risk is only granting those roles.
- `\copy` is **client-side and harmless to the server**: *"`\copy` invokes `COPY FROM STDIN` or
  `COPY TO STDOUT` … Thus, file accessibility and access rights depend on the client rather than the
  server."* Same page. ⇒ students can load/export their own files from DataGrip/psql without any
  server-side privilege.
- The three file roles *"bypass all database-level permission checks when accessing files directly and
  they could be used to gain superuser-level access."*
  — [21.5 Predefined Roles](https://www.postgresql.org/docs/current/predefined-roles.html)
- Server-side large objects: `lo_import`/`lo_export` *"read and write files in the server's file system
  … by default their use is restricted to superusers"*; *"A malicious user of such privileges could
  easily parlay them into becoming superuser (for example by rewriting server configuration files)."*
  Client-side LO functions *"do not require any database privileges."*
  — [33.4 Server-side Functions](https://www.postgresql.org/docs/current/lo-funcs.html)
- `pg_read_file` / `pg_read_binary_file` / `pg_ls_dir` / `pg_stat_file`: *"Only files within the
  database cluster directory and the `log_directory` can be accessed, unless the user is a superuser or
  is granted the role `pg_read_server_files`"*, and granting EXECUTE on them *"allows them the ability
  to read any file on the server that the database server process can read; these functions bypass all
  in-database privilege checks."*
  — [9.28 System Administration Functions](https://www.postgresql.org/docs/current/functions-admin.html)

### 3.5 Procedural languages

- *"One must have the PostgreSQL superuser privilege to register a new language."* *"`TRUSTED` specifies
  that the language does not grant access to data that the user would not otherwise have. If this key
  word is omitted … only users with the PostgreSQL superuser privilege can use this language to create
  new functions."* — [CREATE LANGUAGE](https://www.postgresql.org/docs/current/sql-createlanguage.html)
- *"In a default PostgreSQL installation … **the PL/pgSQL language itself is installed in all
  databases**."* Trusted: PL/pgSQL, PL/Tcl, PL/Perl. *"The languages PL/TclU, PL/PerlU, and PL/PythonU
  are designed to provide unlimited functionality and should *not* be marked trusted."*
  — [xplang-install](https://www.postgresql.org/docs/current/xplang-install.html)
- What trusted PL/Perl cannot do: *"certain Perl operations are disabled to preserve security"* — file
  handles, `require`, `use`, environment-interacting ops; *"There is no way to access internals of the
  database server process or to gain OS-level access with the permissions of the server process."*
  — [plperl-trusted](https://www.postgresql.org/docs/current/plperl-trusted.html)
- *"PL/Python is only available as an 'untrusted' language … Only superusers can create functions in
  untrusted languages such as `plpython3u`."* — [PL/Python](https://www.postgresql.org/docs/current/plpython.html)
- **The trusted-PL sandbox has been escaped in practice**: [CVE-2024-10979](https://www.postgresql.org/support/security/CVE-2024-10979/)
  — *"Incorrect control of environment variables in PostgreSQL PL/Perl allows an unprivileged database
  user to change sensitive process environment variables (e.g. `PATH`). That often suffices to enable
  arbitrary code execution, even if the attacker lacks a database server operating system user."*
  CVSS 8.8, fixed 17.1 / 16.5 / 15.9 / 14.14 / 13.17 / 12.21.
- **Unverified**: an explicit doc sentence that trusted-language `USAGE` defaults to `PUBLIC`
  (§1.2's default-privileges list does say `USAGE` on languages is granted to PUBLIC by default).

### 3.6 pg_terminate_backend / pg_cancel_backend

- Default: use *"is restricted to superusers by default but access may be granted to others using
  `GRANT`"*. The member rule, verbatim: *"This is also allowed if the calling role is a member of the
  role whose backend is being canceled or the calling role has privileges of `pg_signal_backend`,
  however **only superusers can cancel superuser backends**."*
  — [9.28 System Administration Functions](https://www.postgresql.org/docs/current/functions-admin.html)
- ⇒ **student A cannot terminate student B's backend by default** (A is not a member of B's role and
  lacks `pg_signal_backend`); a student **can** signal their own backends, since a role is a member of
  itself. Granting `pg_signal_backend` to students would let them kill each other across every database.
- `pg_signal_autovacuum_worker` is **PG18**. — [PG18 release notes](https://www.postgresql.org/docs/18/release-18.html)
- **Unverified**: which release introduced the "only superusers can cancel superuser backends" rule
  (no PG16 release-note item found — do not attribute it to PG16).

### 3.7 LISTEN / NOTIFY as a DoS vector

- Scope is the current database, and *"**Notifications are visible to all users.**"* Channel names are
  not catalog objects with ACLs — *"It is up to the database designer to define the channel names."*
  — [NOTIFY](https://www.postgresql.org/docs/current/sql-notify.html)
  ⇒ *inference*: any role may LISTEN on any channel in the same database. (Follows from the two quoted
  facts plus the absence of a channel object type in the
  [privileges table](https://www.postgresql.org/docs/current/ddl-priv.html); not a single quoted sentence.)
- **The queue-full DoS, verbatim**: *"If this queue becomes full, transactions calling `NOTIFY` will
  fail at commit. The queue is quite large (8GB in a standard installation) … **However, no cleanup can
  take place if a session executes `LISTEN` and then enters a transaction for a very long time.** Once
  the queue is half full you will see warnings in the log file pointing you to the session that is
  preventing cleanup."* — [NOTIFY](https://www.postgresql.org/docs/current/sql-notify.html)
  ⇒ one student doing `LISTEN x; BEGIN;` and walking away can, with any other notify traffic, fill an
  8 GB **disk** queue and break commits. `idle_in_transaction_session_timeout` is the counter — and it
  is USERSET (§2.2).
- `max_notify_queue_pages` default 1048576 (= 8 GB at 8 KB pages), **start-only** so it cannot be
  lowered at runtime.
  — [19.4](https://www.postgresql.org/docs/current/runtime-config-resource.html#GUC-MAX-NOTIFY-QUEUE-PAGES)

### 3.8 Advisory locks and lock-table exhaustion

- Scope: *"Once acquired at session level, an advisory lock is held until explicitly released or the
  session ends … session-level advisory lock requests do not honor transaction semantics: a lock
  acquired during a transaction that is later rolled back will still be held following the rollback."*
  Transaction-level ones release automatically.
  — [13.3.5 Advisory Locks](https://www.postgresql.org/docs/current/explicit-locking.html)
- *"**Advisory locks are local to each database.**"* — [pg_locks](https://www.postgresql.org/docs/current/view-pg-locks.html)
  (Note the same page: *"`pg_locks` provides a global view of all locks in the database cluster, not
  only those relevant to the current database."*)
- **The DoS mechanism, verbatim**: *"Both advisory locks and regular locks are stored in a shared memory
  pool whose size is defined by the configuration variables `max_locks_per_transaction` and
  `max_connections`. **Care must be taken not to exhaust this memory or the server will be unable to
  grant any locks at all.** This imposes an upper limit on the number of advisory locks grantable by the
  server, typically in the tens to hundreds of thousands."*
  — [13.3 Explicit Locking](https://www.postgresql.org/docs/current/explicit-locking.html)
  ⇒ **advisory locks require no privilege at all and are a documented path to a cluster-wide "cannot
  grant any locks" state.**
- Exhaustion error from the server source: `errmsg("out of shared memory")` with
  `errhint("You might need to increase \"%s\".", "max_locks_per_transaction")` — six call sites.
  — [`lock.c` @ REL_17_STABLE](https://github.com/postgres/postgres/blob/REL_17_STABLE/src/backend/storage/lmgr/lock.c)
- The blocking vector: `ACCESS EXCLUSIVE` *"Conflicts with locks of all modes"*, and *"So long as no
  deadlock situation is detected, **a transaction seeking either a table-level or row-level lock will
  wait indefinitely** for conflicting locks to be released."*
  — [13.3 Explicit Locking](https://www.postgresql.org/docs/current/explicit-locking.html)
  Contained by the object boundary though: `LOCK TABLE` requires privileges on the target table
  (`SELECT` → `ACCESS SHARE` only; `UPDATE`/`DELETE`/`TRUNCATE`/`MAINTAIN` → any mode) and *"is useless
  outside a transaction block."* — [LOCK](https://www.postgresql.org/docs/current/sql-lock.html)
- PG16 loosened it: *"Simplify permissions for LOCK TABLE … Now users can issue lesser lock levels if
  they already have permission for greater lock levels."* — [PG16 release notes](https://www.postgresql.org/docs/16/release-16.html)
- ⚠️ `pg_maintain` (PG17) *"allows executing `VACUUM`, `ANALYZE`, `CLUSTER`, `REFRESH MATERIALIZED
  VIEW`, `REINDEX`, and **`LOCK TABLE`** on all relations"* — granting it hands out cluster-wide
  `LOCK TABLE`. — [21.5 Predefined Roles](https://www.postgresql.org/docs/current/predefined-roles.html)

### 3.9 Other reachable surface

- **`CREATEROLE`** (post-PG16): *"does not convey the ability to create `SUPERUSER` roles, nor does it
  convey any power over `SUPERUSER` roles that already exist"*, nor `REPLICATION`, nor `BYPASSRLS`.
  — [22.2 Role Attributes](https://www.postgresql.org/docs/current/role-attributes.html)
  PG16 tightened it further (see §1.7). Role attributes are **not inherited**: *"being a member of a
  role with, e.g., `CREATEDB` will not allow the member to create new databases."*
  — [CREATE ROLE](https://www.postgresql.org/docs/current/sql-createrole.html)
- `SET ROLE`: *"The current session user must have the `SET` option for the specified role_name"*, and
  *"`SET ROLE` cannot be used within a `SECURITY DEFINER` function."*
  — [SET ROLE](https://www.postgresql.org/docs/current/sql-set-role.html)
- `pg_read_all_data` / `pg_write_all_data` were added in **PG14**.
  — [PG14 release notes](https://www.postgresql.org/docs/14/release-14.html)
  (This resolves §1.9's open item.)
- Event triggers — *"Only superusers can create event triggers."* A student cannot install one, and
  therefore cannot hook the engine's DDL.
  — [CREATE EVENT TRIGGER](https://www.postgresql.org/docs/current/sql-createeventtrigger.html)
- Tablespaces — *"Only superusers can create tablespaces, but they can assign ownership of tablespaces
  to non-superusers."*
  — [CREATE TABLESPACE](https://www.postgresql.org/docs/current/sql-createtablespace.html)
- Database cloning: `IS_TEMPLATE` true means *"this database can be cloned by any user with `CREATEDB`
  privileges; if false (the default), then only superusers or the owner of the database can clone it."*
  — [CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html)
- Unlogged tables need **no special privilege** and are *"not crash-safe: an unlogged table is
  automatically truncated after a crash or unclean shutdown"* — a way for students to write bulk data
  cheaply while still consuming disk. — [CREATE TABLE](https://www.postgresql.org/docs/current/sql-createtable.html)
- Full grantable predefined-role list, each a distinct blast radius: `pg_checkpoint`,
  `pg_create_subscription`, `pg_database_owner`, `pg_maintain`, `pg_monitor`, `pg_read_all_settings`,
  `pg_read_all_stats`, `pg_stat_scan_tables`, `pg_read_all_data`, `pg_write_all_data`,
  `pg_read_server_files`, `pg_write_server_files`, `pg_execute_server_program`,
  `pg_signal_autovacuum_worker`, `pg_signal_backend`, `pg_use_reserved_connections`.
  — [21.5 Predefined Roles](https://www.postgresql.org/docs/current/predefined-roles.html)

### 3.10 Verified CVEs — the "CREATE rights in a shared cluster" pattern

All read individually on postgresql.org/support/security. **Note the recurring precondition: "permission
to create non-temporary objects in at least one schema" — exactly what a student role has.**

| CVE | CVSS | Fixed in | Gist |
|---|---|---|---|
| [CVE-2018-1058](https://www.postgresql.org/support/security/CVE-2018-1058/) | 8.8 | 10.3 / 9.6.8 / 9.5.12 / 9.4.17 / 9.3.22 | Uncontrolled `search_path` element in pg_dump and other client applications |
| [CVE-2020-25695](https://www.postgresql.org/support/security/CVE-2020-25695/) | — | 13.1 / 12.5 / 11.10 / 10.15 | *"An attacker having permission to create non-temporary objects in at least one schema can execute arbitrary SQL functions under the identity of a superuser."* |
| [CVE-2022-1552](https://www.postgresql.org/support/security/CVE-2022-1552/) | 8.8 | 14.3 / 13.7 / 12.11 / 11.16 | Autovacuum, `REINDEX`, `CREATE INDEX`, `REFRESH MATERIALIZED VIEW`, `CLUSTER`, `pg_amcheck` unsafe when a privileged user maintains another user's objects → arbitrary SQL as superuser |
| [CVE-2022-2625](https://www.postgresql.org/support/security/CVE-2022-2625/) | 7.1 | 14.5 / 13.8 / 12.12 / 11.17 | Extension scripts replace objects not belonging to the extension |
| [CVE-2023-2454](https://www.postgresql.org/support/security/CVE-2023-2454/) | 7.2 | 15.3 / 14.8 / 13.11 / 12.15 | *"`CREATE SCHEMA ... schema_element` defeats protective search_path changes … enabled an attacker having database-level `CREATE` privilege to execute arbitrary code as the bootstrap superuser. **Database owners have that right by default.**"* |
| [CVE-2023-39417](https://www.postgresql.org/support/security/CVE-2023-39417/) | 7.5 | 15.4 / 14.9 / 13.12 / 12.16 | Extension-script `@substitutions@` SQL injection (non-bundled trusted extensions only) |
| [CVE-2023-5869](https://www.postgresql.org/support/security/CVE-2023-5869/) | — | 16.1 / 15.5 / 14.10 / 13.13 | Array overflow → arbitrary code execution; needs only an **authenticated** user, not CREATE |
| [CVE-2024-10978](https://www.postgresql.org/support/security/CVE-2024-10978/) | 4.2 | 17.1 / 16.5 / 15.9 / 14.14 | `SET ROLE` / `SET SESSION AUTHORIZATION` reset to wrong user ID |
| [CVE-2024-10979](https://www.postgresql.org/support/security/CVE-2024-10979/) | 8.8 | 17.1 / 16.5 / 15.9 / 14.14 | PL/Perl environment-variable escape → arbitrary code execution |
| [CVE-2025-12817](https://www.postgresql.org/support/security/CVE-2025-12817/) | 3.1 | 18.1 / 17.7 / 16.11 / 15.15 | `CREATE STATISTICS` missing authorization → DoS against other users |
| [CVE-2026-6472](https://www.postgresql.org/support/security/CVE-2026-6472/) | 5.4 | 18.4 / 17.10 / 16.14 / 15.18 | `CREATE TYPE` missing authorization → hijack queries resolving user-defined types via `search_path` |

Listed on the security index but **not individually verified** — do not cite without checking:
CVE-2026-6638, CVE-2026-6637, CVE-2026-6479, CVE-2026-6473, CVE-2026-2006, CVE-2026-2005, CVE-2026-2004,
CVE-2024-10976, CVE-2024-7348, CVE-2024-0985, CVE-2023-39418, CVE-2023-2455.
— [Security index](https://www.postgresql.org/support/security/)

**The operational read**: this class of CVE recurs roughly annually and its precondition is exactly the
student role's baseline. Staying on a current minor release is a load-bearing control, not hygiene.

### 3.11 Unverified in §3

- Whether deleting only contrib `.so` files (leaving `.control`) blocks `CREATE EXTENSION`.
- Which release introduced "only superusers can cancel superuser backends".
- That LISTEN channels have no privilege system (inferable from *"Notifications are visible to all
  users"* + absence of a channel object type in the privileges table, but not stated outright).
- That a student owning their database can consequently install trusted extensions there (privilege rule
  is quoted; the ownership consequence is inference).
- PG16's contrib trusted set was spot-checked (`adminpack`) rather than fully enumerated.

---

## 4. Connection scale

### 4.1 `max_connections` and friends

- *"Determines the maximum number of concurrent connections to the database server. **The default is
  typically 100 connections** … **This parameter can only be set at server start.**"* And: *"PostgreSQL
  sizes certain resources based directly on the value of `max_connections`. Increasing its value leads
  to higher allocation of those resources, **including shared memory**."*
  — [19.3 Connections and Authentication](https://www.postgresql.org/docs/current/runtime-config-connection.html)
- `superuser_reserved_connections` default **3**: when free slots fall to that level, *"new connections
  will be accepted only for superusers"*. POSTMASTER. **This is the emergency door in when students
  saturate the server.** Same page.
- `reserved_connections` (**PG16+**, default 0) reserves slots for the `pg_use_reserved_connections`
  predefined role — the documented way to guarantee slots for a **non-superuser** staff/monitoring
  role. — [PG16 release notes](https://www.postgresql.org/docs/release/16.0/),
  [21.5 Predefined Roles](https://www.postgresql.org/docs/17/predefined-roles.html)
- Slot-exhaustion ordering: between `superuser_reserved_connections` and
  `superuser_reserved_connections + reserved_connections` free slots, only superusers and
  `pg_use_reserved_connections` members get in; below that, superusers only. Same page.
- Lock tables scale with connection count: both `max_locks_per_transaction` and
  `max_pred_locks_per_transaction` allocate space *"per server process or prepared transaction"*
  (default 64 each, POSTMASTER). Raising `max_connections` multiplies both shared tables. *(The current
  docs do not print an explicit formula — this is derived from those two sentences.)*
  — [19.13 Lock Management](https://www.postgresql.org/docs/current/runtime-config-locks.html)
- Kernel resources scale too: *"PostgreSQL uses one semaphore per allowed connection
  (`max_connections`) … in sets of 16"*, and *"The PostgreSQL server uses **one process per
  connection** so you should provide for at least as many processes as allowed connections."*
  — [Kernel Resources](https://www.postgresql.org/docs/current/kernel-resources.html)

### 4.2 Cost per connection

- Process-per-connection is the documented model: *"To achieve this it **starts ('forks') a new process
  for each connection**."* — [Architectural Fundamentals](https://www.postgresql.org/docs/current/tutorial-arch.html)
- **There is no per-connection memory figure in the official docs.** Everything below is secondary and
  labelled as such.
- Secondary (Andres Freund, PostgreSQL committer): PSS-corrected measurement with 1,030 connections gave
  *"an overhead of about **7.6MiB with huge_pages=off and of 1.3MiB huge_pages=on**"*; page-table
  overhead per backend fell from 6,480 kB to 132 kB with huge pages. Naive RSS *"vastly
  over-estimat[es] memory usage"* because Linux attributes shared pages to every process.
  — [blog.anarazel.de](https://blog.anarazel.de/2020/10/07/measuring-the-memory-overhead-of-a-postgres-connection/)
- Secondary (Citus/Microsoft): *"The memory overhead of each connection is below 2 MiB"* with
  appropriate config, *"making thousands of established connections feasible **from a memory
  perspective alone**."*
  — [citusdata.com](https://www.citusdata.com/blog/2020/10/08/analyzing-connection-scalability/)
- **Secondary but the one that bites a teaching cluster — catalog/relcache growth per backend**: a fresh
  connection used ~512 kB in `CacheMemoryContext`, but after touching 100k tables it grew to **593 MB**
  in that single backend. Long-lived DataGrip sessions browsing many student schemas accumulate this.
  — [citusdata.com](https://www.citusdata.com/blog/2020/10/08/analyzing-connection-scalability/)
- Secondary (Brandur): *"Each of these 'backend' processes starts out at around **5 MB** in size, but
  may grow to be much larger depending on the data they're accessing."*
  — [brandur.org](https://brandur.org/postgres-connections)
- **Unverified / estimate**: the commonly quoted "5–15 MB RSS per idle backend" band. It is consistent
  with the two figures above but no primary source states it. Plan capacity from
  `work_mem` × concurrent nodes + `temp_buffers` + a few MB base, not from a headline number.

### 4.3 What actually degrades at high connection counts

- **PG14 fixed the biggest one**: *"**Improve the speed of computing MVCC visibility snapshots on
  systems with many CPUs and high session counts (Andres Freund)**"* — the `GetSnapshotData` rework.
  — [PG14 release notes](https://www.postgresql.org/docs/release/14.0/)
- Pre-PG14 severity (secondary, Citus/Freund): *"**50% of the CPU time is spent in
  `GetSnapshotData()`**"* with one active connection alongside 5,000 idle ones. Measured: 1 active
  connection went 33,457 TPS (0 idle) → 14,496 TPS (10,000 idle). **Mostly-idle** connections (query
  every 100 ms) were *"considerably worse"*: 33,773 → 6,049 TPS. **An exam room of GUI sessions is
  exactly the mostly-idle case.**
  — [citusdata.com](https://www.citusdata.com/blog/2020/10/08/analyzing-connection-scalability/)
- Post-PG14 measurement: 1 active + 10,000 idle went 16,034 → 33,140 TPS; 48 active + 10,000 idle went
  369,845 → 1,115,740 TPS.
  — [citusdata.com](https://www.citusdata.com/blog/2020/10/25/improving-postgres-connection-scalability-snapshots/)
  ⇒ **running PG14+ materially changes the answer here.**
- Remaining degradation mechanisms, per the wiki: disk contention/thrashing, memory pressure
  (`work_mem` per query node per connection), lock contention (spinlock spinning), context switching,
  cache-line contention, and internal structures that *"scale at O(N²) or O(N·log(N))"*.
  — [wiki: Number Of Database Connections](https://wiki.postgresql.org/wiki/Number_Of_Database_Connections)

### 4.4 Realistic ceilings

- The wiki's guidance formula, verbatim: *"for optimal throughput the number of **active** connections
  should be somewhere near **((core_count * 2) + effective_spindle_count)**"*, with *"Core count should
  not include HT threads"* and *"Effective spindle count is zero if the active data set is fully
  cached."* Core principle: *"you will often see a transaction **reach completion sooner** if you queue
  it when resources are saturated."*
  — [wiki: Number Of Database Connections](https://wiki.postgresql.org/wiki/Number_Of_Database_Connections)
- **The formula governs *active* connections, not established sessions.** Hundreds of students idling in
  DataGrip is a different quantity from hundreds simultaneously pressing Execute. That distinction is
  what makes "hundreds of sessions" survivable on PG14+ and "hundreds of concurrent queries" not.
- Secondary (EDB) benchmark on a 32-CPU / 244 GB server: optimal at **300–500** concurrent connections;
  *"After 700, performance dropped precipitously"*; beyond 1000, poor throughput and rising latency. At
  3700+ they hit `could not fork new process for connection: Resource temporarily unavailable`. Their
  tribal-knowledge ceilings: *"a few hundred," "no more than 500," "definitely no more than 1000"* —
  with the caveat *"there isn't a formulaic way to determine that number."*
  — [enterprisedb.com](https://www.enterprisedb.com/postgres-tutorials/challenges-setting-maxconnections-and-why-you-should-use-connection-pooler)
- Secondary (Percona PMM advisor): *"The recommended value is **below 300**."*
  — [percona.com](https://docs.percona.com/percona-monitoring-and-management/3/advisors/checks/configuration-pg-high-max-connections.html)
- Secondary (Percona benchmark): at 56 concurrent clients direct connections beat PgBouncer by 2.5x; at
  150+ the pooler won. **Pooler value is a function of concurrency, not connection count alone.**
  — [percona.com](https://www.percona.com/blog/scaling-postgresql-with-pgbouncer-you-may-need-a-connection-pooler-sooner-than-you-expect/)

### 4.5 PgBouncer — and why per-student roles defeat it

- Pool modes, verbatim: **session** — *"Server is released back to pool after client disconnects.
  **Default.**"*; **transaction** — *"Server is released back to pool after transaction finishes."*;
  **statement** — *"transaction pooling with a twist: Multi-statement transactions are disallowed."*
  — [pgbouncer.org/config.html](https://www.pgbouncer.org/config.html),
  [features.html](https://www.pgbouncer.org/features.html)
- **THE CRITICAL FACT**: *"**A new pool entry is made for each couple of (database, user).**"*
  — [pgbouncer.org/usage.html](https://www.pgbouncer.org/usage.html) — and `default_pool_size` is
  *"The maximum number of server connections to allow **per user/database pair**"* (default 20).
  — [config.html](https://www.pgbouncer.org/config.html)
- ⇒ **Transaction pooling only multiplexes clients *within* one (user, database) pool. With one distinct
  role per student, every student is alone in their own pool, so there is nothing to multiplex.**
  PgBouncer collapses N student sessions into N server connections (floor `min_pool_size`, ceiling
  `default_pool_size`). Per-user pooling does **not** reduce backend count in this envelope.
- The docs show the blowup themselves in the file-descriptor formula: single user per connection →
  `max_client_conn + (max pool_size * total databases * total users)`; shared database user →
  `max_client_conn + (max pool_size * total databases)`. — [config.html](https://www.pgbouncer.org/config.html)
- `min_pool_size` is **per pool**: a 200-student cohort with `min_pool_size=1` pre-opens 200 server
  connections. `reserve_pool_size` (default 0, `reserve_pool_timeout` 5.0s) is likewise per pool.
  Same page.
- `max_client_conn` default is **100** — far below "hundreds of students", so it must be raised
  explicitly. `max_db_connections` (*"per database, regardless of user"*) is the only knob that caps the
  total across all student roles; `max_user_connections` caps per user across databases. Both default 0
  = unlimited. Same page.
- **The only documented way to actually collapse pools** is `[databases] ... user=`: *"When set, all
  outgoing connections use this user, **creating a single pool per database**."* That means all students
  share one server-side role — discarding per-student GRANTs, `current_user`, and role identity at the
  database. Same page.
- What breaks in transaction mode (features.html SQL feature map): `SET`/`RESET`, `LISTEN`,
  `WITH HOLD` cursors, `PREPARE`/`DEALLOCATE`, session-lifetime temp tables, `LOAD`, and
  **session-level advisory locks** are all marked **Never**. Working in transaction mode: startup
  parameters (only `client_encoding`, `DateStyle`, `IntervalStyle`, `Timezone`,
  `standard_conforming_strings`, `application_name`), `NOTIFY`, `WITHOUT HOLD` cursors,
  `ON COMMIT DROP` temp tables, and protocol-level prepared plans (needs `max_prepared_statements` != 0).
  — [features.html](https://www.pgbouncer.org/features.html)
  Note the asymmetry: `NOTIFY` works but `LISTEN` never does.
- Prepared statements: *"**Since version 1.21.0** PgBouncer can track prepared statements in transaction
  pooling mode … To enable this feature, `max_prepared_statements` needs to be set to a non-zero
  value."* JDBC escape hatch: *"adding the `prepareThreshold=0` parameter to the connection string"* —
  relevant since DataGrip is JDBC. — [faq.html](https://www.pgbouncer.org/faq.html)
- **auth_query**: default is
  `SELECT rolname, CASE WHEN rolvaliduntil < now() THEN NULL ELSE rolpassword END FROM pg_authid WHERE rolname=$1 AND rolcanlogin`
  — it returns the **stored secret** and honours `rolvaliduntil`/`rolcanlogin`. Caveat: *"Direct access
  to `pg_authid` requires admin rights. It's preferable to use a non-superuser that calls a
  `SECURITY DEFINER` function instead."* `auth_dbname` selects where the query runs.
  — [config.html](https://www.pgbouncer.org/config.html)
- The alternative, `auth_file`/`userlist.txt`, needs **one line per role** kept in sync with `pg_authid`
  on every password change/add/drop — an operational burden at hundreds of students. Same page.
- **SCRAM caveat, verbatim and load-bearing**: *"SCRAM secrets can only be used for logging into a
  server if the client authentication **also** uses SCRAM, the PgBouncer database definition **does not
  specify a user name**, and the SCRAM secrets are **identical in PgBouncer and the PostgreSQL server
  (same salt and iterations, not merely the same password)**."* Same page.
  ⇒ `auth_query` satisfies the "identical secrets" requirement (it reads real `rolpassword`), but *"does
  not specify a user name"* **directly conflicts with the `user=` trick** that would collapse the pools.
  **You can have per-student SCRAM identity or single-pool collapse, not both.**
- PgBouncer is **single-threaded**: *"(PgBouncer is single-threaded and uses one CPU core per
  instance.)"* Scale-out is multiple instances via `so_reuseport`, which then requires `peer_id`
  peering so **query cancellation** routes correctly — otherwise a student's Cancel in DataGrip may not
  cancel their query. Same page.
- Client-side footprint is small: *"Low memory requirements (**2 kB per connection** by default)."*
  — [features.html](https://www.pgbouncer.org/features.html)
- **What session mode gives you here**: hundreds of long-lived DataGrip sessions map to hundreds of
  server backends — no reduction in backend count. What remains: a `max_client_conn` admission gate
  (turning `FATAL: sorry, too many clients` into a PgBouncer-side queue/refusal — a routing change, not
  a capacity change), per-pool/db/user caps, `server_lifetime` (3600s) / `server_idle_timeout` (600s)
  recycling, online restart *"without dropping client connections"*, and `SHOW POOLS`/`CLIENTS`/`SERVERS`
  accounting.

### 4.6 CREATE DATABASE vs connections (template locking)

- Verbatim: *"The principal limitation is that **no other sessions can be connected to the template
  database while it is being copied**. `CREATE DATABASE` will fail if any other connection exists when
  it starts; otherwise, new connections to the template database are locked out until `CREATE DATABASE`
  completes."* — [CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html)
- Exact error text from `createdb()`: `errmsg("source database \"%s\" is being accessed by other users", dbtemplate)`,
  with `errdetail_busy_db()` producing *"There are %d other sessions using the database."* The same
  helper is used by `createdb()`, `dropdb()`, `RenameDatabase()` and `movedb()`.
  — [`dbcommands.c` @ REL_17_STABLE](https://raw.githubusercontent.com/postgres/postgres/REL_17_STABLE/src/backend/commands/dbcommands.c)
- **Can you CREATE DATABASE while hundreds of sessions are connected to OTHER databases? Yes.** The
  restriction is scoped to the source/template database only. Sessions on unrelated databases are
  irrelevant to the check. — [CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html)
- **The mitigation is `template0`**: *"If `datallowconn` is false, then no new connections to that
  database will be allowed … The `template0` database is normally marked `datallowconn = false`."*
  Nothing can be connected to it, so it never blocks. Conversely, cloning a *reference* database (or
  `template1`) at class time is blocked by a single stray DataGrip data source pointed at it.
  — [22.3 Template Databases](https://www.postgresql.org/docs/current/manage-ag-templatedbs.html)
- **PG15+ `STRATEGY`**: *"If the `WAL_LOG` strategy is used, the database will be copied block by block
  and each block will be separately written to the write-ahead log … **it is the default**. The older
  `FILE_COPY` strategy … **forces the system to perform a checkpoint both before and after the creation
  of the new database**. In some situations, this may have a noticeable negative impact on overall
  system performance."* ⇒ scripting per-student creation with `FILE_COPY` means **2×N forced
  checkpoints**. `STRATEGY` does **not** relax the template-locking requirement.
  — [CREATE DATABASE](https://www.postgresql.org/docs/current/sql-createdatabase.html)
- `DROP DATABASE`: *"if anyone else is connected to the target database, this command will fail unless
  you use the `FORCE` option"*. `FORCE` (PG13+) *"Attempt to terminate all existing connections … It
  doesn't terminate if prepared transactions, active logical replication slots or subscriptions are
  present … If connections would remain, this command will fail."*
  — [DROP DATABASE](https://www.postgresql.org/docs/current/sql-dropdatabase.html)
- Neither `CREATE DATABASE` nor `DROP DATABASE` can run inside a transaction block. Both pages.

### 4.7 Reaping abandoned GUI sessions

- `authentication_timeout` default `1m`: *"**This prevents hung clients from occupying a connection
  indefinitely.**"* Bounds how long a stuck login squats a slot during a stampede.
  — [19.3](https://www.postgresql.org/docs/current/runtime-config-connection.html)
- `client_connection_check_interval` (**PG14+**, default 0 = disabled, Linux/macOS/BSD only): *"Sets the
  time interval between optional checks that the client is still connected, **while running
  queries**."* ⇒ only helps **during query execution**; a student who closes their laptop lid while
  idle is not caught by it. Same page. PG14 release note: *"Allow long-running queries to be canceled if
  the client disconnects."* — [PG14](https://www.postgresql.org/docs/release/14.0/)
- TCP keepalives (`tcp_keepalives_idle`/`_interval`/`_count`, all default 0 = OS default) and
  `tcp_user_timeout` are the lever for students who vanish on flaky WiFi without a FIN: *"For the kernel
  itself to detect lost TCP connections reliably and within a known timeframe … it may also be necessary
  to adjust the TCP keepalive settings."*
  — [19.3](https://www.postgresql.org/docs/current/runtime-config-connection.html)
- Connection-establishment cost is **not quantified anywhere in the PostgreSQL docs**. Secondary
  (Citus/Freund) frames the cost ordering as *"(TLS, latency, and Postgres costs, **in that order**)"* —
  for external GUI clients over TLS the handshake usually dominates the fork.
  — [citusdata.com](https://www.citusdata.com/blog/2020/10/08/analyzing-connection-scalability/)
  **Unverified**: the folklore "a few ms on LAN, 30–100 ms with TLS" figures.

### 4.8 DataGrip specifics

- *"For a data source, connections to the database are established in special wrappers – **sessions**.
  **Each session is a wrapper over a single connection.**"*
  — [JetBrains: Connecting to a database](https://www.jetbrains.com/help/datagrip/connecting-to-a-database.html)
- *"**Single session mode means that the data source and all query consoles use one and the same
  session.**"* — *"When disabled, each new query console creates a separate connection."*
  — [Data Sources and Drivers dialog](https://www.jetbrains.com/help/datagrip/data-sources-and-drivers-dialog.html)
  ⇒ combining those two documented facts, **a single student's data source can hold multiple concurrent
  server connections** — one per open console, plus introspection and data editors.
- *"**Run keep-alive query each**"* — *"Run a keep-alive query to keep the connection alive"*, with an
  interval in seconds. ⇒ **if students have keep-alive on, `idle_session_timeout` will not reap their
  sessions** — the keep-alive resets the idle clock. Same page.
- *"**Auto-disconnect after** — Disconnect from the database after the specified number of seconds"* —
  client-side idle reaping, per data source. Same page.
- Lazy connect: *"on startup, DataGrip does not establish connections to databases but loads the cached
  data"* ⇒ **the login stampede lands when students start working, not when they launch the IDE.**
  — [Connecting to a database](https://www.jetbrains.com/help/datagrip/connecting-to-a-database.html)
- **Unverified**: JetBrains documents **no** default number of connections per data source, nor whether
  Single session mode is on by default. The commonly repeated "DataGrip opens 2–5 connections per data
  source" is unverified — measure `pg_stat_activity` grouped by `usename`/`application_name` in a dry
  run rather than assuming a multiplier.

### 4.9 Unverified in §4

- No official doc states a per-connection memory figure; all §4.2 numbers beyond `work_mem`/
  `temp_buffers` defaults are secondary.
- "5–15 MB RSS per idle backend", the "3–5× core count" pool heuristic, and connection-setup timings —
  unverified folklore.
- JetBrains default connections-per-data-source and Single-session-mode default state.
- The `max_locks_per_transaction × max_connections` shared-memory formula is **derived** from two doc
  sentences, not printed as a formula in the current docs.
