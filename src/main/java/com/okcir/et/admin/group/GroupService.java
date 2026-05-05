package com.okcir.et.admin.group;

import com.okcir.et.admin.accessright.AccessRight;
import com.okcir.et.admin.accessright.AccessRightRepository;
import com.okcir.et.admin.accessright.dto.AccessRightResponseDto;
import com.okcir.et.admin.common.exception.DuplicateResourceException;
import com.okcir.et.admin.common.exception.ResourceNotFoundException;
import com.okcir.et.admin.group.dto.GroupRequestDto;
import com.okcir.et.admin.group.dto.GroupResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final AccessRightRepository accessRightRepository;

  // ── CREATE ───────────────────────────────────────────

  @Transactional
  public GroupResponseDto createGroup(GroupRequestDto request) {
    if (groupRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException(
          "Group name '" + request.getName() + "' already exists");
    }

    Set<AccessRight> accessRights = resolveAccessRights(request.getAccessRightIds());

    Group group = Group.builder()
        .name(request.getName())
        .accessRights(accessRights)
        .build();

    Group saved = groupRepository.save(group);
    return toResponseDto(saved);
  }

  // ── READ (list) ──────────────────────────────────────

  @Transactional(readOnly = true)
  public List<GroupResponseDto> getAllGroups() {
    return groupRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  // ── UPDATE ───────────────────────────────────────────

  @Transactional
  public GroupResponseDto updateGroup(Long id, GroupRequestDto request) {
    Group group = groupRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Group", id));

    if (groupRepository.existsByNameAndIdNot(request.getName(), id)) {
      throw new DuplicateResourceException(
          "Group name '" + request.getName() + "' already exists");
    }

    Set<AccessRight> accessRights = resolveAccessRights(request.getAccessRightIds());

    group.setName(request.getName());
    group.setAccessRights(accessRights);

    Group updated = groupRepository.save(group);
    return toResponseDto(updated);
  }

  // ── Helpers ──────────────────────────────────────────

  private Set<AccessRight> resolveAccessRights(Set<Long> accessRightIds) {
    if (accessRightIds == null || accessRightIds.isEmpty()) {
      return new HashSet<>();
    }

    List<AccessRight> found = accessRightRepository.findAllById(accessRightIds);

    if (found.size() != accessRightIds.size()) {
      // Find which IDs are missing
      Set<Long> foundIds = new HashSet<>();
      for (AccessRight ar : found) {
        foundIds.add(ar.getId());
      }
      Set<Long> missingIds = new HashSet<>(accessRightIds);
      missingIds.removeAll(foundIds);
      throw new ResourceNotFoundException(
          "Access Right(s) not found with id(s): " + missingIds);
    }

    return new HashSet<>(found);
  }

  // ── Mapper ───────────────────────────────────────────

  private GroupResponseDto toResponseDto(Group group) {
    List<AccessRightResponseDto> accessRightDtos = group.getAccessRights().stream()
        .map(ar -> AccessRightResponseDto.builder()
            .id(ar.getId())
            .code(ar.getCode())
            .description(ar.getDescription())
            .build())
        .toList();

    return GroupResponseDto.builder()
        .id(group.getId())
        .name(group.getName())
        .accessRights(accessRightDtos)
        .createdAt(group.getCreatedAt())
        .updatedAt(group.getUpdatedAt())
        .build();
  }
}
