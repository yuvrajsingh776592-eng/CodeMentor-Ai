package com.codementor.api.interview;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InterviewAiClient {
  private final RestClient client;
  private final String token;

  public InterviewAiClient(
      RestClient.Builder b,
      @Value("${app.ai-service.url}") String url,
      @Value("${app.ai-service.token:}") String token) {
    client = b.baseUrl(url).build();
    this.token = token;
  }

  public InterviewFeedback evaluate(
      String topic,
      String difficulty,
      String prompt,
      String explanation,
      String language,
      String code) {
    try {
      var s =
          client
              .post()
              .uri("/interview/evaluate")
              .contentType(MediaType.APPLICATION_JSON)
              .body(
                  new Request(
                      topic, difficulty, prompt, explanation, language, code == null ? "" : code));
      if (!token.isBlank()) s.header("X-Service-Token", token);
      return s.retrieve().body(InterviewFeedback.class);
    } catch (RestClientException ex) {
      throw new ResponseStatusException(
          SERVICE_UNAVAILABLE, "Interview evaluation service is unavailable.");
    }
  }

  public record InterviewFeedback(
      int problemSolving,
      int dsaKnowledge,
      int complexityUnderstanding,
      int codeQuality,
      int communication,
      List<String> strengths,
      List<String> improvements,
      String summary,
      String evaluationSource,
      String disclaimer) {}

  private record Request(
      String topic,
      String difficulty,
      String prompt,
      String explanation,
      String language,
      String code) {}
}
