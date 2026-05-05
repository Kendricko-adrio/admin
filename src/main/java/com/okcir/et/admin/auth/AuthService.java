package com.okcir.et.admin.auth;

import com.okcir.et.admin.auth.dto.LoginRequest;
import com.okcir.et.admin.auth.dto.LoginResponse;
import com.okcir.et.admin.common.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final JwtProvider jwtProvider;

  @Value("${auth.is-ldap}")
  private boolean isLdap;

  /**
   * Authenticate the user and return a JWT-based login response.
   *
   * <ul>
   * <li><b>LDAP mode</b> ({@code auth.is-ldap=true}): simulates REST call
   * to internal LDAP service. On success assigns ROLE_USER, ROLE_ADMIN.</li>
   * <li><b>Basic mode</b> ({@code auth.is-ldap=false}): validates username
   * is provided, assigns ROLE_GUEST.</li>
   * </ul>
   */
  public LoginResponse login(LoginRequest request) {
    List<String> permissions;

    if (isLdap) {
      permissions = authenticateViaLdap(request);
    } else {
      permissions = authenticateBasic(request);
    }

    String token = jwtProvider.generateToken(request.getUsername(), permissions);

    return LoginResponse.builder()
        .accessToken(token)
        .tokenType("Bearer")
        .expiresIn(jwtProvider.getExpirationSeconds())
        .build();
  }

  // ── LDAP simulation ──────────────────────────────────

  private List<String> authenticateViaLdap(LoginRequest request) {
    log.info("Authenticating user '{}' via LDAP simulation", request.getUsername());

    if (request.getPassword() == null || request.getPassword().isBlank()) {
      throw new AuthenticationException("Password is required for LDAP authentication");
    }

    // Simulated LDAP REST call – in production this would be a real HTTP call
    boolean ldapSuccess = simulateLdapCall(request.getUsername(), request.getPassword());

    if (!ldapSuccess) {
      throw new AuthenticationException("Invalid username or password");
    }

    // Dummy permissions for LDAP-authenticated users
    return List.of("ROLE_USER", "ROLE_ADMIN");
  }

  /**
   * Simulates an internal REST call to an LDAP service.
   * Returns {@code true} when the password equals "password" (demo purposes).
   */
  private boolean simulateLdapCall(String username, String password) {
    log.debug("Simulating LDAP REST call for user '{}'", username);
    // In a real scenario, this would call an external LDAP service via
    // RestClient/WebClient
    return "password".equals(password);
  }

  // ── Basic (no-password) authentication ───────────────

  private List<String> authenticateBasic(LoginRequest request) {
    log.info("Authenticating user '{}' via Basic mode (no password check)", request.getUsername());

    // Username presence is already guaranteed by @NotBlank on the DTO,
    // but we keep an explicit guard for safety.
    if (request.getUsername() == null || request.getUsername().isBlank()) {
      throw new AuthenticationException("Username is required");
    }

    // Default permissions for basic-mode users
    return List.of("ROLE_GUEST");
  }
}
