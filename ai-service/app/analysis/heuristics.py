import re
from app.schemas import AnalyzeRequest, AnalysisResponse, TestCase


def analyze_static(request: AnalyzeRequest) -> AnalysisResponse:
    code = request.code
    lower = code.lower()
    issues: list[str] = []
    suggestions: list[str] = []

    if not any(token in code for token in ("return", "print", "cout", "System.out", "raise")):
        issues.append("Potential issue: no obvious return or output statement was detected; verify that the required result is produced.")
    if re.search(r"\bwhile\s*\(\s*true\s*\)|\bwhile\s+True\s*:", code):
        issues.append("Potential issue: an unconditional loop was detected. Confirm that every path can exit.")
    if re.search(r"\b(?:TODO|FIXME|pass\s*$)", code, re.MULTILINE):
        issues.append("Confirmed observation: the source contains a TODO/FIXME marker or a pass-only statement.")
    if re.search(r"\b(?:==\s*null|null\s*==|\.length\s*[-+]\s*1)\b", code):
        issues.append("Potential issue: inspect null handling and boundary indexing around the detected expression.")
    if len(code.splitlines()) > 120:
        suggestions.append("Consider extracting repeated logic into small helpers; the source is longer than 120 lines.")
    if re.search(r"for\s*\([^;]*;[^;]*;[^)]*\)\s*\{[\s\S]{0,500}?for\s*\(", code) or re.search(r"for\b[\s\S]{0,300}?for\b", code):
        time = "Likely O(n²) or O(n·m)"
        time_reason = "Multiple loop constructs appear nested or close together. Static pattern matching cannot prove the exact bounds or nesting."
        suggestions.append("Check whether repeated scans can be replaced with a hash set, prefix structure, or sorted two-pointer pass.")
    elif re.search(r"\b(?:sort|sorted)\s*\(", code):
        time = "Likely O(n log n)"
        time_reason = "A sort operation appears in the source; the exact bound depends on the language and data structure."
    elif re.search(r"\b(?:for|while)\b", code):
        time = "Likely O(n) or O(n·k)"
        time_reason = "A loop is present. The input-dependent bound and any hidden work inside the loop need manual review."
    else:
        time = "Unclear from static heuristics"
        time_reason = "No common loop or sort pattern was recognized. Library calls and recursion can change the bound."

    if re.search(r"\b(?:new\s+\w+\[|\[\s*\]|append\(|push_back\(|HashMap|unordered_map|dict\s*\()", code):
        space, space_reason = "Likely O(n)", "A collection or dynamically sized structure appears to be allocated; confirm its size relative to input."
    elif re.search(r"\breturn\s+\w+\s*\(", code):
        space, space_reason = "Potentially O(depth)", "A recursive-looking return may use stack space; static analysis cannot reliably infer recursion depth here."
    else:
        space, space_reason = "Unclear; possibly O(1) auxiliary space", "No common dynamic allocation pattern was recognized. Account for library and call-stack usage."

    if not issues:
        issues.append("No obvious issue was detected by the basic static checks. This does not establish correctness.")
    tests = [
        TestCase(name="Typical case", input="Use a small valid input from the problem statement", expected="The expected result for that input", purpose="Checks the main behavior."),
        TestCase(name="Boundary case", input="Use the minimum and maximum supported sizes", expected="The result required by the constraints", purpose="Checks boundary handling."),
        TestCase(name="Empty or minimal input", input="Use empty input if the problem permits it; otherwise use the smallest valid input", expected="The specified empty/minimal result", purpose="Checks empty-state assumptions."),
    ]
    if re.search(r"\b(?:sort|sorted|binary|mid)\b", lower):
        tests.append(TestCase(name="Duplicate values", input="Include repeated values at both ends and around the target", expected="The result required for duplicates", purpose="Checks equality and boundary conditions."))

    return AnalysisResponse(
        score=max(30, min(78, 58 + (10 if "return" in code else 0) - 8 * min(2, len(issues)))),
        correctness="Not verified. Static checks can identify patterns, but they do not execute the program or prove that it solves the supplied problem.",
        issues=issues, timeComplexity=time, timeExplanation=time_reason,
        spaceComplexity=space, spaceExplanation=space_reason,
        codeQuality="Heuristic review only: inspect naming, decomposition, and input validation in context. No execution or compiler was used.",
        optimizationSuggestions=suggestions or ["Compare the approach against the input constraints and measure it with representative cases before optimizing."],
        betterApproach="A better approach cannot be recommended reliably without understanding the full requirements and constraints. Validate the algorithm against the problem statement first.",
        testCases=tests, analysisSource="static-heuristics",
        disclaimer="Static analysis only. The code was not executed, compiled, or proven correct.",
    )
