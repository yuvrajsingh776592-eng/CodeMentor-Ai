package com.codementor.api.interview;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {
  Optional<Interview> findByIdAndUserId(UUID id, UUID userId);
}
