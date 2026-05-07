package com.okcir.et.admin.currency;

import com.okcir.et.admin.common.ApiResponse;
import com.okcir.et.admin.currency.dto.CurrencyPairRequestDto;
import com.okcir.et.admin.currency.dto.CurrencyPairResponseDto;
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
@RequestMapping("/api/admin/currency-pairs")
@RequiredArgsConstructor
public class CurrencyPairController {

  private final CurrencyPairService currencyPairService;

  // ── POST / ───────────────────────────────────────────
  @PostMapping
  public ResponseEntity<ApiResponse<CurrencyPairResponseDto>> createCurrencyPair(
      @Valid @RequestBody CurrencyPairRequestDto request) {
    CurrencyPairResponseDto created = currencyPairService.createCurrencyPair(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(created));
  }

  // ── GET /{id} ────────────────────────────────────────
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CurrencyPairResponseDto>> getCurrencyPairById(
      @PathVariable Long id) {
    CurrencyPairResponseDto currencyPair = currencyPairService.getCurrencyPairById(id);
    return ResponseEntity.ok(ApiResponse.success(currencyPair));
  }

  // ── GET / ────────────────────────────────────────────
  @GetMapping
  public ResponseEntity<ApiResponse<List<CurrencyPairResponseDto>>> getAllCurrencyPairs() {
    List<CurrencyPairResponseDto> currencyPairs = currencyPairService.getAllCurrencyPairs();
    return ResponseEntity.ok(ApiResponse.success(currencyPairs));
  }

  // ── PUT /{id} ────────────────────────────────────────
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CurrencyPairResponseDto>> updateCurrencyPair(
      @PathVariable Long id,
      @Valid @RequestBody CurrencyPairRequestDto request) {
    CurrencyPairResponseDto updated = currencyPairService.updateCurrencyPair(id, request);
    return ResponseEntity.ok(ApiResponse.success("Currency pair updated successfully", updated));
  }

  // ── DELETE /{id} ─────────────────────────────────────
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCurrencyPair(@PathVariable Long id) {
    currencyPairService.deleteCurrencyPair(id);
    return ResponseEntity.ok(ApiResponse.success("Currency pair deleted successfully", null));
  }
}
