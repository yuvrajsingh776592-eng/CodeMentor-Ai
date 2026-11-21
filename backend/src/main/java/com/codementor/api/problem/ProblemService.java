package com.codementor.api.problem;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.codementor.api.user.CurrentUserService;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProblemService {
  private final ProblemRepository problems;
  private final CurrentUserService current;
  private final ProblemAttemptRepository attempts;

  public ProblemService(
      ProblemRepository problems, CurrentUserService current, ProblemAttemptRepository attempts) {
    this.problems = problems;
    this.current = current;
    this.attempts = attempts;
  }

  @Transactional
  public ProblemDtos.ProblemResponse create(ProblemDtos.CreateRequest r) {
    return view(
        problems.save(
            new Problem(
                current.get(),
                r.title().trim(),
                r.topic(),
                r.difficulty(),
                r.platform(),
                r.url(),
                r.notes())));
  }

  @Transactional(readOnly = true)
  public List<ProblemDtos.ProblemResponse> list() {
    return problems.findAllByUserIdOrderByUpdatedAtDesc(current.id()).stream()
        .map(this::view)
        .toList();
  }

  @Transactional
  public ProblemDtos.ProblemResponse update(UUID id, ProblemDtos.UpdateRequest r) {
    var p =
        problems
            .findByIdAndUserId(id, current.id())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Problem not found"));
    p.update(r.status(), r.notes());
    if (r.status() != Problem.Status.NOT_STARTED)
      attempts.save(
          new ProblemAttempt(
              current.get(), p, r.status() == Problem.Status.SOLVED ? "SOLVED" : "ATTEMPTED"));
    return view(p);
  }

  private ProblemDtos.ProblemResponse view(Problem p) {
    return new ProblemDtos.ProblemResponse(
        p.getId(),
        p.getTitle(),
        p.getTopic(),
        p.getDifficulty(),
        p.getPlatform(),
        p.getUrl(),
        p.getStatus(),
        p.getNotes(),
        p.getLastAttemptedAt(),
        p.getCreatedAt());
  }
}
