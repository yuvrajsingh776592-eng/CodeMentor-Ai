package com.codementor.api.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
    try {
      this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    } catch (RuntimeException e) {
      throw new IllegalStateException(
          "app.jwt.secret must be a base64 encoded key of at least 256 bits", e);
    }
    this.expirationMs = expirationMs;
  }

  public String issue(String email) {
    var now = new Date();
    return Jwts.builder()
        .subject(email)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMs))
        .signWith(key)
        .compact();
  }

  public String subject(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
  }
}
