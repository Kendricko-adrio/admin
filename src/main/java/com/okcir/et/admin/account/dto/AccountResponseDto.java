package com.okcir.et.admin.account.dto;

import com.okcir.et.admin.group.dto.GroupSummaryDto;
import com.okcir.et.admin.settlementinstruction.dto.SettlementInstructionResponseDto;
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
public class AccountResponseDto {

  private Long id;
  private String name;
  private String description;
  private String counterparty;
  private List<GroupSummaryDto> groups;
  private List<SettlementInstructionResponseDto> settlementInstructions;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
