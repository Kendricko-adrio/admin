# Plan: Enhanced Login Feature — Admin Service

## Overview

Enhance flow login Admin Service agar:
- Validasi user via database (non-LDAP mode)
- LDAP mode dengan placeholder `#TODO`
- JWT access token berisi `username`, `groups`, `access_rights`
- Response simpel: `access_token`, `refresh_token`, `token_type`, `expires_in`
- Semua info user (groups, permissions) ada di dalam JWT, bukan di response body

---

## Files to Modify (5 files)

### 1. `src/main/java/com/okcir/et/admin/user/UserRepository.java`

Add method:
```java
Optional<User> findByUsername(String username);
```

Full file after change — add this line inside the interface, before the closing brace:
```java
  java.util.Optional<User> findByUsername(String username);
```

### 2. `src/main/java/com/okcir/et/admin/auth/dto/LoginResponse.java`

Add `refreshToken` field. Remove NO existing fields.

```java
package com.okcir.et.admin.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("token_type")
  @Builder.Default
  private String tokenType = "Bearer";

  @JsonProperty("expires_in")
  private long expiresIn;
}
```

**IMPORTANT:** No `userId`, `role`, `permissions`, `groups` fields. Those are inside the JWT.

### 3. `src/main/java/com/okcir/et/admin/auth/JwtProvider.java`

Full rewrite. New signature: `generateToken(String username, List<String> groups, List<String> accessRights)`. Add `generateRefreshToken(String username)`. Add `iss`, `jti` claims. JWT claims:

```json
{
  "sub": "username",
  "iss": "auth.yourdomain.com",
  "aud": "api.yourdomain.com",
  "jti": "uuid-v4",
  "iat": 1716235422,
  "exp": 1716236322,
  "groups": ["TRADER"],
  "access_rights": ["orders:read", "users:read"]
}
```

Key details:
- Keep HS256 signing (symmetric key from `jwt.secret`)
- `jti` = random UUID
- Inject `@Value("${jwt.issuer}")` and `@Value("${jwt.audience}")`
- `generateRefreshToken` — separate JWT, expiration from `jwt.refresh-expiration`, claims: `sub`, `jti`, `type`="refresh"
- `getExpirationSeconds()` returns the access token expiration (not refresh)

Full implementation:

```java
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
```

**NOTE:** The `Jwts.builder().audience().add(audience).and()` chain may vary by jjwt version. Use the correct API for the jjwt version in pom.xml. Check the actual jjwt version and use the appropriate API:
- jjwt 0.12.x: `.audience().add(audience).and()`
- jjwt 0.11.x: `.setAudience(audience)`

Look at the pom.xml to determine which version is used and write the correct API call.

### 4. `src/main/java/com/okcir/et/admin/auth/AuthService.java`

Full rewrite. Key changes:
- Inject `UserRepository` (from `com.okcir.et.admin.user` package)
- Use `@Transactional(readOnly = true)` for lazy loading of `groups` and `accessRights`
- LDAP mode: `#TODO: Implement REST call to LDAP service` — throw `AuthenticationException("LDAP authentication not yet implemented")` for now
- Non-LDAP mode: `userRepository.findByUsername(username)` → not found → throw `AuthenticationException("User not found")`
- Extract groups and access rights from the User entity
- Generate both tokens
- Return LoginResponse with only the 4 fields

Full implementation:

```java
package com.okcir.et.admin.auth;

import com.okcir.et.admin.accessright.AccessRight;
import com.okcir.et.admin.auth.dto.LoginRequest;
import com.okcir.et.admin.auth.dto.LoginResponse;
import com.okcir.et.admin.common.exception.AuthenticationException;
import com.okcir.et.admin.group.Group;
import com.okcir.et.admin.user.User;
import com.okcir.et.admin.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;

  @Value("${auth.is-ldap}")
  private boolean isLdap;

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    String username = request.getUsername();

    if (isLdap) {
      // TODO: Implement REST call to LDAP service for password verification
      // The LDAP service will validate username + password and return user identity.
      // After successful LDAP auth, look up the user in our database to get groups/access rights.
      authenticateViaLdap(request);
    } else {
      authenticateBasic(request);
    }

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new AuthenticationException("User not found"));

    List<String> groupNames = user.getGroups().stream()
        .map(Group::getName)
        .distinct()
        .sorted()
        .toList();

    List<String> accessRightCodes = user.getGroups().stream()
        .flatMap(g -> g.getAccessRights().stream())
        .map(AccessRight::getCode)
        .distinct()
        .sorted()
        .toList();

    String accessToken = jwtProvider.generateToken(username, groupNames, accessRightCodes);
    String refreshToken = jwtProvider.generateRefreshToken(username);

    log.info("User '{}' logged in successfully — groups: {}, accessRights: {}",
        username, groupNames, accessRightCodes);

    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtProvider.getExpirationSeconds())
        .build();
  }

  private void authenticateViaLdap(LoginRequest request) {
    // TODO: Implement REST call to LDAP service
    // Steps:
    // 1. Call LDAP service REST endpoint with username + password
    // 2. If LDAP returns success → continue to DB lookup
    // 3. If LDAP returns failure → throw AuthenticationException
    throw new AuthenticationException("LDAP authentication not yet implemented");
  }

  private void authenticateBasic(LoginRequest request) {
    String username = request.getUsername();
    if (username == null || username.isBlank()) {
      throw new AuthenticationException("Username is required");
    }
    // In basic mode, we only check if the user exists in the database.
    // Password is ignored (user can enter anything).
    if (!userRepository.existsByUsername(username)) {
      throw new AuthenticationException("User not found");
    }
  }
}
```

### 5. `src/main/resources/application.yml`

Update the jwt section and keep everything else exactly the same:

```yaml
spring:
  application:
    name: admin

  # ── PostgreSQL ──────────────────────────────────────────
  datasource:
    url: jdbc:postgresql://localhost:5432/et
    username: okcir
    password: okcir
    driver-class-name: org.postgresql.Driver

  # ── JPA / Hibernate ────────────────────────────────────
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  # ── Flyway ─────────────────────────────────────────────
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0

  # ── Redis ──────────────────────────────────────────────
  data:
    redis:
      host: localhost
      port: 6379

# ── Auth / JWT ───────────────────────────────────────
auth:
  is-ldap: false  # true = LDAP (not yet implemented), false = username-only (check DB)

jwt:
  secret: "YourBase64EncodedSecretKeyThatIsAtLeast256BitsLong!!"
  expiration: 900              # seconds (15 minutes)
  refresh-expiration: 604800   # seconds (7 days)
  issuer: "auth.yourdomain.com"
  audience: "api.yourdomain.com"
```

---

## Files NOT Modified

- `LoginRequest.java` — stays the same (username @NotBlank, password optional)
- `AuthController.java` — stays the same (POST /api/auth/login)
- `GlobalExceptionHandler.java` — stays the same
- All entity classes (User, Group, AccessRight) — stay the same
- All other repositories, services, controllers — stay the same
- `JpaConfig.java`, `AdminApplication.java` — stay the same

---

## Verification After Implementation

1. Run `./mvnw compile` — must pass with no errors
2. Check that `LoginResponse` has exactly 4 fields: `accessToken`, `refreshToken`, `tokenType`, `expiresIn`
3. Check that `JwtProvider.generateToken` includes claims: `sub`, `iss`, `aud`, `jti`, `iat`, `exp`, `groups`, `access_rights`
4. Check that `AuthService` uses `UserRepository` and `@Transactional(readOnly = true)`
5. Check that `UserRepository` has `findByUsername` method
6. Check that `application.yml` has `jwt.issuer`, `jwt.audience`, `jwt.refresh-expiration`, and `expiration: 900`

---

## Context7 MCP Instructions

When implementing, use Context7 MCP server to look up documentation for:
- **jjwt** (io.jsonwebtoken / jwt): Use `mcp_context7_resolve_library_id` with libraryName="jjwt" then `mcp_context7_query_docs` to get the exact API for `.audience()`, `.issuer()`, `.id()`, `.claim()` builder methods. The jjwt API differs between 0.11.x and 0.12.x — always verify the correct API for the version in pom.xml.
- **Spring Boot**: For `@Value`, `@Transactional`, repository method naming conventions — look up the Spring Boot version in pom.xml (4.0.2) and use Context7 to get exact API docs.
- **Lombok**: For `@Builder.Default` behavior with `@JsonProperty` — verify no conflicts.

To use Context7:
1. First call `mcp_context7_resolve_library_id` with the library name
2. Then call `mcp_context7_query_docs` with the library ID and specific query
3. Use the documentation snippets to write correct, version-appropriate code

**PRO TIP:** Before writing JwtProvider, check pom.xml for the jjwt version, then use Context7 to look up the EXACT builder API for that version. The `.audience()` API is the most common source of compilation errors.
