# Database schema

PostgreSQL schema is managed by `backend/src/main/resources/db/migration/V1__create_core_tables.sql`.

- **users**: account identity, unique email, BCrypt password hash, timestamps.
- **problems**: user-owned DSA tracker rows with topic, difficulty, status, notes, and last attempt time.
- **problem_attempts**: append-only attempt outcomes used for topic success rates and streaks.
- **submissions**: user-owned source snapshots and JSONB analysis reports.
- **interviews**: user-owned topic/difficulty/problem session and final rubric JSONB.
- **interview_messages**: ordered interviewer/candidate conversation entries.

Every private table references `users`; interview messages cascade with the interview. Indexes support user-scoped listing, topic/status filtering, and chronological history. User deletion cascades to that user's learning records.

A topic success rate is `solved / attempted * 100`; topic completion is `solved / tracked * 100`. Streaks use attempt event dates in UTC. Empty topics do not get ranked as weak or strong. The current focus recommendation therefore appears only after the user has attempted at least one tracked problem.
