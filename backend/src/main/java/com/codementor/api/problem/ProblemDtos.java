package com.codementor.api.problem;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public final class ProblemDtos {
  private ProblemDtos() {}

  public record CreateRequest(
      @NotBlank @Size(max = 200) String title,
      @NotBlank @Size(max = 60) String topic,
      @NotNull Problem.Difficulty difficulty,
      @Size(max = 80) String platform,
      @Size(max = 1000) String url,
      @Size(max = 10000) String notes) {}

  public record UpdateRequest(@NotNull Problem.Status status, @Size(max = 10000) String notes) {}

  public record ProblemResponse(
      UUID id,
      String title,
      String topic,
      Problem.Difficulty difficulty,
      String platform,
      String url,
      Problem.Status status,
      String notes,
      Instant lastAttemptedAt,
      Instant createdAt) {}
}
