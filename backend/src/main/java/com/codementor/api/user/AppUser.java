package com.codementor.api.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    indexes = @Index(name = "idx_users_email", columnList = "email", unique = true))
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 254, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected AppUser() {}

  public AppUser(String name, String email, String passwordHash) {
    this.name = name;
    this.email = email.toLowerCase();
    this.passwordHash = passwordHash;
  }

  @PrePersist
  void created() {
    createdAt = updatedAt = Instant.now();
  }

  @PreUpdate
  void updated() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
