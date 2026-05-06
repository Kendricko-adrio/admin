package com.okcir.et.admin.accessright;

import com.okcir.et.admin.accessright.dto.AccessRightResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessRightService {

  private final AccessRightRepository accessRightRepository;

  @Transactional(readOnly = true)
  public List<AccessRightResponseDto> getAllAccessRights() {
    return accessRightRepository.findAllByOrderByCodeAsc().stream()
        .map(this::toResponseDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public Map<String, List<AccessRightResponseDto>> getAccessRightsByCategory() {
    return accessRightRepository.findAllByOrderByCodeAsc().stream()
        .map(this::toResponseDto)
        .collect(Collectors.groupingBy(
            ar -> ar.getCategory() != null ? ar.getCategory() : "UNCATEGORIZED",
            LinkedHashMap::new,
            Collectors.toList()
        ));
  }

  // ── Mapper ───────────────────────────────────────────

  private AccessRightResponseDto toResponseDto(AccessRight accessRight) {
    return AccessRightResponseDto.builder()
        .id(accessRight.getId())
        .code(accessRight.getCode())
        .description(accessRight.getDescription())
        .parentCode(accessRight.getParentCode())
        .category(accessRight.getCategory())
        .build();
  }
}
