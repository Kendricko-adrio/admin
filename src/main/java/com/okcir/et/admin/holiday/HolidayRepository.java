package com.okcir.et.admin.holiday;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

  List<Holiday> findByCurrencyId(Long currencyId);

  boolean existsByCurrencyIdAndTypeAndHolidayDate(
      Long currencyId, HolidayType type, LocalDate holidayDate);

  boolean existsByCurrencyIdAndTypeAndDayOfWeek(
      Long currencyId, HolidayType type, String dayOfWeek);

  boolean existsByCurrencyIdAndTypeAndHolidayDateAndIdNot(
      Long currencyId, HolidayType type, LocalDate holidayDate, Long id);

  boolean existsByCurrencyIdAndTypeAndDayOfWeekAndIdNot(
      Long currencyId, HolidayType type, String dayOfWeek, Long id);
}
