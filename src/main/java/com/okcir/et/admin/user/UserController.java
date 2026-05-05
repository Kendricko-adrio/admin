package com.okcir.et.admin.user;

import com.okcir.et.admin.common.ApiResponse;
import com.okcir.et.admin.user.dto.UserGroupAssignmentDto;
import com.okcir.et.admin.user.dto.UserRequestDto;
import com.okcir.et.admin.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  // ── POST / ───────────────────────────────────────────
  @PostMapping
  public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
      @Valid @RequestBody UserRequestDto request) {
    UserResponseDto created = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(created));
  }

  // ── GET /{id} ────────────────────────────────────────
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
      @PathVariable Long id) {
    UserResponseDto user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  // ── GET / ────────────────────────────────────────────
  @GetMapping
  public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers(
      @org.springframework.web.bind.annotation.RequestParam(required = false) String username) {
    List<UserResponseDto> users = userService.searchUsers(username);
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  // ── PUT /{id} ────────────────────────────────────────
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UserRequestDto request) {
    UserResponseDto updated = userService.updateUser(id, request);
    return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
  }

  // ── DELETE /{id} ─────────────────────────────────────
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
  }

  // ── PUT /{userId}/groups ─────────────────────────────
  @PutMapping("/{userId}/groups")
  public ResponseEntity<ApiResponse<UserResponseDto>> assignGroupsToUser(
      @PathVariable Long userId,
      @Valid @RequestBody UserGroupAssignmentDto request) {
    UserResponseDto updated = userService.assignGroupsToUser(userId, request.getGroupIds());
    return ResponseEntity.ok(ApiResponse.success("Groups assigned successfully", updated));
  }
}
