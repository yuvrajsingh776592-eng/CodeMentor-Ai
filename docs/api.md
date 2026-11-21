# REST API

Base URL: `http://localhost:8080`. JSON is used for request and response bodies. Protected routes require `Authorization: Bearer <token>`. Errors use `{ "code", "message", "timestamp" }` where available.

| Method | Path | Auth | Purpose |
|---|---|---:|---|
| POST | `/api/auth/register` | No | Create an account; returns token and user |
| POST | `/api/auth/login` | No | Authenticate; returns token and user |
| POST | `/api/auth/logout` | Yes | Client-side stateless logout acknowledgement |
| GET | `/api/auth/me` | Yes | Current user profile |
| GET | `/api/users/me` | Yes | Current user profile (user resource route) |
| POST | `/api/analyze` | Yes | Request static/AI-assisted review; stores submission |
| GET | `/api/submissions` | Yes | List current user's review history |
| GET | `/api/submissions/{id}` | Yes | Fetch one owned report |
| GET | `/api/problems` | Yes | List current user's tracker |
| POST | `/api/problems` | Yes | Add a problem |
| PATCH | `/api/problems/{id}` | Yes | Update status and notes |
| GET | `/api/progress` | Yes | Aggregate attempts, topics, streak, and activity |
| GET | `/api/progress/topics` | Yes | Topic completion and success rates |
| POST | `/api/interview/start` | Yes | Start a mock interview |
| POST | `/api/interview/{id}/answer` | Yes | Continue a mock interview; code submits for evaluation |
| GET | `/api/interview/{id}/result` | Yes | Fetch messages and completed rubric report |
| GET | `/api/health` | No | API liveness/version |

## Analyze request

```json
{
  "language": "python",
  "category": "Arrays",
  "problem": "Return the indices of two distinct values that add to the target.",
  "code": "def solve(nums, target): ...",
  "problemTitle": "Two Sum"
}
```

The report includes score, correctness wording, issues, likely time/space complexity, code-quality notes, suggestions, test ideas, analysis source, and a disclaimer. A score is a rough signal, not a correctness guarantee. No endpoint executes submitted code.

## Error behavior

- `400` for invalid DTO fields
- `401` for absent/invalid authentication
- `404` when an owned resource is not found
- `409` for duplicate account registration or completed interview updates
- `429` when a per-address/user request limit is reached
- `503` when the AI service is unavailable
- `500` with a generic message for unexpected server errors
