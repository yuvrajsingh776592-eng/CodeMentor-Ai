package com.codementor.api.common;

import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
    var message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return ResponseEntity.badRequest()
        .body(new ApiError("VALIDATION_ERROR", message, Instant.now()));
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<ApiError> authentication(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiError("UNAUTHORIZED", "Email or password is incorrect.", Instant.now()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest()
        .body(new ApiError("INVALID_REQUEST", "Request body is invalid.", Instant.now()));
  }

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<ApiError> status(ResponseStatusException ex) {
    var status = HttpStatus.valueOf(ex.getStatusCode().value());
    return ResponseEntity.status(status)
        .body(
            new ApiError(
                status.name(),
                ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(),
                Instant.now()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception ex) {
    log.error("Unexpected API error", ex);
    return ResponseEntity.internalServerError()
        .body(new ApiError("INTERNAL_ERROR", "An unexpected error occurred.", Instant.now()));
  }
}
