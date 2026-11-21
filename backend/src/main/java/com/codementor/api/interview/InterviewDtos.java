package com.codementor.api.interview;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public final class InterviewDtos {
  private InterviewDtos() {}

  public record StartRequest(
      @NotBlank @Size(max = 60) String topic,
      @NotBlank @Pattern(regexp = "EASY|MEDIUM|HARD") String difficulty) {}

  public record AnswerRequest(
      @NotBlank @Size(max = 12000) String explanation,
      @Size(max = 30) String language,
      @Size(max = 30000) String code) {}

  public record Message(UUID id, String role, String content, Instant createdAt) {}

  public record InterviewResponse(
      UUID id,
      String topic,
      String difficulty,
      String prompt,
      String status,
      Instant startedAt,
      List<Message> messages,
      Object result) {}
}
