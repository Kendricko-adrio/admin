package com.okcir.et.admin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private Set<String> groups;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
