# CodeMentor AI

A full-stack coding practice workspace for careful code reviews, DSA tracking, progress insights, and mock technical interviews. The product is designed to make AI feedback useful without presenting it as proof: submitted programs are never executed, and static/AI-assisted findings carry clear uncertainty labels.

## Features

- **Accounts:** registration, BCrypt password hashing, JWT-based protected API, and user-scoped data.
- **AI Code Review:** C++, Java, and Python editor with problem context, a review score, potential issues, likely complexity, improvement suggestions, and test ideas.
- **Problem tracker:** create DSA problems, organize by topic and difficulty, add notes, and update attempt status.
- **Question bank:** browse 20 source-attributed questions across all 16 requested DSA categories; search by title or problem ID, filter by topic and level, sort, open stable detail URLs, bookmark, track status and submission history, and request hints/approach/complexity/solution explanations progressively. Seed data is paraphrased and links to the official problem source.
- **Study plan:** browse 583 deduplicated items from the AlgoForge DSA sheet across 42 topics, open available resources, and keep a per-account checklist.
- **Progress analytics:** completion, attempts, topic rates, 28-day activity, current and longest streak, and focus recommendations based on recorded activity.
- **Submission history:** reopen saved analysis reports.
- **Mock interview:** choose a topic and difficulty, explain your approach, submit code for feedback, and see separate rubric dimensions.
- **Optional LLM:** add a key only to the Python service environment. Without it, the app shows explicitly labeled deterministic heuristics.

## Architecture

```text
React + TypeScript + Monaco
             │ REST + JWT
             ▼
Spring Boot API ───── PostgreSQL
             │ internal service token
             ▼
FastAPI analysis service ───── optional LLM API
```

See [architecture](docs/architecture.md), [API](docs/api.md), [database](docs/database.md), and [deployment](docs/deployment.md) notes.

## Run with Docker Compose

Requirements: Docker Desktop or Docker Engine with Compose.

```sh
cp .env.example .env
# For real AI explanations, add your key to OPENAI_API_KEY in .env.
docker compose up --build
```

Open `http://localhost:3000`, create an account, then open **Question bank** from the sidebar. The curated catalog seeds idempotently on API startup; existing user data is retained. API health: `http://localhost:8081/api/health`. Compose keeps PostgreSQL and FastAPI private to its network. The optional LLM key is read only by FastAPI.

The checked-in Compose values are local-development defaults. Replace the database password, JWT signing key, and internal service token before exposing a deployment. Generate a strong base64 JWT secret with `openssl rand -base64 32`.

## Run services separately

- **API:** JDK 21+ and Maven 3.9+; configure PostgreSQL and environment variables, then `cd backend && mvn spring-boot:run`.
- **Analysis service:** Python 3.11+; `cd ai-service && python -m venv .venv && . .venv/bin/activate && pip install -r requirements.txt && uvicorn app.main:app --reload --port 8000`.
- **Frontend:** Node 20.19+; `cd frontend && npm ci && npm run dev`. Set `VITE_API_URL` if the API is not on `http://localhost:8080`.

## Environment variables

| Variable | Used by | Purpose |
|---|---|---|
| `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` | API | PostgreSQL connection |
| `JWT_SECRET`, `JWT_EXPIRATION_MS` | API | JWT signing and lifetime |
| `CORS_ALLOWED_ORIGINS` | API | Explicit comma-separated browser origins |
| `AI_SERVICE_URL`, `AI_SERVICE_TOKEN` | API and FastAPI | Private service connection |
| `OPENAI_API_KEY`, `OPENAI_MODEL` | FastAPI only | Optional model review |
| `ALLOWED_ORIGINS` | FastAPI | CORS for the analysis service |
| `VITE_API_URL` | Frontend build | Browser-visible API base URL, never a secret |

## Verification

```sh
# Frontend production build
cd frontend && npm ci && npm run build

# Backend compile and tests
cd backend && mvn verify

# AI service checks
cd ai-service && pip install -r requirements-dev.txt && pytest
```

## Screenshots

Screenshots will be added after a local Compose run is available. The current environment does not include Docker, so the stack has not yet been launched for screenshots.

## Roadmap

The project follows the requested sequence: setup, backend, PostgreSQL, auth, analysis, FastAPI, service integration, frontend/editor, tracker/analytics/interviews, automated tests, Docker, CI, then cloud deployment. Cloud deployment still needs a target provider and credentials; no cloud resources are created by this repository.
