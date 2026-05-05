package com.okcir.et.admin.group;

import com.okcir.et.admin.common.ApiResponse;
import com.okcir.et.admin.group.dto.GroupRequestDto;
import com.okcir.et.admin.group.dto.GroupResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class GroupController {

  private final GroupService groupService;

  // ── POST / ───────────────────────────────────────────
  @PostMapping
  public ResponseEntity<ApiResponse<GroupResponseDto>> createGroup(
      @Valid @RequestBody GroupRequestDto request) {
    GroupResponseDto created = groupService.createGroup(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(created));
  }

  // ── GET / ────────────────────────────────────────────
  @GetMapping
  public ResponseEntity<ApiResponse<List<GroupResponseDto>>> getAllGroups() {
    List<GroupResponseDto> groups = groupService.getAllGroups();
    return ResponseEntity.ok(ApiResponse.success(groups));
  }

  // ── PUT /{id} ────────────────────────────────────────
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<GroupResponseDto>> updateGroup(
      @PathVariable Long id,
      @Valid @RequestBody GroupRequestDto request) {
    GroupResponseDto updated = groupService.updateGroup(id, request);
    return ResponseEntity.ok(ApiResponse.success("Group updated successfully", updated));
  }
}
