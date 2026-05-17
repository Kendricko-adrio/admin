package com.okcir.et.admin.user;

import com.okcir.et.admin.common.exception.DuplicateResourceException;
import com.okcir.et.admin.common.exception.ResourceNotFoundException;
import com.okcir.et.admin.group.Group;
import com.okcir.et.admin.group.GroupRepository;
import com.okcir.et.admin.user.dto.UserRequestDto;
import com.okcir.et.admin.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  // ── CREATE ───────────────────────────────────────────

  @Transactional
  public UserResponseDto createUser(UserRequestDto request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException(
          "Username '" + request.getUsername() + "' is already taken");
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException(
          "Email '" + request.getEmail() + "' is already registered");
    }

    String groupName = request.getGroup();
    if (groupName == null || groupName.isBlank()) {
      throw new IllegalArgumentException("Group name is required");
    }

    Group group = groupRepository.findByName(groupName)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Group not found with name: " + groupName));

    User user = User.builder()
        .username(request.getUsername())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .build();
    user.getGroups().add(group);

    User saved = userRepository.save(user);
    return toResponseDto(saved);
  }

  // ── READ (single) ────────────────────────────────────

  @Transactional(readOnly = true)
  public UserResponseDto getUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
    return toResponseDto(user);
  }

  // ── READ (list) ──────────────────────────────────────

  @Transactional(readOnly = true)
  public List<UserResponseDto> getAllUsers() {
    return userRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<UserResponseDto> searchUsers(String username) {
    if (username == null || username.isBlank()) {
      return getAllUsers();
    }
    return userRepository.findByUsernameContainingIgnoreCase(username).stream()
        .map(this::toResponseDto)
        .toList();
  }

  // ── UPDATE ───────────────────────────────────────────

  @Transactional
  public UserResponseDto updateUser(Long id, UserRequestDto request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));

    // Check username uniqueness, excluding the current user
    if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
      throw new DuplicateResourceException(
          "Username '" + request.getUsername() + "' is already taken");
    }
    // Check email uniqueness, excluding the current user
    if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
      throw new DuplicateResourceException(
          "Email '" + request.getEmail() + "' is already registered");
    }

    user.setUsername(request.getUsername());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());

    String groupName = request.getGroup();
    if (groupName != null && !groupName.isBlank()) {
      Group group = groupRepository.findByName(groupName)
          .orElseThrow(() -> new ResourceNotFoundException(
              "Group not found with name: " + groupName));
      user.setGroups(new HashSet<>(Set.of(group)));
    }

    User updated = userRepository.save(user);
    return toResponseDto(updated);
  }

  // ── DELETE ────────────────────────────────────────────

  @Transactional
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User", id);
    }
    userRepository.deleteById(id);
  }

  // ── GROUP ASSIGNMENT ─────────────────────────────────

  @Transactional
  public UserResponseDto assignGroupsToUser(Long userId, Set<Long> groupIds) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", userId));

    List<Group> groups = groupRepository.findAllById(groupIds);

    if (groups.size() != groupIds.size()) {
      Set<Long> foundIds = new HashSet<>();
      for (Group g : groups) {
        foundIds.add(g.getId());
      }
      Set<Long> missingIds = new HashSet<>(groupIds);
      missingIds.removeAll(foundIds);
      throw new ResourceNotFoundException(
          "Group(s) not found with id(s): " + missingIds);
    }

    user.setGroups(new HashSet<>(groups));
    User updated = userRepository.save(user);
    return toResponseDto(updated);
  }

  // ── Mapper ───────────────────────────────────────────

  private UserResponseDto toResponseDto(User user) {
    Set<String> groupNames = user.getGroups() == null
        ? new HashSet<>()
        : user.getGroups().stream()
            .map(Group::getName)
            .collect(Collectors.toSet());

    return UserResponseDto.builder()
        .id(user.getId())
        .username(user.getUsername())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .groups(groupNames)
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
