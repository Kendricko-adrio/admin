package com.okcir.et.admin.currency;

import com.okcir.et.admin.common.exception.DuplicateResourceException;
import com.okcir.et.admin.common.exception.ResourceNotFoundException;
import com.okcir.et.admin.currency.dto.CurrencyRequestDto;
import com.okcir.et.admin.currency.dto.CurrencyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {

  private final CurrencyRepository currencyRepository;

  // ── CREATE ───────────────────────────────────────────

  @Transactional
  public CurrencyResponseDto createCurrency(CurrencyRequestDto request) {
    String normalizedCode = request.getCode().toUpperCase();

    if (currencyRepository.existsByCode(normalizedCode)) {
      throw new DuplicateResourceException(
          "Currency code already exists: " + normalizedCode);
    }

    Currency currency = Currency.builder()
        .code(normalizedCode)
        .name(request.getName())
        .symbol(request.getSymbol())
        .build();

    Currency saved = currencyRepository.save(currency);
    return toResponseDto(saved);
  }

  // ── READ (single) ────────────────────────────────────

  @Transactional(readOnly = true)
  public CurrencyResponseDto getCurrencyById(Long id) {
    Currency currency = currencyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    return toResponseDto(currency);
  }

  // ── READ (list) ──────────────────────────────────────

  @Transactional(readOnly = true)
  public List<CurrencyResponseDto> getAllCurrencies() {
    return currencyRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  // ── UPDATE ───────────────────────────────────────────

  @Transactional
  public CurrencyResponseDto updateCurrency(Long id, CurrencyRequestDto request) {
    Currency currency = currencyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Currency", id));

    String normalizedCode = request.getCode().toUpperCase();

    if (currencyRepository.existsByCodeAndIdNot(normalizedCode, id)) {
      throw new DuplicateResourceException(
          "Currency code already exists: " + normalizedCode);
    }

    currency.setCode(normalizedCode);
    currency.setName(request.getName());
    currency.setSymbol(request.getSymbol());

    Currency updated = currencyRepository.save(currency);
    return toResponseDto(updated);
  }

  // ── DELETE ───────────────────────────────────────────

  @Transactional
  public void deleteCurrency(Long id) {
    if (!currencyRepository.existsById(id)) {
      throw new ResourceNotFoundException("Currency", id);
    }
    currencyRepository.deleteById(id);
  }

  // ── Mapper ───────────────────────────────────────────

  private CurrencyResponseDto toResponseDto(Currency currency) {
    return CurrencyResponseDto.builder()
        .id(currency.getId())
        .code(currency.getCode())
        .name(currency.getName())
        .symbol(currency.getSymbol())
        .createdAt(currency.getCreatedAt())
        .updatedAt(currency.getUpdatedAt())
        .build();
  }
}
