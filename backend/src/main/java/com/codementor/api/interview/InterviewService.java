package com.codementor.api.interview;

import static org.springframework.http.HttpStatus.*;

import com.codementor.api.user.CurrentUserService;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
public class InterviewService {
  private final InterviewRepository interviews;
  private final InterviewMessageRepository messages;
  private final CurrentUserService current;
  private final InterviewAiClient ai;
  private final ObjectMapper mapper;
  private static final Map<String, String> PROMPTS =
      Map.of(
          "Arrays",
          "Given an integer array and a target, return the indices of two distinct values that add"
              + " to the target. Explain your approach and trade-offs.",
          "Trees",
          "Given a binary tree, describe how you would determine whether it is height-balanced."
              + " Explain your traversal and complexity.",
          "Graphs",
          "Given an unweighted graph and two vertices, explain how to find the shortest path"
              + " between them.",
          "Dynamic Programming",
          "Given a list of coin denominations and a target amount, describe how to find the minimum"
              + " number of coins required.",
          "Strings",
          "Describe how you would find the longest substring without repeated characters.");

  public InterviewService(
      InterviewRepository i,
      InterviewMessageRepository m,
      CurrentUserService c,
      InterviewAiClient a,
      ObjectMapper o) {
    interviews = i;
    messages = m;
    current = c;
    ai = a;
    mapper = o;
  }

  @Transactional
  public InterviewDtos.InterviewResponse start(InterviewDtos.StartRequest r) {
    String prompt =
        PROMPTS.getOrDefault(
            r.topic(),
            "Describe an algorithm for a representative "
                + r.topic()
                + " problem. State assumptions, edge cases, and complexity.");
    var interview =
        interviews.save(new Interview(current.get(), r.topic(), r.difficulty(), prompt));
    messages.save(new InterviewMessage(interview, "INTERVIEWER", prompt));
    return view(interview, null);
  }

  @Transactional
  public InterviewDtos.InterviewResponse answer(UUID id, InterviewDtos.AnswerRequest r) {
    var interview = owned(id);
    if (interview.getStatus() != Interview.Status.ACTIVE)
      throw new ResponseStatusException(CONFLICT, "Interview has already been completed");
    messages.save(new InterviewMessage(interview, "CANDIDATE", r.explanation()));
    boolean hasCode = r.code() != null && !r.code().isBlank();
    if (!hasCode) {
      messages.save(
          new InterviewMessage(
              interview,
              "INTERVIEWER",
              "What are the time and auxiliary-space complexity, and which edge case would you test"
                  + " first? You can submit your code with your next response."));
      return view(interview, null);
    }
    var feedback =
        ai.evaluate(
            interview.getTopic(),
            interview.getDifficulty(),
            interview.getProblemPrompt(),
            r.explanation(),
            r.language() == null || r.language().isBlank() ? "python" : r.language(),
            r.code());
    var json = mapper.valueToTree(feedback);
    interview.complete(json);
    messages.save(
        new InterviewMessage(
            interview,
            "INTERVIEWER",
            "Thanks. The mock interview is complete. Your rubric-based feedback is ready."));
    return view(interview, feedback);
  }

  @Transactional(readOnly = true)
  public InterviewDtos.InterviewResponse result(UUID id) {
    var i = owned(id);
    return view(
        i,
        i.getFeedback() == null
            ? null
            : mapper.convertValue(i.getFeedback(), InterviewAiClient.InterviewFeedback.class));
  }

  private Interview owned(UUID id) {
    return interviews
        .findByIdAndUserId(id, current.id())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Interview not found"));
  }

  private InterviewDtos.InterviewResponse view(Interview i, Object result) {
    var list =
        messages.findAllByInterviewIdOrderByCreatedAt(i.getId()).stream()
            .map(
                m ->
                    new InterviewDtos.Message(
                        m.getId(), m.getRole(), m.getContent(), m.getCreatedAt()))
            .toList();
    return new InterviewDtos.InterviewResponse(
        i.getId(),
        i.getTopic(),
        i.getDifficulty(),
        i.getProblemPrompt(),
        i.getStatus().name(),
        i.getStartedAt(),
        list,
        result);
  }
}
