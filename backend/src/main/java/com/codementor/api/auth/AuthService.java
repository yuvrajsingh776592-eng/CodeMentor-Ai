package com.codementor.api.auth;

import static org.springframework.http.HttpStatus.CONFLICT;

import com.codementor.api.config.JwtService;
import com.codementor.api.user.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final AuthenticationManager auth;
  private final JwtService jwt;

  public AuthService(
      UserRepository users, PasswordEncoder encoder, AuthenticationManager auth, JwtService jwt) {
    this.users = users;
    this.encoder = encoder;
    this.auth = auth;
    this.jwt = jwt;
  }

  @Transactional
  public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest r) {
    var email = r.email().trim().toLowerCase();
    if (users.existsByEmailIgnoreCase(email))
      throw new ResponseStatusException(CONFLICT, "An account with this email already exists");
    try {
      var u = users.saveAndFlush(new AppUser(r.name().trim(), email, encoder.encode(r.password())));
      return response(u);
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(CONFLICT, "An account with this email already exists");
    }
  }

  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest r) {
    var email = r.email().trim().toLowerCase();
    auth.authenticate(new UsernamePasswordAuthenticationToken(email, r.password()));
    return response(users.findByEmailIgnoreCase(email).orElseThrow());
  }

  public AuthDtos.UserResponse current(AppUser u) {
    return view(u);
  }

  private AuthDtos.AuthResponse response(AppUser u) {
    return new AuthDtos.AuthResponse(jwt.issue(u.getEmail()), view(u));
  }

  private AuthDtos.UserResponse view(AppUser u) {
    return new AuthDtos.UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
  }
}
