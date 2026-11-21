package com.codementor.api.progress;

import com.codementor.api.problem.*;
import com.codementor.api.submission.SubmissionRepository;
import com.codementor.api.user.CurrentUserService;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {
  private final ProblemRepository problems;
  private final ProblemAttemptRepository attempts;
  private final SubmissionRepository submissions;
  private final CurrentUserService current;

  public ProgressService(
      ProblemRepository p,
      ProblemAttemptRepository a,
      SubmissionRepository s,
      CurrentUserService c) {
    problems = p;
    attempts = a;
    submissions = s;
    current = c;
  }

  @Transactional(readOnly = true)
  public ProgressDtos.Summary summary() {
    var uid = current.id();
    var ps = problems.findAllByUserIdOrderByUpdatedAtDesc(uid);
    var ats = attempts.findAllByUserIdOrderByAttemptedAtDesc(uid);
    var byTopic =
        ps.stream()
            .collect(Collectors.groupingBy(Problem::getTopic, TreeMap::new, Collectors.toList()));
    var topics =
        byTopic.entrySet().stream()
            .map(
                e -> {
                  var rows = e.getValue();
                  long solved =
                      rows.stream().filter(p -> p.getStatus() == Problem.Status.SOLVED).count();
                  long attempted =
                      rows.stream()
                          .filter(p -> p.getStatus() != Problem.Status.NOT_STARTED)
                          .count();
                  return new ProgressDtos.TopicProgress(
                      e.getKey(),
                      rows.size(),
                      solved,
                      attempted,
                      rows.isEmpty() ? 0 : round(100.0 * solved / rows.size()),
                      attempted == 0 ? 0 : round(100.0 * solved / attempted));
                })
            .toList();
    var ranked =
        topics.stream()
            .filter(t -> t.attempted() > 0)
            .sorted(Comparator.comparingDouble(ProgressDtos.TopicProgress::successRate))
            .toList();
    var counts =
        ats.stream()
            .collect(
                Collectors.groupingBy(
                    a -> a.getAttemptedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                    Collectors.counting()));
    var today = LocalDate.now(ZoneOffset.UTC);
    int streak = streak(counts, today);
    var activity = new ArrayList<ProgressDtos.ActivityDay>();
    for (int i = 27; i >= 0; i--) {
      var d = today.minusDays(i);
      activity.add(new ProgressDtos.ActivityDay(d.toString(), counts.getOrDefault(d, 0L)));
    }
    long solved = ps.stream().filter(p -> p.getStatus() == Problem.Status.SOLVED).count();
    long attempted = ps.stream().filter(p -> p.getStatus() != Problem.Status.NOT_STARTED).count();
    long totalSub = submissions.countByUserId(uid);
    long weekly =
        ats.stream()
            .filter(
                a ->
                    !a.getAttemptedAt()
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                        .isBefore(today.minusDays(6)))
            .count();
    long monthly =
        ats.stream()
            .filter(
                a ->
                    YearMonth.from(a.getAttemptedAt().atZone(ZoneOffset.UTC).toLocalDate())
                        .equals(YearMonth.from(today)))
            .count();
    var difficulty =
        Arrays.stream(Problem.Difficulty.values())
            .map(
                d ->
                    new ProgressDtos.DifficultyCount(
                        d.name(), ps.stream().filter(p -> p.getDifficulty() == d).count()))
            .toList();
    return new ProgressDtos.Summary(
        solved,
        attempted,
        ps.size() - solved,
        totalSub,
        streak,
        longest(counts),
        ranked.isEmpty() ? "—" : ranked.get(ranked.size() - 1).topic(),
        ranked.isEmpty() ? "Not enough data" : ranked.get(0).topic(),
        weekly,
        monthly,
        difficulty,
        topics,
        activity);
  }

  private int streak(Map<LocalDate, Long> days, LocalDate now) {
    LocalDate d = days.containsKey(now) ? now : now.minusDays(1);
    if (!days.containsKey(d)) return 0;
    int n = 0;
    while (days.containsKey(d)) {
      n++;
      d = d.minusDays(1);
    }
    return n;
  }

  private int longest(Map<LocalDate, Long> days) {
    int best = 0, run = 0;
    LocalDate previous = null;
    for (var d : new TreeSet<>(days.keySet())) {
      if (previous != null && previous.plusDays(1).equals(d)) run++;
      else run = 1;
      best = Math.max(best, run);
      previous = d;
    }
    return best;
  }

  private double round(double v) {
    return Math.round(v * 10.0) / 10.0;
  }
}
