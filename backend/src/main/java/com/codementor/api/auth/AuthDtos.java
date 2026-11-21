package com.codementor.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class AuthDtos {
  private AuthDtos() {}

  public record RegisterRequest(
      @NotBlank @Size(max = 120) String name,
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(min = 10, max = 72) String password) {}

  public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

  public record UserResponse(UUID id, String name, String email, Instant createdAt) {}

  public record AuthResponse(String token, UserResponse user) {}
}
