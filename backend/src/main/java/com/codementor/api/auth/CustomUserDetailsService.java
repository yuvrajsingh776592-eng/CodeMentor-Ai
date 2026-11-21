package com.codementor.api.auth;

import com.codementor.api.user.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository users;

  public CustomUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String email) {
    var u =
        users
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    return User.withUsername(u.getEmail())
        .password(u.getPasswordHash())
        .authorities("USER")
        .build();
  }
}
