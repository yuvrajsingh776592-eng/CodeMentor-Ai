# Architecture

```text
Browser (React, Vite, TypeScript)
   │ JWT-authenticated REST calls
   ▼
Spring Boot API ───────── PostgreSQL
   │ private service token
   ▼
FastAPI analysis service ─── optional LLM provider
```

## Responsibilities

- **Frontend:** user interface, form validation, Monaco editor, charts, and short-lived browser-tab token storage. The browser never receives the LLM key.
- **Spring Boot:** registration/login, JWT validation, per-user authorization, input validation, persistence, and orchestration of the AI service.
- **PostgreSQL:** user accounts, problems, attempts, submissions, interviews, and interview messages. Flyway owns schema migrations; Hibernate validates mappings at startup.
- **FastAPI:** bounded request schemas, deterministic static heuristics, cautious prompt construction, and optional LLM explanations. User code is never executed.

## Request path: code review

1. The signed-in browser sends code and problem context to `POST /api/analyze` with a bearer token.
2. Spring validates the request and resolves the authenticated user.
3. Spring forwards the request over the Compose-only network to FastAPI with a service token.
4. FastAPI returns a typed report. Without an LLM key, the basic heuristic report is returned and labeled as such.
5. Spring stores the report and source text under the current user's account and returns a DTO.

## Security notes

Passwords use BCrypt. JWTs are short lived and held in `sessionStorage`, then removed on sign-out. CORS uses an explicit origin list. Code is size-limited and not run by either service. LLM credentials live only in the AI service environment. Local Compose defaults are for development and must be replaced for a public deployment. Production should use TLS, managed secret storage, token revocation, distributed rate limiting at the edge (the API currently uses an in-process per-instance guard), backups, and monitoring.

## Local ports

- Frontend: `http://localhost:3000`
- Spring API: `http://localhost:8080`
- FastAPI: Compose-internal port `8000` (not published to the host)
- PostgreSQL: Compose-internal port `5432` (not published to the host)
