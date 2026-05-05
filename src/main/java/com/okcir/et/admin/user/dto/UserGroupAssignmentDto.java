package com.okcir.et.admin.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupAssignmentDto {

  @NotEmpty(message = "At least one group ID is required")
  private Set<Long> groupIds;
}
