package com.okcir.et.admin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
