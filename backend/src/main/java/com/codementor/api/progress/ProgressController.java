package com.codementor.api.progress;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {
  private final ProgressService service;

  public ProgressController(ProgressService s) {
    service = s;
  }

  @GetMapping
  public ProgressDtos.Summary summary() {
    return service.summary();
  }

  @GetMapping("/topics")
  public java.util.List<ProgressDtos.TopicProgress> topics() {
    return service.summary().topics();
  }
}
