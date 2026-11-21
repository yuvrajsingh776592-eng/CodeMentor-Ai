package com.codementor.api.problem;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {
  private final ProblemService service;

  public ProblemController(ProblemService service) {
    this.service = service;
  }

  @GetMapping
  public List<ProblemDtos.ProblemResponse> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProblemDtos.ProblemResponse create(@Valid @RequestBody ProblemDtos.CreateRequest r) {
    return service.create(r);
  }

  @PatchMapping("/{id}")
  public ProblemDtos.ProblemResponse update(
      @PathVariable UUID id, @Valid @RequestBody ProblemDtos.UpdateRequest r) {
    return service.update(id, r);
  }
}
