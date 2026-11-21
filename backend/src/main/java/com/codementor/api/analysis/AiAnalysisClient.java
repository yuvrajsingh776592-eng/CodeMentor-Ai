package com.codementor.api.analysis;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AiAnalysisClient {
  private final RestClient client;
  private final String serviceToken;

  public AiAnalysisClient(
      RestClient.Builder builder,
      @Value("${app.ai-service.url}") String baseUrl,
      @Value("${app.ai-service.token:}") String serviceToken) {
    this.client = builder.baseUrl(baseUrl).build();
    this.serviceToken = serviceToken;
  }

  public AnalyzeDtos.AnalysisResponse analyze(AnalyzeDtos.AnalyzeRequest request) {
    try {
      var spec =
          client
              .post()
              .uri("/analyze")
              .contentType(MediaType.APPLICATION_JSON)
              .body(
                  new AiRequest(
                      request.language(), request.problem(), request.code(), request.category()));
      if (!serviceToken.isBlank()) spec.header("X-Service-Token", serviceToken);
      return spec.retrieve().body(AnalyzeDtos.AnalysisResponse.class);
    } catch (RestClientException ex) {
      throw new ResponseStatusException(
          SERVICE_UNAVAILABLE, "Code analysis service is unavailable. Please try again shortly.");
    }
  }

  private record AiRequest(String language, String problem, String code, String category) {}
}
