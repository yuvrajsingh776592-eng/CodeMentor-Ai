package com.codementor.api.submission;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
  List<Submission> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<Submission> findByIdAndUserId(UUID id, UUID userId);

  long countByUserId(UUID userId);
}
