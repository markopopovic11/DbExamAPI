# Continuity is live state, partitioned into stages

What task N+1 may assume about a shared environment's database, how
auto-populate interacts with continuation, and what a mid-sequence reset
restores (#27) — building on the isolation unit (ADR 0002) and the swap
machinery (ADR 0003). Four decisions compose.

## Checks see the live database, as-is

A check runs against whatever the database actually holds at check time. The
system guarantees nothing about the state between tasks — whatever the
student has done since task N is what task N+1's checks see. Tolerating that
drift is an authoring concern: judge what the task itself must produce
(SCHEMA_MATCH, STATE_MATCH), or give exact-data checks a fresh basis (a
separate environment, or a stage — below). A recorded Check Result is
immutable evidence captured at check time; no later mutation of the database,
by student or engine, ever rewrites a sealed grade. Rejected:
engine-reconstructed "pristine task-N-final" state (requires model-solution
replay and assumes student schemas converge to canonical shape — they don't
under DDL tasks) and per-boundary snapshots (storage and provisioning cost,
and the graded state diverges from what the student sees in their SQL tool).

## Auto-populate is authored, best-effort, fill-empty-only

A populate dataset is authored data attached to a DDL task; no dataset means
no populate (environments whose tasks need data from the start get it from
the template, like any other starting state). Populate fires on **every
accepted submission** of the task — there is no fully-correct gate — and
applies **per table, best-effort**: tables that exist and accept the data get
it, missing or broken tables are skipped. A student with an 80%-right schema
gets 80% of the data, so a partial-credit DDL cascades proportionately into
later query tasks instead of totally (the fully-correct gate was rejected for
exactly that all-or-nothing cascade). Populate only ever inserts into
**empty** dataset tables — it never deletes or overwrites — so re-fires are
harmless, student-created rows are never eaten, and data converges table by
table as the student fixes their schema. Every run records a **populate
report** (which tables received data, which were skipped and why), visible to
the student — so they know why their SELECT returns nothing — and to graders
as dispute evidence, in the same spirit as the empty swap capture.

## Stages and restage points

An environment's task sequence may be partitioned into **stages** by authored
**restage points**, each carrying a database template. The student
**finalizes** a stage as one explicit, irreversible act: every task in the
stage locks against further submissions, pull-on-discard captures the
database (sealing its DB_STATE submissions), and the ADR 0003 swap rotates in
a clone of the next stage's template behind the unchanged connection
descriptor. Exam end auto-finalizes whatever stages remain open, so capture
always happens. Finalizing with unanswered tasks is legal (confirmed in the
UI — the student's choice, the student's points).

This exists for the stacked exam shape — "build the schema, then write DML
against the *correct* schema" — and dissolves its copy-leak by construction.
A canonical-basis database is a published copy of the canonical DDL: any
student who can connect can have their SQL tool regenerate its full DDL, so
no permission scheme can hide it. The only fix is temporal: canonical state
becomes reachable only after the DDL answer is sealed, and with restage the
two never coexist. The stage — not the single gating task — is the unit of
finalization, because a restage destroys the whole stage's state: leaving
earlier same-stage tasks formally open would let them be re-checked against
the wrong stage's database. Rejected: a gated second environment (two
connection descriptors, per-student CONNECT choreography, and the same
finalization concept anyway) and authoring-guidance-only (the stacked shape
would simply be unsupported). Guidance survives as the zero-mechanism
default: exams that don't need stacking should give the DML section a
different schema domain than the DDL task asks for.

## Reset restores the current stage

A student-triggered swap clones the **current stage's** template (stage one's
basis is the environment's base template). The student redoes only their own
un-finalized, current-stage work — typed submissions remain visible in the UI
to re-run. Finalized stages are sealed as submissions and captures; they are
never replayed and cannot be revisited. Swapping back to the base template
regardless of stage was rejected as incoherent: it would strand the student
on a database from a chapter whose tasks are locked. Restage points are
**orthogonal to ResetPolicy** — restage is authored exam structure, not a
student rescue, so an exam that forbids self-service resets (NONE) may still
be staged.

## Consequences

- Finalization is a new, irreversible student act and a new pull point;
  the pulled-submission cadence outside swaps (#31) should treat stage
  finalization (explicit or exam-end) as one of its fixed capture edges.
- ADR 0003's pool is sized per environment; with stages the operative unit
  becomes the stage's template, and restages cluster in time as a cohort
  moves through the exam — pool targets are set per (environment × stage).
- After an in-stage reset, a CHECKED DDL task stays sealed while its tables
  are gone; re-submitting the DDL re-fires populate, which refills the now
  empty tables. No special case.
- The `ResetPolicy` enum predates ADR 0003/0004: `RECREATE` (drop and
  rebuild) contradicts the zero-mid-exam-drops rule, and the swap-vs-clone
  distinction collapsed into one claim-with-fallback path. Implementation
  catch-up, tracked for the build phase, not a new decision.
- Authoring surfaces to build: attaching a populate dataset to a DDL task,
  declaring restage points with their templates, and the populate report as
  a first-class record beside Check Results.
