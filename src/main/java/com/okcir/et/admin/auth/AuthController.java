package com.okcir.et.admin.auth;

import com.okcir.et.admin.auth.dto.LoginRequest;
import com.okcir.et.admin.auth.dto.LoginResponse;
import com.okcir.et.admin.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  // ── POST /api/auth/login ──────────────────────────────
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
  }
}
