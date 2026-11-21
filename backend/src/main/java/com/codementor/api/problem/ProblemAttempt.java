package com.codementor.api.problem;

import com.codementor.api.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "problem_attempts",
    indexes = @Index(name = "idx_attempts_user_date", columnList = "user_id,attempted_at"))
public class ProblemAttempt {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "problem_id", nullable = false)
  private Problem problem;

  @Column(nullable = false, length = 20)
  private String outcome;

  @Column(name = "attempted_at", nullable = false, updatable = false)
  private Instant attemptedAt;

  protected ProblemAttempt() {}

  public ProblemAttempt(AppUser user, Problem problem, String outcome) {
    this.user = user;
    this.problem = problem;
    this.outcome = outcome;
  }

  @PrePersist
  void create() {
    attemptedAt = Instant.now();
  }

  public String getOutcome() {
    return outcome;
  }

  public Instant getAttemptedAt() {
    return attemptedAt;
  }

  public Problem getProblem() {
    return problem;
  }
}
