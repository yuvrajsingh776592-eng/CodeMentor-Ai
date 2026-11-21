package com.codementor.api.user;

import com.codementor.api.auth.AuthDtos;
import com.codementor.api.auth.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final CurrentUserService currentUser;
  private final AuthService authService;

  public UserController(CurrentUserService currentUser, AuthService authService) {
    this.currentUser = currentUser;
    this.authService = authService;
  }

  @GetMapping("/me")
  public AuthDtos.UserResponse me() {
    return authService.current(currentUser.get());
  }
}
