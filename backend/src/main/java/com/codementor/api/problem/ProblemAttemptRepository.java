package com.codementor.api.problem;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemAttemptRepository extends JpaRepository<ProblemAttempt, UUID> {
  List<ProblemAttempt> findAllByUserIdOrderByAttemptedAtDesc(UUID userId);
}
