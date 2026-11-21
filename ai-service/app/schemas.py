from typing import Literal
from pydantic import BaseModel, Field, field_validator

Language = Literal["cpp", "java", "python"]


class AnalyzeRequest(BaseModel):
    language: Language
    problem: str = Field(min_length=5, max_length=12000)
    code: str = Field(min_length=1, max_length=30000)
    category: str = Field(default="Other", max_length=60)

    @field_validator("problem", "code")
    @classmethod
    def reject_blank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("must not be blank")
        return value


class TestCase(BaseModel):
    name: str
    input: str
    expected: str
    purpose: str


class AnalysisResponse(BaseModel):
    score: int = Field(ge=0, le=100)
    correctness: str
    issues: list[str]
    timeComplexity: str
    timeExplanation: str
    spaceComplexity: str
    spaceExplanation: str
    codeQuality: str
    optimizationSuggestions: list[str]
    betterApproach: str
    testCases: list[TestCase]
    analysisSource: Literal["static-heuristics", "llm-assisted", "llm-fallback"]
    disclaimer: str

class InterviewRequest(BaseModel):
    topic: str = Field(min_length=1, max_length=60)
    difficulty: Literal["EASY", "MEDIUM", "HARD"]
    prompt: str = Field(min_length=5, max_length=12000)
    explanation: str = Field(min_length=1, max_length=12000)
    language: Language = "python"
    code: str = Field(default="", max_length=30000)

class InterviewFeedback(BaseModel):
    problemSolving: int = Field(ge=0, le=100)
    dsaKnowledge: int = Field(ge=0, le=100)
    complexityUnderstanding: int = Field(ge=0, le=100)
    codeQuality: int = Field(ge=0, le=100)
    communication: int = Field(ge=0, le=100)
    strengths: list[str]
    improvements: list[str]
    summary: str
    evaluationSource: Literal["llm-assisted", "rubric-heuristics"]
    disclaimer: str

class CoachRequest(BaseModel):
    questionId: str = Field(min_length=1, max_length=40)
    title: str = Field(min_length=1, max_length=200)
    description: str = Field(min_length=5, max_length=12000)
    examples: str = Field(max_length=8000)
    constraints: str = Field(max_length=4000)
    hintIndex: int = Field(ge=0, le=5)
    approach: str | None = Field(default=None, max_length=5000)
    timeComplexity: str | None = Field(default=None, max_length=120)
    spaceComplexity: str | None = Field(default=None, max_length=120)
    solution: str | None = Field(default=None, max_length=8000)
    mode: Literal["HINT", "APPROACH", "COMPLEXITY", "SOLUTION"]

class CoachResponse(BaseModel):
    content: str
    aiGenerated: bool
