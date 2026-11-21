package com.codementor.api.interview;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "interview_messages",
    indexes =
        @Index(
            name = "idx_interview_messages_interview_date",
            columnList = "interview_id,created_at"))
public class InterviewMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "interview_id", nullable = false)
  private Interview interview;

  @Column(nullable = false, length = 20)
  private String role;

  @Column(nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected InterviewMessage() {}

  public InterviewMessage(Interview i, String role, String content) {
    interview = i;
    this.role = role;
    this.content = content;
  }

  @PrePersist
  void create() {
    createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getRole() {
    return role;
  }

  public String getContent() {
    return content;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
