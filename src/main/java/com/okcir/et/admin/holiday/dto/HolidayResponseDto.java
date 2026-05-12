package com.okcir.et.admin.holiday.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponseDto {

  private Long id;
  private Long currencyId;
  private String currencyCode;
  private String type;
  private LocalDate holidayDate;
  private String dayOfWeek;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
