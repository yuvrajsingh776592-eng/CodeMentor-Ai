-- Keep this table in a forward migration so databases that already applied an
-- earlier version of V1 are upgraded safely. Fresh databases create it in V1.
CREATE TABLE IF NOT EXISTS interview_messages (
    id UUID PRIMARY KEY,
    interview_id UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_interview_messages_interview_date
    ON interview_messages(interview_id, created_at);
