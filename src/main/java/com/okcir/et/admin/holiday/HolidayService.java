package com.okcir.et.admin.holiday;

import com.okcir.et.admin.common.exception.DuplicateResourceException;
import com.okcir.et.admin.common.exception.ResourceNotFoundException;
import com.okcir.et.admin.currency.Currency;
import com.okcir.et.admin.currency.CurrencyRepository;
import com.okcir.et.admin.holiday.dto.HolidayRequestDto;
import com.okcir.et.admin.holiday.dto.HolidayResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

  private final HolidayRepository holidayRepository;
  private final CurrencyRepository currencyRepository;

  // ── CREATE ───────────────────────────────────────────

  @Transactional
  public HolidayResponseDto createHoliday(HolidayRequestDto request) {
    HolidayType type = validateType(request);

    Currency currency = currencyRepository.findById(request.getCurrencyId())
        .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCurrencyId()));

    checkDuplicate(request.getCurrencyId(), type, request.getHolidayDate(), request.getDayOfWeek(), null);

    Holiday holiday = Holiday.builder()
        .currency(currency)
        .type(type)
        .holidayDate(request.getHolidayDate())
        .dayOfWeek(request.getDayOfWeek())
        .build();

    Holiday saved = holidayRepository.save(holiday);
    return toResponseDto(saved);
  }

  // ── READ (single) ────────────────────────────────────

  @Transactional(readOnly = true)
  public HolidayResponseDto getHolidayById(Long id) {
    Holiday holiday = holidayRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Holiday", id));
    return toResponseDto(holiday);
  }

  // ── READ (all) ───────────────────────────────────────

  @Transactional(readOnly = true)
  public List<HolidayResponseDto> getAllHolidays() {
    return holidayRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  // ── READ (by currency) ───────────────────────────────

  @Transactional(readOnly = true)
  public List<HolidayResponseDto> getHolidaysByCurrencyId(Long currencyId) {
    if (!currencyRepository.existsById(currencyId)) {
      throw new ResourceNotFoundException("Currency", currencyId);
    }
    return holidayRepository.findByCurrencyId(currencyId).stream()
        .map(this::toResponseDto)
        .toList();
  }

  // ── UPDATE ───────────────────────────────────────────

  @Transactional
  public HolidayResponseDto updateHoliday(Long id, HolidayRequestDto request) {
    Holiday holiday = holidayRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Holiday", id));

    HolidayType type = validateType(request);

    Currency currency = currencyRepository.findById(request.getCurrencyId())
        .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCurrencyId()));

    checkDuplicate(request.getCurrencyId(), type, request.getHolidayDate(), request.getDayOfWeek(), id);

    holiday.setCurrency(currency);
    holiday.setType(type);
    holiday.setHolidayDate(request.getHolidayDate());
    holiday.setDayOfWeek(request.getDayOfWeek());

    Holiday updated = holidayRepository.save(holiday);
    return toResponseDto(updated);
  }

  // ── DELETE ───────────────────────────────────────────

  @Transactional
  public void deleteHoliday(Long id) {
    if (!holidayRepository.existsById(id)) {
      throw new ResourceNotFoundException("Holiday", id);
    }
    holidayRepository.deleteById(id);
  }

  // ── Validation ───────────────────────────────────────

  private HolidayType validateType(HolidayRequestDto request) {
    HolidayType type;
    try {
      type = HolidayType.valueOf(request.getType().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid holiday type: " + request.getType());
    }

    if (type == HolidayType.HOLIDAY && request.getHolidayDate() == null) {
      throw new IllegalArgumentException("holidayDate is required for HOLIDAY type");
    }
    if (type == HolidayType.REST_DAY && request.getDayOfWeek() == null) {
      throw new IllegalArgumentException("dayOfWeek is required for REST_DAY type");
    }
    if (type == HolidayType.REST_DAY && request.getDayOfWeek() != null) {
      try {
        DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("dayOfWeek must be a valid day: MONDAY–SUNDAY");
      }
    }
    return type;
  }

  private void checkDuplicate(Long currencyId, HolidayType type,
      java.time.LocalDate holidayDate, String dayOfWeek, Long excludeId) {
    boolean duplicate;
    if (type == HolidayType.HOLIDAY) {
      duplicate = excludeId == null
          ? holidayRepository.existsByCurrencyIdAndTypeAndHolidayDate(currencyId, type, holidayDate)
          : holidayRepository.existsByCurrencyIdAndTypeAndHolidayDateAndIdNot(currencyId, type, holidayDate, excludeId);
      if (duplicate) {
        throw new DuplicateResourceException(
            "Holiday already exists for currency on date: " + holidayDate);
      }
    } else {
      duplicate = excludeId == null
          ? holidayRepository.existsByCurrencyIdAndTypeAndDayOfWeek(currencyId, type, dayOfWeek)
          : holidayRepository.existsByCurrencyIdAndTypeAndDayOfWeekAndIdNot(currencyId, type, dayOfWeek, excludeId);
      if (duplicate) {
        throw new DuplicateResourceException(
            "Rest day already exists for currency on day: " + dayOfWeek);
      }
    }
  }

  // ── Mapper ───────────────────────────────────────────

  private HolidayResponseDto toResponseDto(Holiday holiday) {
    return HolidayResponseDto.builder()
        .id(holiday.getId())
        .currencyId(holiday.getCurrency().getId())
        .currencyCode(holiday.getCurrency().getCode())
        .type(holiday.getType().name())
        .holidayDate(holiday.getHolidayDate())
        .dayOfWeek(holiday.getDayOfWeek())
        .createdAt(holiday.getCreatedAt())
        .updatedAt(holiday.getUpdatedAt())
        .build();
  }
}
