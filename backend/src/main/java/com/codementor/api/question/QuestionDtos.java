package com.codementor.api.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class QuestionDtos {
  private QuestionDtos() {}

  public enum Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }
  public enum Status { TODO, ATTEMPTED, SOLVED }
  public enum CoachMode { HINT, APPROACH, COMPLEXITY, SOLUTION }

  public record Example(String input, String output, String explanation) {}
  public record Progress(Status status, boolean bookmarked, int hintsRevealed) {}
  public record QuestionSummary(
      String id, String slug, String title, Difficulty difficulty, List<String> topics,
      String sourceName, String sourceUrl, Status status, boolean bookmarked) {}
  public record QuestionDetail(
      String id, String slug, String title, String description, List<Example> examples,
      List<String> constraints, Difficulty difficulty, List<String> topics,
      String sourceName, String sourceUrl, Progress progress, List<String> revealedHints, int hintCount) {}
  public record QuestionPage(List<QuestionSummary> items, int page, int size, long totalItems,
      int totalPages, boolean hasNext, boolean hasPrevious) {}
  public record ProgressSummary(long total, long todo, long attempted, long solved, long bookmarked) {}
  public record ProgressUpdate(@NotNull Status status, boolean bookmarked) {}
  public record CoachRequest(@NotNull CoachMode mode, @Min(0) @Max(5) Integer hintIndex) {}
  public record CoachResponse(CoachMode mode, String content, int hintsRevealed, boolean aiGenerated) {}
  public record Attempt(String id, String outcome, Instant attemptedAt, String submissionId,
      Integer score, String language) {}
}
