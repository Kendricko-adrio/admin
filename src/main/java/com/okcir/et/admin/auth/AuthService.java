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
