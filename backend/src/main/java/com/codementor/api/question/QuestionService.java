package com.codementor.api.question;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.codementor.api.question.QuestionDtos.CoachMode;
import com.codementor.api.question.QuestionDtos.QuestionSummary;
import com.codementor.api.user.CurrentUserService;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class QuestionService {
  private static final TypeReference<List<String>> STRINGS = new TypeReference<>() {};
  private static final TypeReference<List<QuestionDtos.Example>> EXAMPLES = new TypeReference<>() {};

  private final QuestionRepository questions;
  private final CurrentUserService current;
  private final ObjectMapper mapper;
  private final QuestionCoachClient coach;

  public QuestionService(QuestionRepository questions, CurrentUserService current,
      ObjectMapper mapper, QuestionCoachClient coach) {
    this.questions = questions;
    this.current = current;
    this.mapper = mapper;
    this.coach = coach;
  }

  @Transactional(readOnly = true)
  public QuestionDtos.QuestionPage search(String query, String topic, String difficulty,
      String sort, int page, int size) {
    if (page < 1 || size < 1) throw new ResponseStatusException(BAD_REQUEST, "Page and size must be positive");
    if (!sort.equalsIgnoreCase("title") && !sort.equalsIgnoreCase("difficulty"))
      throw new ResponseStatusException(BAD_REQUEST, "Sort must be title or difficulty");
    var rows = questions.findAll().stream()
        .filter(q -> query == null || query.isBlank() || matches(q, query.trim().toLowerCase(Locale.ROOT)))
        .filter(q -> topic == null || topic.isBlank() || list(q.topicsJson()).stream().anyMatch(t -> t.equalsIgnoreCase(topic)))
        .filter(q -> difficulty == null || difficulty.isBlank() || q.difficulty().name().equalsIgnoreCase(difficulty))
        .sorted(sort.equalsIgnoreCase("title")
            ? Comparator.comparing(QuestionRepository.QuestionRecord::title, String.CASE_INSENSITIVE_ORDER)
            : Comparator.comparing(QuestionRepository.QuestionRecord::difficulty)
                .thenComparing(QuestionRepository.QuestionRecord::title, String.CASE_INSENSITIVE_ORDER))
        .toList();
    long total = rows.size();
    int pages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
    long offset = (long) (page - 1) * size;
    int from = (int) Math.min(offset, rows.size());
    int to = (int) Math.min(offset + size, rows.size());
    var slice = rows.subList(from, to);
    UUID userId = current.id();
    return new QuestionDtos.QuestionPage(slice.stream().map(q -> summary(q, userId)).toList(),
        page, size, total, pages, page < pages, page > 1 && total > 0);
  }

  @Transactional(readOnly = true)
  public QuestionDtos.QuestionDetail get(String slug) {
    var question = find(slug);
    var state = questions.progress(current.id(), question.id());
    return new QuestionDtos.QuestionDetail(question.id(), question.slug(), question.title(),
        question.description(), examples(question.examplesJson()), list(question.constraintsJson()),
        question.difficulty(), list(question.topicsJson()), question.sourceName(), question.sourceUrl(),
        new QuestionDtos.Progress(state.status(), state.bookmarked(), state.hintsRevealed()),
        questions.revealedHints(current.id(), question.id()), list(question.hintsJson()).size());
  }

  @Transactional
  public QuestionDtos.QuestionDetail updateProgress(String slug, QuestionDtos.ProgressUpdate update) {
    var question = find(slug);
    UUID userId = current.id();
    var before = questions.progress(userId, question.id());
    var after = questions.updateProgress(userId, question.id(), update);
    if (before.status() != update.status() && update.status() != QuestionDtos.Status.TODO)
      questions.addAttempt(userId, question.id(), update.status().name(), null);
    return detail(question, after);
  }

  @Transactional
  public QuestionDtos.CoachResponse coach(String slug, QuestionDtos.CoachRequest request) {
    var question = find(slug);
    UUID userId = current.id();
    var progress = questions.progress(userId, question.id());
    int revealed = progress.hintsRevealed();
    String content;
    boolean generated = false;
    if (request.mode() == CoachMode.HINT) {
      if (revealed >= list(question.hintsJson()).size())
        throw new ResponseStatusException(BAD_REQUEST, "All available hints have already been revealed");
      if (request.hintIndex() != null && request.hintIndex() != revealed)
        throw new ResponseStatusException(BAD_REQUEST, "Request the next hint in sequence");
      var response = coach.ask(question, request.mode(), revealed);
      content = response.content() == null || response.content().isBlank()
          ? list(question.hintsJson()).get(revealed) : response.content();
      generated = response.aiGenerated();
      revealed = questions.revealHint(userId, question.id(), revealed);
      questions.saveHint(userId, question.id(), revealed - 1, content);
    } else {
      var response = coach.ask(question, request.mode(), revealed);
      generated = response.aiGenerated();
      content = response.content();
      if (content == null || content.isBlank()) {
        content = switch (request.mode()) {
          case APPROACH -> question.approach();
          case COMPLEXITY -> "Time: " + question.timeComplexity() + "\nSpace: " + question.spaceComplexity();
          case SOLUTION -> question.solutionExplanation();
          case HINT -> throw new IllegalStateException("Hint mode handled above");
        };
      }
    }
    return new QuestionDtos.CoachResponse(request.mode(), content, revealed, generated);
  }

  @Transactional(readOnly = true)
  public List<QuestionDtos.Attempt> attempts(String slug) {
    var question = find(slug);
    return questions.attempts(current.id(), question.id());
  }

  @Transactional(readOnly = true)
  public List<String> topics() {
    return questions.findAll().stream().flatMap(q -> list(q.topicsJson()).stream())
        .distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
  }

  @Transactional(readOnly = true)
  public QuestionDtos.ProgressSummary progressSummary() {
    return questions.progressSummary(current.id());
  }

  public QuestionRepository.QuestionRecord find(String slug) {
    return questions.findBySlug(slug)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Question not found"));
  }

  private QuestionDtos.QuestionDetail detail(QuestionRepository.QuestionRecord q,
      QuestionRepository.ProgressRecord p) {
    return new QuestionDtos.QuestionDetail(q.id(), q.slug(), q.title(), q.description(),
        examples(q.examplesJson()), list(q.constraintsJson()), q.difficulty(), list(q.topicsJson()),
        q.sourceName(), q.sourceUrl(), new QuestionDtos.Progress(p.status(), p.bookmarked(), p.hintsRevealed()),
        questions.revealedHints(current.id(), q.id()), list(q.hintsJson()).size());
  }

  private QuestionSummary summary(QuestionRepository.QuestionRecord q, UUID userId) {
    var p = questions.progress(userId, q.id());
    return new QuestionSummary(q.id(), q.slug(), q.title(), q.difficulty(), list(q.topicsJson()),
        q.sourceName(), q.sourceUrl(), p.status(), p.bookmarked());
  }

  private boolean matches(QuestionRepository.QuestionRecord q, String query) {
    return q.id().toLowerCase(Locale.ROOT).contains(query)
        || q.slug().toLowerCase(Locale.ROOT).contains(query)
        || q.title().toLowerCase(Locale.ROOT).contains(query);
  }

  private List<String> list(String json) {
    try { return mapper.readValue(json, STRINGS); }
    catch (Exception ex) { throw new IllegalStateException("Invalid question bank list data", ex); }
  }

  private List<QuestionDtos.Example> examples(String json) {
    try { return mapper.readValue(json, EXAMPLES); }
    catch (Exception ex) { throw new IllegalStateException("Invalid question examples", ex); }
  }
}
