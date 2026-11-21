package com.codementor.api.health;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
public class HealthController {
  @Value("${app.version:development}")
  private String version;

  @GetMapping
  public HealthResponse health() {
    return new HealthResponse("ok", version, Instant.now());
  }

  public record HealthResponse(String status, String version, Instant timestamp) {}
}
