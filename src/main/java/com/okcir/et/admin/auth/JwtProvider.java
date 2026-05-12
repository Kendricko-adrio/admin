package com.okcir.et.admin.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtProvider {

  private final SecretKey signingKey;
  private final long expirationSeconds;
  private final long refreshExpirationSeconds;
  private final String issuer;
  private final String audience;

  public JwtProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration}") long expirationSeconds,
      @Value("${jwt.refresh-expiration}") long refreshExpirationSeconds,
      @Value("${jwt.issuer}") String issuer,
      @Value("${jwt.audience}") String audience) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationSeconds = expirationSeconds;
    this.refreshExpirationSeconds = refreshExpirationSeconds;
    this.issuer = issuer;
    this.audience = audience;
  }

  public String generateToken(String username, List<String> groups, List<String> accessRights) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + expirationSeconds * 1000);

    return Jwts.builder()
        .issuer(issuer)
        .subject(username)
        .audience().add(audience).and()
        .claim("groups", groups)
        .claim("access_rights", accessRights)
        .id(UUID.randomUUID().toString())
        .issuedAt(now)
        .expiration(expiration)
        .signWith(signingKey)
        .compact();
  }

  public String generateRefreshToken(String username) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + refreshExpirationSeconds * 1000);

    return Jwts.builder()
        .issuer(issuer)
        .subject(username)
        .id(UUID.randomUUID().toString())
        .claim("type", "refresh")
        .issuedAt(now)
        .expiration(expiration)
        .signWith(signingKey)
        .compact();
  }

  public long getExpirationSeconds() {
    return expirationSeconds;
  }
}
