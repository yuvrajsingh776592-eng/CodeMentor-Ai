package com.codementor.api.question;

import java.io.InputStream;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class QuestionBankSeeder implements ApplicationRunner {
  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;

  public QuestionBankSeeder(JdbcTemplate jdbc, ObjectMapper mapper) {
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws Exception {
    try (InputStream input = new ClassPathResource("question-bank.json").getInputStream()) {
      List<SeedQuestion> questions = mapper.readValue(input, new TypeReference<>() {});
      for (var q : questions) {
        jdbc.update("""
            INSERT INTO questions(id, slug, title, description, examples, constraints_json,
              difficulty, topics, source_name, source_url, hints, approach, time_complexity,
              space_complexity, solution_explanation)
            VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?::jsonb, ?, ?, ?::jsonb, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET slug=EXCLUDED.slug, title=EXCLUDED.title,
              description=EXCLUDED.description, examples=EXCLUDED.examples,
              constraints_json=EXCLUDED.constraints_json, difficulty=EXCLUDED.difficulty,
              topics=EXCLUDED.topics, source_name=EXCLUDED.source_name,
              source_url=EXCLUDED.source_url, hints=EXCLUDED.hints, approach=EXCLUDED.approach,
              time_complexity=EXCLUDED.time_complexity, space_complexity=EXCLUDED.space_complexity,
              solution_explanation=EXCLUDED.solution_explanation
            """, q.id(), q.slug(), q.title(), q.description(), json(q.examples()),
            json(q.constraints()), q.difficulty(), json(q.topics()), q.sourceName(), q.sourceUrl(),
            json(q.hints()), q.approach(), q.timeComplexity(), q.spaceComplexity(), q.solutionExplanation());
      }
    }
  }

  private String json(Object value) { return mapper.writeValueAsString(value); }

  private record SeedQuestion(String id, String slug, String title, String description,
      List<QuestionDtos.Example> examples, List<String> constraints, String difficulty,
      List<String> topics, String sourceName, String sourceUrl, List<String> hints,
      String approach, String timeComplexity, String spaceComplexity, String solutionExplanation) {}
}
