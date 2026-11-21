package com.codementor.api.interview;

import com.codementor.api.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(
    name = "interviews",
    indexes = @Index(name = "idx_interviews_user_started", columnList = "user_id,started_at"))
public class Interview {
  public enum Status {
    ACTIVE,
    COMPLETED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(nullable = false, length = 60)
  private String topic;

  @Column(nullable = false, length = 20)
  private String difficulty;

  @Column(name = "problem_prompt", nullable = false, columnDefinition = "text")
  private String problemPrompt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status = Status.ACTIVE;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "feedback_json", columnDefinition = "jsonb")
  private JsonNode feedback;

  @Column(name = "started_at", nullable = false, updatable = false)
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  protected Interview() {}

  public Interview(AppUser user, String topic, String difficulty, String prompt) {
    this.user = user;
    this.topic = topic;
    this.difficulty = difficulty;
    this.problemPrompt = prompt;
  }

  @PrePersist
  void create() {
    startedAt = Instant.now();
  }

  public void complete(JsonNode feedback) {
    this.feedback = feedback;
    this.status = Status.COMPLETED;
    this.completedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getTopic() {
    return topic;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public String getProblemPrompt() {
    return problemPrompt;
  }

  public Status getStatus() {
    return status;
  }

  public JsonNode getFeedback() {
    return feedback;
  }

  public Instant getStartedAt() {
    return startedAt;
  }
}
