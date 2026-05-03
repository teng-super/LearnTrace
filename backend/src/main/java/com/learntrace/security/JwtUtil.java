package com.learntrace.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
  private final SecretKey key;
  private final long expiresHours;

  public JwtUtil(@Value("${learntrace.jwt.secret}") String secret,
                 @Value("${learntrace.jwt.expires-hours}") long expiresHours) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiresHours = expiresHours;
  }

  public String create(Long userId, String username) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("username", username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expiresHours * 3600)))
        .signWith(key)
        .compact();
  }

  public AuthUser parse(String token) {
    Claims claims = Jwts.parser().verifyWith(key).build()
        .parseSignedClaims(token)
        .getPayload();
    return new AuthUser(Long.valueOf(claims.getSubject()), claims.get("username", String.class));
  }
}
