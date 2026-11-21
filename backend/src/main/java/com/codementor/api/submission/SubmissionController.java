package com.codementor.api.submission;

import com.codementor.api.analysis.AnalyzeDtos;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SubmissionController {
  private final SubmissionService service;

  public SubmissionController(SubmissionService service) {
    this.service = service;
  }

  @PostMapping("/analyze")
  @ResponseStatus(HttpStatus.CREATED)
  public AnalyzeDtos.SubmissionResponse analyze(
      @Valid @RequestBody AnalyzeDtos.AnalyzeRequest request) {
    return service.analyze(request);
  }

  @GetMapping("/submissions")
  public List<AnalyzeDtos.SubmissionSummary> list() {
    return service.list();
  }

  @GetMapping("/submissions/{id}")
  public AnalyzeDtos.SubmissionResponse get(@PathVariable UUID id) {
    return service.get(id);
  }
}
