package com.okcir.et.admin.currency;

import com.okcir.et.admin.common.ApiResponse;
import com.okcir.et.admin.currency.dto.CurrencyRequestDto;
import com.okcir.et.admin.currency.dto.CurrencyResponseDto;
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
@RequestMapping("/api/admin/currencies")
@RequiredArgsConstructor
public class CurrencyController {

  private final CurrencyService currencyService;

  // ── POST / ───────────────────────────────────────────
  @PostMapping
  public ResponseEntity<ApiResponse<CurrencyResponseDto>> createCurrency(
      @Valid @RequestBody CurrencyRequestDto request) {
    CurrencyResponseDto created = currencyService.createCurrency(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(created));
  }

  // ── GET /{id} ────────────────────────────────────────
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CurrencyResponseDto>> getCurrencyById(
      @PathVariable Long id) {
    CurrencyResponseDto currency = currencyService.getCurrencyById(id);
    return ResponseEntity.ok(ApiResponse.success(currency));
  }

  // ── GET / ────────────────────────────────────────────
  @GetMapping
  public ResponseEntity<ApiResponse<List<CurrencyResponseDto>>> getAllCurrencies() {
    List<CurrencyResponseDto> currencies = currencyService.getAllCurrencies();
    return ResponseEntity.ok(ApiResponse.success(currencies));
  }

  // ── PUT /{id} ────────────────────────────────────────
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CurrencyResponseDto>> updateCurrency(
      @PathVariable Long id,
      @Valid @RequestBody CurrencyRequestDto request) {
    CurrencyResponseDto updated = currencyService.updateCurrency(id, request);
    return ResponseEntity.ok(ApiResponse.success("Currency updated successfully", updated));
  }

  // ── DELETE /{id} ─────────────────────────────────────
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCurrency(@PathVariable Long id) {
    currencyService.deleteCurrency(id);
    return ResponseEntity.ok(ApiResponse.success("Currency deleted successfully", null));
  }
}
