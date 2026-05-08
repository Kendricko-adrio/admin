package com.okcir.et.admin.holiday.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayRequestDto {

  @NotNull(message = "Currency ID is required")
  private Long currencyId;

  @NotBlank(message = "Holiday type is required")
  private String type;

  private LocalDate holidayDate;

  @Size(max = 10, message = "dayOfWeek must not exceed 10 characters")
  private String dayOfWeek;
}
