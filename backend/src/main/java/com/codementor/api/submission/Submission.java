package com.codementor.api.submission;

import com.codementor.api.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(
    name = "submissions",
    indexes = @Index(name = "idx_submissions_user_created", columnList = "user_id,created_at"))
public class Submission {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(name = "question_id", length = 40)
  private String questionId;

  @Column(name = "problem_title", nullable = false, length = 200)
  private String problemTitle;

  @Column(nullable = false, length = 30)
  private String language;

  @Column(name = "problem_category", nullable = false, length = 60)
  private String category;

  @Column(name = "problem_statement", nullable = false, columnDefinition = "text")
  private String problem;

  @Column(name = "source_code", nullable = false, columnDefinition = "text")
  private String code;

  @Column(nullable = false)
  private int score;

  @Column(name = "time_complexity", nullable = false, length = 100)
  private String timeComplexity;

  @Column(name = "space_complexity", nullable = false, length = 100)
  private String spaceComplexity;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "analysis_json", nullable = false, columnDefinition = "jsonb")
  private JsonNode analysis;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Submission() {}

  public Submission(
      AppUser user,
      String title,
      String language,
      String category,
      String questionId,
      String problem,
      String code,
      int score,
      String time,
      String space,
      JsonNode analysis) {
    this.user = user;
    this.problemTitle = title;
    this.language = language;
    this.category = category;
    this.questionId = questionId;
    this.problem = problem;
    this.code = code;
    this.score = score;
    this.timeComplexity = time;
    this.spaceComplexity = space;
    this.analysis = analysis;
  }

  @PrePersist
  void created() {
    createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getProblemTitle() {
    return problemTitle;
  }

  public String getLanguage() {
    return language;
  }

  public String getCategory() {
    return category;
  }

  public String getQuestionId() { return questionId; }

  public int getScore() {
    return score;
  }

  public String getTimeComplexity() {
    return timeComplexity;
  }

  public String getSpaceComplexity() {
    return spaceComplexity;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public JsonNode getAnalysis() {
    return analysis;
  }
}
