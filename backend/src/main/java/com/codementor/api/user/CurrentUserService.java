package com.codementor.api.user;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentUserService {
  private final UserRepository users;

  public CurrentUserService(UserRepository users) {
    this.users = users;
  }

  public AppUser get() {
    var principal = SecurityContextHolder.getContext().getAuthentication();
    if (principal == null)
      throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
    return users
        .findByEmailIgnoreCase(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User no longer exists"));
  }

  public UUID id() {
    return get().getId();
  }
}
