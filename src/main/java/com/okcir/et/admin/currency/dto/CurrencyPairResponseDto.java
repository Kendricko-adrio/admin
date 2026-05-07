package com.okcir.et.admin.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyPairResponseDto {

  private Long id;
  private String pairCode;
  private BigDecimal rate;
  private CurrencyResponseDto baseCurrency;
  private CurrencyResponseDto quoteCurrency;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
