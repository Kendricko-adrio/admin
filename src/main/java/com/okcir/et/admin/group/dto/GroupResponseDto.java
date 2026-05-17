package com.okcir.et.admin.group.dto;

import com.okcir.et.admin.accessright.dto.AccessRightResponseDto;
import com.okcir.et.admin.account.dto.AccountSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDto {

  private Long id;
  private String name;
  private List<AccessRightResponseDto> accessRights;
  private List<AccountSummaryDto> accounts;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
