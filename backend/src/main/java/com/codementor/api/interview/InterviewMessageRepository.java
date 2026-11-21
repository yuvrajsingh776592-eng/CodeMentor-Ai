package com.codementor.api.interview;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, UUID> {
  List<InterviewMessage> findAllByInterviewIdOrderByCreatedAt(UUID id);
}
