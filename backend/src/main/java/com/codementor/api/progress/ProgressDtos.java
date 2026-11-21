package com.codementor.api.progress;

import java.util.List;

public final class ProgressDtos {
  private ProgressDtos() {}

  public record Summary(
      long solved,
      long attempted,
      long remaining,
      long submissions,
      int currentStreak,
      int longestStreak,
      String strongestTopic,
      String weakestTopic,
      long weeklyActivity,
      long monthlyActivity,
      List<DifficultyCount> difficultyDistribution,
      List<TopicProgress> topics,
      List<ActivityDay> activity) {}

  public record TopicProgress(
      String topic,
      long total,
      long solved,
      long attempted,
      double completionPercent,
      double successRate) {}

  public record DifficultyCount(String difficulty, long count) {}

  public record ActivityDay(String date, long count) {}
}
