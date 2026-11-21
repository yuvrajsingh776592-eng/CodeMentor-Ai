package com.codementor.api.auth;

import com.codementor.api.user.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;
  private final CurrentUserService current;

  public AuthController(AuthService auth, CurrentUserService current) {
    this.auth = auth;
    this.current = current;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest r) {
    return auth.register(r);
  }

  @PostMapping("/login")
  public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest r) {
    return auth.login(r);
  }

  @PostMapping("/logout")
  public void logout() {}

  @GetMapping("/me")
  public AuthDtos.UserResponse me() {
    return auth.current(current.get());
  }
}
