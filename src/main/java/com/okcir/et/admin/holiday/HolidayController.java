package com.okcir.et.admin.holiday;

import com.okcir.et.admin.common.ApiResponse;
import com.okcir.et.admin.holiday.dto.HolidayRequestDto;
import com.okcir.et.admin.holiday.dto.HolidayResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/holidays")
@RequiredArgsConstructor
public class HolidayController {

  private final HolidayService holidayService;

  // ── POST / ───────────────────────────────────────────
  @PostMapping
  public ResponseEntity<ApiResponse<HolidayResponseDto>> createHoliday(
      @Valid @RequestBody HolidayRequestDto request) {
    HolidayResponseDto created = holidayService.createHoliday(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(created));
  }

  // ── GET / ────────────────────────────────────────────
  @GetMapping
  public ResponseEntity<ApiResponse<List<HolidayResponseDto>>> getAllHolidays() {
    List<HolidayResponseDto> holidays = holidayService.getAllHolidays();
    return ResponseEntity.ok(ApiResponse.success(holidays));
  }

  // ── GET /{id} ────────────────────────────────────────
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<HolidayResponseDto>> getHolidayById(
      @PathVariable Long id) {
    HolidayResponseDto holiday = holidayService.getHolidayById(id);
    return ResponseEntity.ok(ApiResponse.success(holiday));
  }

  // ── GET /currency/{currencyId} ───────────────────────
  @GetMapping("/currency/{currencyId}")
  public ResponseEntity<ApiResponse<List<HolidayResponseDto>>> getHolidaysByCurrencyId(
      @PathVariable Long currencyId) {
    List<HolidayResponseDto> holidays = holidayService.getHolidaysByCurrencyId(currencyId);
    return ResponseEntity.ok(ApiResponse.success(holidays));
  }

  // ── PUT /{id} ────────────────────────────────────────
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<HolidayResponseDto>> updateHoliday(
      @PathVariable Long id,
      @Valid @RequestBody HolidayRequestDto request) {
    HolidayResponseDto updated = holidayService.updateHoliday(id, request);
    return ResponseEntity.ok(ApiResponse.success("Holiday updated successfully", updated));
  }

  // ── DELETE /{id} ─────────────────────────────────────
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable Long id) {
    holidayService.deleteHoliday(id);
    return ResponseEntity.ok(ApiResponse.success("Holiday deleted successfully", null));
  }
}
