package com.okcir.et.admin.currency.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyPairRequestDto {

  @NotNull(message = "Base currency ID is required")
  private Long baseCurrencyId;

  @NotNull(message = "Quote currency ID is required")
  private Long quoteCurrencyId;

  private BigDecimal rate;
}
