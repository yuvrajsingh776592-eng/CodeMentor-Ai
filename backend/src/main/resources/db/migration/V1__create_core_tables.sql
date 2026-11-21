CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE problems (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    topic VARCHAR(60) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    platform VARCHAR(80),
    url VARCHAR(1000),
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    notes TEXT,
    last_attempted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_problems_user_status ON problems(user_id, status);
CREATE INDEX idx_problems_user_topic ON problems(user_id, topic);

CREATE TABLE submissions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id UUID REFERENCES problems(id) ON DELETE SET NULL,
    problem_title VARCHAR(200) NOT NULL,
    language VARCHAR(30) NOT NULL,
    problem_category VARCHAR(60) NOT NULL,
    problem_statement TEXT NOT NULL,
    source_code TEXT NOT NULL,
    score INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
    time_complexity VARCHAR(100) NOT NULL,
    space_complexity VARCHAR(100) NOT NULL,
    analysis_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_submissions_user_created ON submissions(user_id, created_at DESC);

CREATE TABLE problem_attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    outcome VARCHAR(20) NOT NULL,
    attempted_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_attempts_user_date ON problem_attempts(user_id, attempted_at DESC);

CREATE TABLE interviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic VARCHAR(60) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    problem_prompt TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    feedback_json JSONB,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);
CREATE INDEX idx_interviews_user_started ON interviews(user_id, started_at DESC);

CREATE TABLE interview_messages (
    id UUID PRIMARY KEY,
    interview_id UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_interview_messages_interview_date ON interview_messages(interview_id, created_at);
