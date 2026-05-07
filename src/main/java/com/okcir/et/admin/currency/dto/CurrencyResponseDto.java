package com.okcir.et.admin.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyResponseDto {

  private Long id;
  private String code;
  private String name;
  private String symbol;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
