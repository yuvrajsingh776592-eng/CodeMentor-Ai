package com.codementor.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
  @Test
  void issueAndReadTokenForEmail() {
    var secret =
        Base64.getEncoder().encodeToString("codementor-tests-use-a-strong-secret-key".getBytes());
    var service = new JwtService(secret, 60_000);
    var token = service.issue("learner@example.com");
    assertThat(service.subject(token)).isEqualTo("learner@example.com");
  }
}
