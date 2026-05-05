package com.okcir.et.admin.accessright;

import com.okcir.et.admin.accessright.dto.AccessRightResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessRightService {

  private final AccessRightRepository accessRightRepository;

  @Transactional(readOnly = true)
  public List<AccessRightResponseDto> getAllAccessRights() {
    return accessRightRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  // ── Mapper ───────────────────────────────────────────

  private AccessRightResponseDto toResponseDto(AccessRight accessRight) {
    return AccessRightResponseDto.builder()
        .id(accessRight.getId())
        .code(accessRight.getCode())
        .description(accessRight.getDescription())
        .build();
  }
}
