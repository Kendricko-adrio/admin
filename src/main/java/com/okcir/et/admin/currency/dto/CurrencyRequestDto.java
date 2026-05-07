package com.okcir.et.admin.currency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRequestDto {

  @NotBlank(message = "Currency code is required")
  @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
  private String code;

  @NotBlank(message = "Currency name is required")
  @Size(max = 100, message = "Currency name must not exceed 100 characters")
  private String name;

  @Size(max = 10, message = "Currency symbol must not exceed 10 characters")
  private String symbol;
}
