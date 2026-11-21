package com.codementor.api.question;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class QuestionCoachClient {
  private final RestClient client;
  private final String token;

  public QuestionCoachClient(RestClient.Builder builder,
      @Value("${app.ai-service.url}") String url,
      @Value("${app.ai-service.token:}") String token) {
    this.client = builder.baseUrl(url).build();
    this.token = token;
  }

  public CoachResult ask(QuestionRepository.QuestionRecord question,
      QuestionDtos.CoachMode mode, int hintIndex) {
    try {
      var request = new CoachRequest(question.id(), question.title(), question.description(),
          question.examplesJson(), question.constraintsJson(), null, hintIndex,
          mode == QuestionDtos.CoachMode.APPROACH ? question.approach() : null,
          mode == QuestionDtos.CoachMode.COMPLEXITY ? question.timeComplexity() : null,
          mode == QuestionDtos.CoachMode.COMPLEXITY ? question.spaceComplexity() : null,
          mode == QuestionDtos.CoachMode.SOLUTION ? question.solutionExplanation() : null, mode.name());
      var call = client.post().uri("/coach").contentType(MediaType.APPLICATION_JSON).body(request);
      if (!token.isBlank()) call.header("X-Service-Token", token);
      var response = call.retrieve().body(CoachResult.class);
      return response == null ? new CoachResult(null, false) : response;
    } catch (RestClientException ex) {
      return new CoachResult(null, false);
    }
  }

  public record CoachRequest(String questionId, String title, String description, String examples,
      String constraints, String hints, int hintIndex, String approach, String timeComplexity,
      String spaceComplexity, String solution, String mode) {}
  public record CoachResult(String content, boolean aiGenerated) {}
}
