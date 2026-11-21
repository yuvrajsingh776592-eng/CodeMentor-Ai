package com.codementor.api.submission;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.codementor.api.analysis.*;
import com.codementor.api.user.CurrentUserService;
import com.codementor.api.question.QuestionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import tools.jackson.databind.ObjectMapper;

@Service
public class SubmissionService {
  private final AiAnalysisClient ai;
  private final SubmissionRepository submissions;
  private final CurrentUserService current;
  private final ObjectMapper mapper;
  private final QuestionRepository questions;

  public SubmissionService(
      AiAnalysisClient ai,
      SubmissionRepository submissions,
      CurrentUserService current,
      ObjectMapper mapper,
      QuestionRepository questions) {
    this.ai = ai;
    this.submissions = submissions;
    this.current = current;
    this.mapper = mapper;
    this.questions = questions;
  }

  @Transactional
  public AnalyzeDtos.SubmissionResponse analyze(AnalyzeDtos.AnalyzeRequest r) {
    if (r.questionId() != null && questions.findById(r.questionId()).isEmpty())
      throw new ResponseStatusException(BAD_REQUEST, "Question does not exist");
    var analysis = ai.analyze(r);
    var entity =
        submissions.saveAndFlush(
            new Submission(
                current.get(),
                r.problemTitle() == null || r.problemTitle().isBlank()
                    ? r.category() + " practice"
                    : r.problemTitle().trim(),
                r.language(),
                r.category(),
                r.questionId(),
                r.problem(),
                r.code(),
                analysis.score(),
                analysis.timeComplexity(),
                analysis.spaceComplexity(),
                mapper.valueToTree(analysis)));
    if (r.questionId() != null)
      questions.addAttempt(current.id(), r.questionId(), "ATTEMPTED", entity.getId());
    return toResponse(entity, analysis);
  }

  @Transactional(readOnly = true)
  public java.util.List<AnalyzeDtos.SubmissionSummary> list() {
    return submissions.findAllByUserIdOrderByCreatedAtDesc(current.id()).stream()
        .map(
            s ->
                new AnalyzeDtos.SubmissionSummary(
                    s.getId(),
                    s.getProblemTitle(),
                    s.getLanguage(),
                    s.getScore(),
                    s.getTimeComplexity(),
                    s.getSpaceComplexity(),
                    s.getCreatedAt()))
        .toList();
  }

  @Transactional(readOnly = true)
  public AnalyzeDtos.SubmissionResponse get(UUID id) {
    var s =
        submissions
            .findByIdAndUserId(id, current.id())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Submission not found"));
    return toResponse(s, mapper.convertValue(s.getAnalysis(), AnalyzeDtos.AnalysisResponse.class));
  }

  private AnalyzeDtos.SubmissionResponse toResponse(Submission s, AnalyzeDtos.AnalysisResponse a) {
    return new AnalyzeDtos.SubmissionResponse(
        s.getId(),
        s.getProblemTitle(),
        s.getLanguage(),
        s.getCategory(),
        s.getScore(),
        s.getTimeComplexity(),
        s.getSpaceComplexity(),
        s.getCreatedAt(),
        a);
  }
}
