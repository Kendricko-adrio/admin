package com.okcir.et.admin.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequestDto {

  @NotBlank(message = "Group name is required")
  @Size(min = 2, max = 100, message = "Group name must be between 2 and 100 characters")
  private String name;

  private Set<Long> accessRightIds;

  private Set<Long> accountIds;
}
