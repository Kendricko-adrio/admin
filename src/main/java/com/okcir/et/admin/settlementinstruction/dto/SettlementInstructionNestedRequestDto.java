package com.okcir.et.admin.settlementinstruction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInstructionNestedRequestDto {

  @NotNull(message = "Currency ID is required")
  private Long ccyId;

  @NotBlank(message = "Settlement number is required")
  @Size(max = 100, message = "Settlement number must not exceed 100 characters")
  private String settlementNumber;

  @Builder.Default
  private Boolean isDefault = false;
}
