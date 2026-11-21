CREATE TABLE questions (
    id VARCHAR(40) PRIMARY KEY,
    slug VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    examples JSONB NOT NULL,
    constraints_json JSONB NOT NULL,
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    topics JSONB NOT NULL,
    source_name VARCHAR(100) NOT NULL,
    source_url VARCHAR(1000) NOT NULL,
    hints JSONB NOT NULL,
    approach TEXT NOT NULL,
    time_complexity VARCHAR(120) NOT NULL,
    space_complexity VARCHAR(120) NOT NULL,
    solution_explanation TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
CREATE INDEX idx_questions_slug ON questions(slug);

CREATE TABLE user_question_progress (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_id VARCHAR(40) NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'ATTEMPTED', 'SOLVED')),
    bookmarked BOOLEAN NOT NULL DEFAULT FALSE,
    hints_revealed INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, question_id)
);
CREATE INDEX idx_question_progress_user_status ON user_question_progress(user_id, status);

CREATE TABLE question_attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_id VARCHAR(40) NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    submission_id UUID REFERENCES submissions(id) ON DELETE SET NULL,
    outcome VARCHAR(20) NOT NULL CHECK (outcome IN ('ATTEMPTED', 'SOLVED')),
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_question_attempts_user_date ON question_attempts(user_id, attempted_at DESC);
CREATE INDEX idx_question_attempts_question ON question_attempts(user_id, question_id, attempted_at DESC);

CREATE TABLE question_coach_history (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_id VARCHAR(40) NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    hint_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, question_id, hint_index)
);

ALTER TABLE submissions ADD COLUMN question_id VARCHAR(40) REFERENCES questions(id) ON DELETE SET NULL;
CREATE INDEX idx_submissions_user_question ON submissions(user_id, question_id, created_at DESC);
