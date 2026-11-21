package com.codementor.api.question;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class QuestionRepository {
  private final JdbcTemplate jdbc;

  public QuestionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public List<QuestionRecord> findAll() {
    return jdbc.query("""
        SELECT id, slug, title, description, examples::text, constraints_json::text,
               difficulty, topics::text, source_name, source_url, hints::text,
               approach, time_complexity, space_complexity, solution_explanation
        FROM questions ORDER BY id
        """, (rs, row) -> new QuestionRecord(
        rs.getString("id"), rs.getString("slug"), rs.getString("title"),
        rs.getString("description"), rs.getString("examples"), rs.getString("constraints_json"),
        QuestionDtos.Difficulty.valueOf(rs.getString("difficulty")), rs.getString("topics"),
        rs.getString("source_name"), rs.getString("source_url"), rs.getString("hints"),
        rs.getString("approach"), rs.getString("time_complexity"),
        rs.getString("space_complexity"), rs.getString("solution_explanation")));
  }

  public Optional<QuestionRecord> findBySlug(String slug) {
    return findAll().stream().filter(q -> q.slug().equals(slug)).findFirst();
  }

  public Optional<QuestionRecord> findById(String id) {
    return findAll().stream().filter(q -> q.id().equals(id)).findFirst();
  }

  public ProgressRecord progress(UUID userId, String questionId) {
    return jdbc.query("""
        SELECT status, bookmarked, hints_revealed FROM user_question_progress
        WHERE user_id = ? AND question_id = ?
        """, rs -> rs.next()
        ? new ProgressRecord(QuestionDtos.Status.valueOf(rs.getString("status")),
            rs.getBoolean("bookmarked"), rs.getInt("hints_revealed"))
        : new ProgressRecord(QuestionDtos.Status.TODO, false, 0), userId, questionId);
  }

  public ProgressRecord updateProgress(UUID userId, String questionId,
      QuestionDtos.ProgressUpdate update) {
    jdbc.update("""
        INSERT INTO user_question_progress(user_id, question_id, status, bookmarked)
        VALUES (?, ?, ?, ?)
        ON CONFLICT(user_id, question_id) DO UPDATE SET status = EXCLUDED.status,
          bookmarked = EXCLUDED.bookmarked, updated_at = now()
        """, userId, questionId, update.status().name(), update.bookmarked());
    return progress(userId, questionId);
  }

  public int revealHint(UUID userId, String questionId, int expectedIndex) {
    int changed = jdbc.update("""
        INSERT INTO user_question_progress(user_id, question_id, hints_revealed)
        VALUES (?, ?, 1)
        ON CONFLICT(user_id, question_id) DO UPDATE SET
          hints_revealed = user_question_progress.hints_revealed + 1, updated_at = now()
        WHERE user_question_progress.hints_revealed = ?
        """, userId, questionId, expectedIndex);
    if (changed == 0) throw new org.springframework.web.server.ResponseStatusException(
        org.springframework.http.HttpStatus.CONFLICT, "A hint was already revealed in another request");
    return progress(userId, questionId).hintsRevealed();
  }

  public void saveHint(UUID userId, String questionId, int index, String content) {
    jdbc.update("""
        INSERT INTO question_coach_history(user_id, question_id, hint_index, content)
        VALUES (?, ?, ?, ?) ON CONFLICT(user_id, question_id, hint_index)
        DO UPDATE SET content = EXCLUDED.content, created_at = now()
        """, userId, questionId, index, content);
  }

  public List<String> revealedHints(UUID userId, String questionId) {
    return jdbc.query("""
        SELECT content FROM question_coach_history
        WHERE user_id = ? AND question_id = ? ORDER BY hint_index
        """, (rs, row) -> rs.getString("content"), userId, questionId);
  }

  public QuestionDtos.ProgressSummary progressSummary(UUID userId) {
    return jdbc.query("""
        SELECT count(*) AS total,
          count(*) FILTER (WHERE coalesce(p.status, 'TODO') = 'TODO') AS todo,
          count(*) FILTER (WHERE p.status = 'ATTEMPTED') AS attempted,
          count(*) FILTER (WHERE p.status = 'SOLVED') AS solved,
          count(*) FILTER (WHERE p.bookmarked = true) AS bookmarked
        FROM questions q LEFT JOIN user_question_progress p
          ON p.question_id = q.id AND p.user_id = ?
        """, rs -> {
          rs.next();
          return new QuestionDtos.ProgressSummary(rs.getLong("total"), rs.getLong("todo"),
              rs.getLong("attempted"), rs.getLong("solved"), rs.getLong("bookmarked"));
        }, userId);
  }

  public void addAttempt(UUID userId, String questionId, String outcome, UUID submissionId) {
    jdbc.update("""
        INSERT INTO question_attempts(id, user_id, question_id, submission_id, outcome)
        VALUES (?, ?, ?, ?, ?)
        """, UUID.randomUUID(), userId, questionId, submissionId, outcome);
    jdbc.update("""
        INSERT INTO user_question_progress(user_id, question_id, status)
        VALUES (?, ?, 'ATTEMPTED')
        ON CONFLICT(user_id, question_id) DO UPDATE SET
          status = CASE WHEN ? = 'SOLVED' THEN 'SOLVED' WHEN user_question_progress.status = 'SOLVED' THEN 'SOLVED' ELSE 'ATTEMPTED' END,
          updated_at = now()
        """, userId, questionId, outcome);
  }

  public List<QuestionDtos.Attempt> attempts(UUID userId, String questionId) {
    return jdbc.query("""
        SELECT qa.id::text, qa.outcome, qa.attempted_at, qa.submission_id::text,
               s.score, s.language
        FROM question_attempts qa LEFT JOIN submissions s ON s.id = qa.submission_id
        WHERE qa.user_id = ? AND qa.question_id = ? ORDER BY qa.attempted_at DESC
        """, (rs, row) -> new QuestionDtos.Attempt(rs.getString("id"), rs.getString("outcome"),
        rs.getTimestamp("attempted_at").toInstant(), rs.getString("submission_id"),
        (Integer) rs.getObject("score"), rs.getString("language")), userId, questionId);
  }

  public record QuestionRecord(String id, String slug, String title, String description,
      String examplesJson, String constraintsJson, QuestionDtos.Difficulty difficulty,
      String topicsJson, String sourceName, String sourceUrl, String hintsJson, String approach,
      String timeComplexity, String spaceComplexity, String solutionExplanation) {}

  public record ProgressRecord(QuestionDtos.Status status, boolean bookmarked, int hintsRevealed) {}
}
