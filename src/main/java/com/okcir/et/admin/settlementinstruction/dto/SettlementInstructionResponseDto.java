package com.okcir.et.admin.settlementinstruction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInstructionResponseDto {

  private Long id;
  private Long accountId;
  private Long ccyId;
  private String settlementNumber;
  private Boolean isDefault;
  private String currencyCode;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
