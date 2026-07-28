# DbExam

A platform where students take database-course exams: they work out SQL tasks
against real PostgreSQL databases that the system provisions, resets, and
grades.

## Language

### People & access

**User**:
Anyone who can sign in. Has exactly one global role: admin or student.
_Avoid_: account, principal

**Student**:
A user whose role is STUDENT; takes exams.
_Avoid_: pupil, candidate

**Admin**:
A user whose role is ADMIN. Global role only grants sign-in; actual power is
scoped per exam through participation.
_Avoid_: professor, TA, staff (roles that no longer exist as concepts)

**Exam Participant**:
The enrollment of one user in one exam — either as a taker or, when flagged
admin, as that exam's administrator. The anchor for everything per-student in
an exam: held databases, progress, submissions, personal timing.
_Avoid_: enrollment, registration, attendee

### Exam structure

**Course**:
A stable catalog entry (code + name) that exams belong to. Not re-created per
semester — the exam's own dates carry time.
_Avoid_: subject, class

**Exam**:
A scheduled event of a course: a time window with enrolled participants. An
exam has no type — its character is entirely composed from its tasks and
environments.
_Avoid_: exam type (the concept was deliberately eliminated), session, term

**Task**:
A single executable work item in an exam: text the student reads plus the kind
of SQL they must produce, worth points. Tasks form a flat ordered list — there
are no subtasks.
_Avoid_: subtask, question, problem, exercise

**Check**:
One criterion by which a task is judged (result match, schema match, state
match, performance), worth part of the task's points. A task can have several;
partial credit is their sum.
_Avoid_: test case, assertion, grader rule

### Environments & databases

**Environment**:
A declarative database setup within one exam — shared or per-student, starting
state, reset behavior. Tasks point at an environment; tasks sharing one work
in the same database, which is how later tasks continue on earlier tasks'
state. Retired, never deleted.
_Avoid_: scenario, workspace, task config

**Database Template**:
A reusable, rebuildable starting state (schema + data) for databases, global
across exams. Retired, never deleted.
_Avoid_: mockup, dump, seed database

**Retire**:
Taking an environment or template out of service: it can no longer be
referenced by anything new, but it survives forever as history for whatever
already references it. The opposite of retiring is reactivating.
_Avoid_: delete, deactivate (as concept names), archive

**Managed Database**:
One physical PostgreSQL database the system created, tracked for its whole
life (state, holder, which connection descriptor it currently serves). Never
reused, never forgotten — discarded ones remain as history.
_Avoid_: instance, workspace, sandbox

**Connection Descriptor**:
The stable connection details (host, port, database name, role, password) a
participant uses for one environment — fixed for the whole exam, even across
swaps.
_Avoid_: connection string, DSN, credentials (alone)

**Pool**:
The stock of ready, unassigned managed databases kept pre-created for an
environment so a fresh one can be handed out instantly. May be empty — a
fresh database can always be cloned on the spot instead.
_Avoid_: cache, buffer

**Prep**:
The admin-triggered act of creating and assigning every enrolled
participant's databases before the exam window opens.
_Avoid_: warmup, pre-provisioning

**Claim**:
The atomic act of assigning an available pooled database to a participant.
_Avoid_: allocate, checkout

**Swap**:
Discarding a participant's ruined database and handing them a fresh one
behind their unchanged connection descriptor, taking a swap capture on the
way out.
_Avoid_: reset (that's the environment policy family), reprovision

**Swap Capture**:
The pulled submission taken from a database at the moment it is discarded,
so un-pulled work never dies with a swap. An empty capture is itself
evidence of what the database held.
_Avoid_: exit snapshot, backup

**Auto-populate**:
The engine's best-effort filling of a student-built schema with authored
data on each accepted DDL submission — per table, only ever into empty
tables, never deleting or overwriting.
_Avoid_: seeding, data generation (as a concept name)

**Populate Report**:
The recorded evidence of one auto-populate run: which tables received data,
which were skipped and why. Visible to the student and to graders.
_Avoid_: populate log, seed status

**Stage**:
A contiguous run of an environment's tasks that share one database state
basis. Stages are separated by restage points; an environment with no
restage points is one stage.
_Avoid_: phase, section, part

**Restage Point**:
An authored boundary between two stages, carrying the template whose clone
becomes the next stage's database when the stage before it is finalized.
_Avoid_: checkpoint, milestone

**Finalize**:
The participant's explicit, irreversible closing of a stage: its tasks lock
against further submissions, the database is captured, and the next stage's
database is swapped in. Exam end finalizes whatever remains open.
_Avoid_: complete, lock (as act names), submit stage

### Containment & operations

**Watchdog**:
The engine's out-of-session monitor of student databases — sizes, query
runtimes, connections — that automatically cancels queries or terminates
sessions past admin-set ceilings, since in-session limits are
student-defeatable.
_Avoid_: monitor, reaper, sentinel

**Evict**:
The operator-only act of suspending a student's database access mid-exam:
every session terminated and login blocked until explicitly reinstated.
Automation stops at session termination; eviction is always a human decision.
_Avoid_: ban, kick, lock out

**Incident**:
The recorded evidence of one containment event — which student, what breached,
what the watchdog saw and did. The trail behind every eviction decision.
_Avoid_: alert, violation

### Record & judging

**Submission**:
One answer a participant produced for a task, with the exact SQL and which
database it ran against. Append-only and sacred: databases are disposable,
submissions are the source of truth — never updated, never deleted.
_Avoid_: attempt, answer, entry

**Pulled submission**:
A submission the engine extracted from the student's database itself (schema,
procedure source) rather than received as typed text — what makes working in
external SQL tools equivalent to the UI.
_Avoid_: captured, sniffed

**Student Task**:
The per-participant state of one task: progress status, which submission
counts as the final answer, and the earned score.
_Avoid_: assignment, task instance

**Check Result**:
The recorded evidence of running one check against one submission: passed or
not, points awarded, and what the grader saw. The ground truth behind every
score and the answer to every grading dispute.
_Avoid_: grade, verdict

**Document**:
A markdown material (task sheets, instructions, diagrams) grouped in folders
that attach to exams and tasks.
_Avoid_: file, attachment
