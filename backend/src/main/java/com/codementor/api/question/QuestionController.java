package com.codementor.api.question;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
  private final QuestionService service;

  public QuestionController(QuestionService service) { this.service = service; }

  @GetMapping
  public QuestionDtos.QuestionPage list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String topic,
      @RequestParam(required = false) String difficulty,
      @RequestParam(defaultValue = "title") String sort,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "16") int size) {
    return service.search(q, topic, difficulty, sort, page, Math.min(size, 100));
  }

  @GetMapping("/topics")
  public List<String> topics() { return service.topics(); }

  @GetMapping("/progress/summary")
  public QuestionDtos.ProgressSummary progressSummary() { return service.progressSummary(); }

  @GetMapping("/{slug}")
  public QuestionDtos.QuestionDetail get(@PathVariable String slug) { return service.get(slug); }

  @GetMapping("/{slug}/attempts")
  public List<QuestionDtos.Attempt> attempts(@PathVariable String slug) { return service.attempts(slug); }

  @PatchMapping("/{slug}/progress")
  public QuestionDtos.QuestionDetail updateProgress(@PathVariable String slug,
      @Valid @RequestBody QuestionDtos.ProgressUpdate update) {
    return service.updateProgress(slug, update);
  }

  @PostMapping("/{slug}/coach")
  public QuestionDtos.CoachResponse coach(@PathVariable String slug,
      @Valid @RequestBody QuestionDtos.CoachRequest request) {
    return service.coach(slug, request);
  }
}
