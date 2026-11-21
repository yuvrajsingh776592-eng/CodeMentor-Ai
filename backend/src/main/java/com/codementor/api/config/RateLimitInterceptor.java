package com.codementor.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
  private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
  private static final long WINDOW_SECONDS = 60;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String path = request.getRequestURI();
    int limit =
        path.equals("/api/analyze")
            ? 12
            : path.equals("/api/auth/login") ? 8 : path.equals("/api/auth/register") ? 5 : 0;
    if (limit == 0) return true;
    String identity =
        request.getUserPrincipal() != null
            ? request.getUserPrincipal().getName()
            : request.getRemoteAddr();
    String key = path + ":" + identity;
    long now = Instant.now().getEpochSecond();
    Window window =
        windows.compute(
            key,
            (ignored, current) ->
                current == null || now - current.startedAt >= WINDOW_SECONDS
                    ? new Window(now, 1)
                    : new Window(current.startedAt, current.count + 1));
    if (windows.size() > 10_000)
      windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt >= WINDOW_SECONDS);
    if (window.count > limit)
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS,
          "Request limit reached. Please wait a minute and try again.");
    return true;
  }

  private record Window(long startedAt, int count) {}
}
