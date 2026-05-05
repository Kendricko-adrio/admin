package com.okcir.et.admin.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {

  private final SecretKey signingKey;
  private final long expirationSeconds;

  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration}") long expirationSeconds) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationSeconds = expirationSeconds;
  }

  /**
   * Generate a signed JWT containing the username as subject
   * and a custom "permissions" claim.
   */
  public String generateToken(String username, List<String> permissions) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expirationSeconds * 1000);

    return Jwts.builder()
        .subject(username)
        .claim("permissions", permissions)
        .issuedAt(now)
        .expiration(expiration)
        .signWith(signingKey)
        .compact();
  }

  public long getExpirationSeconds() {
    return expirationSeconds;
  }
}
