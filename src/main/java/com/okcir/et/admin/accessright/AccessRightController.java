package com.okcir.et.admin.accessright;

import com.okcir.et.admin.accessright.dto.AccessRightResponseDto;
import com.okcir.et.admin.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/access-rights")
@RequiredArgsConstructor
public class AccessRightController {

  private final AccessRightService accessRightService;

  // ── GET / ────────────────────────────────────────────
  @GetMapping
  public ResponseEntity<ApiResponse<List<AccessRightResponseDto>>> getAllAccessRights() {
    List<AccessRightResponseDto> accessRights = accessRightService.getAllAccessRights();
    return ResponseEntity.ok(ApiResponse.success(accessRights));
  }
}
