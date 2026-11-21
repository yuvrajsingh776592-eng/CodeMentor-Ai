import asyncio
import httpx
from app.main import app
from app.analysis.heuristics import analyze_static
from app.schemas import AnalyzeRequest

def request(method, path, **kwargs):
    async def send():
        async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url="http://test") as client:
            return await client.request(method, path, **kwargs)
    return asyncio.run(send())


def test_health_endpoint_is_live():
    response = request("GET", "/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_analysis_discloses_static_only_review(monkeypatch):
    monkeypatch.delenv("AI_SERVICE_TOKEN", raising=False)
    response = request("POST", "/analyze", json={"language": "python", "category": "Arrays", "problem": "Find the sum of a list.", "code": "def solve(values):\n    return sum(values)"})
    assert response.status_code == 200
    report = response.json()
    assert report["analysisSource"] == "static-heuristics"
    assert "not executed" in report["disclaimer"].lower()
    assert 0 <= report["score"] <= 100


def test_schema_rejects_blank_source():
    response = request("POST", "/analyze", json={"language": "python", "category": "Arrays", "problem": "A long enough prompt.", "code": "   "})
    assert response.status_code == 422


def test_loop_heuristic_is_qualified():
    report = analyze_static(AnalyzeRequest(language="cpp", category="Arrays", problem="Find a value in a list.", code="for (int i=0; i<n; i++) { cout << i; }"))
    assert report.timeComplexity.startswith("Likely")
    assert "input-dependent" in report.timeExplanation.lower()


def test_llm_response_is_marked_and_qualified(monkeypatch):
    from types import SimpleNamespace
    import app.analysis.llm as review

    class FakeCompletions:
        async def create(self, **kwargs):
            return SimpleNamespace(choices=[SimpleNamespace(message=SimpleNamespace(content='''{"score": 80, "correctness": "Not proven.", "issues": [], "timeComplexity": "O(n)", "timeExplanation": "One pass.", "spaceComplexity": "O(1)", "spaceExplanation": "No extra collection.", "codeQuality": "Readable.", "optimizationSuggestions": [], "betterApproach": "None apparent.", "testCases": [], "disclaimer": ""}'''))])

    fake = SimpleNamespace(chat=SimpleNamespace(completions=FakeCompletions()))
    monkeypatch.setattr(review.settings, "openai_api_key", "test-only-key")
    monkeypatch.setattr(review, "AsyncOpenAI", lambda **kwargs: fake)
    request = AnalyzeRequest(language="python", category="Arrays", problem="Return the values in order.", code="def solve(values):\n    return values")
    result = asyncio.run(review.analyze(request))
    assert result.analysisSource == "llm-assisted"
    assert "not executed" in result.disclaimer.lower()


def test_coach_returns_no_solution_by_default_without_an_ai_key(monkeypatch):
    monkeypatch.delenv("AI_SERVICE_TOKEN", raising=False)
    monkeypatch.setattr("app.analysis.llm.settings.openai_api_key", None)
    response = request("POST", "/coach", json={
        "questionId": "LC-1", "title": "Two Sum", "description": "Find two distinct values.",
        "examples": "[]", "constraints": "[]", "hintIndex": 0, "approach": "Use a map.",
        "timeComplexity": "O(n)", "spaceComplexity": "O(n)", "mode": "HINT",
    })
    assert response.status_code == 200
    assert response.json() == {"content": "", "aiGenerated": False}


def test_coach_requires_explicit_solution_content_for_solution_mode(monkeypatch):
    monkeypatch.delenv("AI_SERVICE_TOKEN", raising=False)
    monkeypatch.setattr("app.analysis.llm.settings.openai_api_key", "")
    response = request("POST", "/coach", json={
        "questionId": "LC-1", "title": "Two Sum", "description": "Find two distinct values.",
        "examples": "[]", "constraints": "[]", "hintIndex": 0, "approach": "Use a map.",
        "timeComplexity": "O(n)", "spaceComplexity": "O(n)", "mode": "SOLUTION",
    })
    assert response.status_code == 422
