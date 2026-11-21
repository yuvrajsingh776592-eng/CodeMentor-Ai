package com.codementor.api.problem;

import com.codementor.api.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "problems",
    indexes = {
      @Index(name = "idx_problems_user_status", columnList = "user_id,status"),
      @Index(name = "idx_problems_user_topic", columnList = "user_id,topic")
    })
public class Problem {
  public enum Status {
    NOT_STARTED,
    ATTEMPTED,
    SOLVED,
    NEED_REVISION
  }

  public enum Difficulty {
    EASY,
    MEDIUM,
    HARD
  }

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 60)
  private String topic;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Difficulty difficulty;

  @Column(length = 80)
  private String platform;

  @Column(length = 1000)
  private String url;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Status status = Status.NOT_STARTED;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(name = "last_attempted_at")
  private Instant lastAttemptedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Problem() {}

  public Problem(
      AppUser user,
      String title,
      String topic,
      Difficulty difficulty,
      String platform,
      String url,
      String notes) {
    this.user = user;
    this.title = title;
    this.topic = topic;
    this.difficulty = difficulty;
    this.platform = platform;
    this.url = url;
    this.notes = notes;
  }

  @PrePersist
  void create() {
    createdAt = updatedAt = Instant.now();
  }

  @PreUpdate
  void update() {
    updatedAt = Instant.now();
  }

  public void update(Status status, String notes) {
    this.status = status;
    this.notes = notes;
    if (status == Status.ATTEMPTED || status == Status.SOLVED || status == Status.NEED_REVISION)
      this.lastAttemptedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getTopic() {
    return topic;
  }

  public Difficulty getDifficulty() {
    return difficulty;
  }

  public String getPlatform() {
    return platform;
  }

  public String getUrl() {
    return url;
  }

  public Status getStatus() {
    return status;
  }

  public String getNotes() {
    return notes;
  }

  public Instant getLastAttemptedAt() {
    return lastAttemptedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
