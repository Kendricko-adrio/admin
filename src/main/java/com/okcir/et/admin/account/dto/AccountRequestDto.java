package com.okcir.et.admin.account.dto;

import com.okcir.et.admin.settlementinstruction.dto.SettlementInstructionNestedRequestDto;
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
public class AccountRequestDto {

  @NotBlank(message = "Account name is required")
  @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
  private String name;

  private String description;

  private String counterparty;

  private Set<Long> groupIds;

  @Builder.Default
  private List<SettlementInstructionNestedRequestDto> settlementInstructions = List.of();
}
