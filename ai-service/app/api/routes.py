import hmac
from fastapi import APIRouter, Header, HTTPException, status
from app.config import settings
from app.schemas import AnalyzeRequest, AnalysisResponse, CoachRequest, CoachResponse
from app.analysis.llm import analyze, coach_question

router = APIRouter()


@router.post("/analyze", response_model=AnalysisResponse)
async def analyze_code(request: AnalyzeRequest, x_service_token: str | None = Header(default=None)):
    expected = __import__("os").getenv("AI_SERVICE_TOKEN", "")
    if expected and not hmac.compare_digest(expected, x_service_token or ""):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid service credential")
    return await analyze(request)

from app.schemas import InterviewRequest, InterviewFeedback
from app.analysis.llm import evaluate_interview

@router.post("/interview/evaluate", response_model=InterviewFeedback)
async def evaluate(request: InterviewRequest, x_service_token: str | None = Header(default=None)):
    expected = __import__("os").getenv("AI_SERVICE_TOKEN", "")
    if expected and not hmac.compare_digest(expected, x_service_token or ""):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid service credential")
    return await evaluate_interview(request)

@router.post("/coach", response_model=CoachResponse)
async def coach(request: CoachRequest, x_service_token: str | None = Header(default=None)):
    expected = __import__("os").getenv("AI_SERVICE_TOKEN", "")
    if expected and not hmac.compare_digest(expected, x_service_token or ""):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid service credential")
    return await coach_question(request)
