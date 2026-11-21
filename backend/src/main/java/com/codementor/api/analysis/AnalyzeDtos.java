package com.codementor.api.analysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AnalyzeDtos {
  private AnalyzeDtos() {}

  public record AnalyzeRequest(
      @NotBlank @Pattern(regexp = "cpp|java|python") String language,
      @NotBlank @Size(max = 60) String category,
      @NotBlank @Size(min = 5, max = 12000) String problem,
      @NotBlank @Size(max = 30000) String code,
      @Size(max = 200) String problemTitle,
      @Size(max = 40) String questionId) {}

  public record TestCase(String name, String input, String expected, String purpose) {}

  public record AnalysisResponse(
      int score,
      String correctness,
      List<String> issues,
      String timeComplexity,
      String timeExplanation,
      String spaceComplexity,
      String spaceExplanation,
      String codeQuality,
      List<String> optimizationSuggestions,
      String betterApproach,
      List<TestCase> testCases,
      String analysisSource,
      String disclaimer) {}

  public record SubmissionResponse(
      UUID id,
      String problemTitle,
      String language,
      String category,
      int score,
      String timeComplexity,
      String spaceComplexity,
      Instant createdAt,
      AnalysisResponse analysis) {}

  public record SubmissionSummary(
      UUID id,
      String problemTitle,
      String language,
      int score,
      String timeComplexity,
      String spaceComplexity,
      Instant createdAt) {}
}
