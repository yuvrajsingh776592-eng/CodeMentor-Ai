package com.codementor.api.problem;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {
  List<Problem> findAllByUserIdOrderByUpdatedAtDesc(UUID userId);

  Optional<Problem> findByIdAndUserId(UUID id, UUID userId);
}
