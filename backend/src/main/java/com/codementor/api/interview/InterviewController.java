package com.codementor.api.interview;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {
  private final InterviewService service;

  public InterviewController(InterviewService s) {
    service = s;
  }

  @PostMapping("/start")
  @ResponseStatus(HttpStatus.CREATED)
  public InterviewDtos.InterviewResponse start(@Valid @RequestBody InterviewDtos.StartRequest r) {
    return service.start(r);
  }

  @PostMapping("/{id}/answer")
  public InterviewDtos.InterviewResponse answer(
      @PathVariable UUID id, @Valid @RequestBody InterviewDtos.AnswerRequest r) {
    return service.answer(id, r);
  }

  @GetMapping("/{id}/result")
  public InterviewDtos.InterviewResponse result(@PathVariable UUID id) {
    return service.result(id);
  }
}
