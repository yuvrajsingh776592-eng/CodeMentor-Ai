# Deployment guide

Repository is container-ready but not deployed to a cloud account. Choose a provider supporting private service networking and managed PostgreSQL.

## Service placement

- Publish the frontend at the public hostname.
- Publish Spring Boot behind HTTPS at an API hostname.
- Keep PostgreSQL and FastAPI on private networks.
- Set `VITE_API_URL` to the public API hostname.

## Production secrets

Set these through the provider's secret manager, not a committed file:

- `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
- `JWT_SECRET` (generate with `openssl rand -base64 32` or stronger)
- `AI_SERVICE_TOKEN` (random, shared only by Spring and FastAPI)
- `CORS_ALLOWED_ORIGINS` (the exact HTTPS frontend origin)
- `OPENAI_API_KEY` and `OPENAI_MODEL` only when enabling LLM feedback

Use distinct secrets in each environment. Replace Compose defaults before public deployment. Configure TLS, resource limits, backups, monitoring, log retention, and edge rate limits. The services do not execute user code.

## Release steps

1. Pass the CI workflow.
2. Build versioned frontend, backend, and AI-service images.
3. Deploy PostgreSQL and allow Flyway to apply migrations.
4. Deploy the private AI service and API, then the frontend.
5. Check `/api/health`, create a test account, add a practice problem, and review harmless sample code.
6. Verify logs do not contain credentials or submitted source, then enable backups and monitoring.

A provider-specific deploy pipeline can be added when a provider and project are selected.
