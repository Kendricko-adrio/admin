package com.okcir.et.admin.currency;

import com.okcir.et.admin.common.exception.DuplicateResourceException;
import com.okcir.et.admin.common.exception.ResourceNotFoundException;
import com.okcir.et.admin.currency.dto.CurrencyPairRequestDto;
import com.okcir.et.admin.currency.dto.CurrencyPairResponseDto;
import com.okcir.et.admin.currency.dto.CurrencyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyPairService {

  private final CurrencyPairRepository currencyPairRepository;
  private final CurrencyRepository currencyRepository;

  // ── CREATE ───────────────────────────────────────────

  @Transactional
  public CurrencyPairResponseDto createCurrencyPair(CurrencyPairRequestDto request) {
    if (request.getBaseCurrencyId().equals(request.getQuoteCurrencyId())) {
      throw new DuplicateResourceException(
          "Base currency and quote currency must be different");
    }

    Currency baseCurrency = currencyRepository.findById(request.getBaseCurrencyId())
        .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getBaseCurrencyId()));
    Currency quoteCurrency = currencyRepository.findById(request.getQuoteCurrencyId())
        .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getQuoteCurrencyId()));

    if (currencyPairRepository.existsByBaseCurrencyIdAndQuoteCurrencyId(
        request.getBaseCurrencyId(), request.getQuoteCurrencyId())) {
      throw new DuplicateResourceException(
          "Currency pair already exists for base currency " + baseCurrency.getCode()
              + " and quote currency " + quoteCurrency.getCode());
    }

    String pairCode = baseCurrency.getCode() + quoteCurrency.getCode();

    CurrencyPair currencyPair = CurrencyPair.builder()
        .baseCurrency(baseCurrency)
        .quoteCurrency(quoteCurrency)
        .pairCode(pairCode)
        .rate(request.getRate())
        .build();

    CurrencyPair saved = currencyPairRepository.save(currencyPair);
    return toResponseDto(saved);
  }

  // ── READ (single) ────────────────────────────────────

  @Transactional(readOnly = true)
  public CurrencyPairResponseDto getCurrencyPairById(Long id) {
    CurrencyPair currencyPair = currencyPairRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("CurrencyPair", id));
    return toResponseDto(currencyPair);
  }

  // ── READ (list) ──────────────────────────────────────

  @Transactional(readOnly = true)
  public List<CurrencyPairResponseDto> getAllCurrencyPairs() {
    return currencyPairRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  // ── UPDATE ───────────────────────────────────────────

  @Transactional
  public CurrencyPairResponseDto updateCurrencyPair(Long id, CurrencyPairRequestDto request) {
    CurrencyPair currencyPair = currencyPairRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("CurrencyPair", id));

    if (request.getBaseCurrencyId().equals(request.getQuoteCurrencyId())) {
      throw new DuplicateResourceException(
          "Base currency and quote currency must be different");
    }

    Currency baseCurrency = currencyRepository.findById(request.getBaseCurrencyId())
        .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getBaseCurrencyId()));
    Currency quoteCurrency = currencyRepository.findById(request.getQuoteCurrencyId())
        .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getQuoteCurrencyId()));

    boolean baseChanged = !currencyPair.getBaseCurrency().getId().equals(request.getBaseCurrencyId());
    boolean quoteChanged = !currencyPair.getQuoteCurrency().getId().equals(request.getQuoteCurrencyId());

    if (baseChanged || quoteChanged) {
      if (currencyPairRepository.existsByBaseCurrencyIdAndQuoteCurrencyIdAndIdNot(
          request.getBaseCurrencyId(), request.getQuoteCurrencyId(), id)) {
        throw new DuplicateResourceException(
            "Currency pair already exists for base currency " + baseCurrency.getCode()
                + " and quote currency " + quoteCurrency.getCode());
      }
    }

    String pairCode = baseCurrency.getCode() + quoteCurrency.getCode();

    currencyPair.setBaseCurrency(baseCurrency);
    currencyPair.setQuoteCurrency(quoteCurrency);
    currencyPair.setPairCode(pairCode);
    currencyPair.setRate(request.getRate());

    CurrencyPair updated = currencyPairRepository.save(currencyPair);
    return toResponseDto(updated);
  }

  // ── DELETE ───────────────────────────────────────────

  @Transactional
  public void deleteCurrencyPair(Long id) {
    if (!currencyPairRepository.existsById(id)) {
      throw new ResourceNotFoundException("CurrencyPair", id);
    }
    currencyPairRepository.deleteById(id);
  }

  // ── Mapper ───────────────────────────────────────────

  private CurrencyPairResponseDto toResponseDto(CurrencyPair currencyPair) {
    return CurrencyPairResponseDto.builder()
        .id(currencyPair.getId())
        .pairCode(currencyPair.getPairCode())
        .rate(currencyPair.getRate())
        .baseCurrency(toCurrencyResponseDto(currencyPair.getBaseCurrency()))
        .quoteCurrency(toCurrencyResponseDto(currencyPair.getQuoteCurrency()))
        .createdAt(currencyPair.getCreatedAt())
        .updatedAt(currencyPair.getUpdatedAt())
        .build();
  }

  private CurrencyResponseDto toCurrencyResponseDto(Currency currency) {
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
