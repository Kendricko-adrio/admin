# Holiday Feature — Implementation Plan

> **Branch:** `dev/holiday` (already created)  
> **Date:** 2026-05-08  
> **Model for implementation:** opencode-go/kimi-k2.6

---

## 1. Business Requirements

Holiday feature binds holidays and rest days to currencies. Two types:

| Type | Meaning | Example |
|------|---------|---------|
| `HOLIDAY` | A specific calendar date when a currency's market is closed | IDR holiday on **2026-01-23** |
| `REST_DAY` | A recurring day-of-week when a currency's market is closed | USD rest on **SATURDAY** and **SUNDAY** |

A currency can have **multiple** holidays and rest days.

---

## 2. Database Design

### Migration: `V5__Create_Holidays_Table.sql`

```sql
CREATE TABLE holidays (
    id           BIGSERIAL    PRIMARY KEY,
    currency_id  BIGINT       NOT NULL,
    type         VARCHAR(10)  NOT NULL,  -- 'HOLIDAY' or 'REST_DAY'
    holiday_date DATE,                   -- nullable, used when type = HOLIDAY
    day_of_week  VARCHAR(10),            -- nullable, used when type = REST_DAY
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_holidays_currency
        FOREIGN KEY (currency_id) REFERENCES currencies(id) ON DELETE CASCADE,
    CONSTRAINT uq_holidays_currency_date
        UNIQUE (currency_id, type, holiday_date),
    CONSTRAINT uq_holidays_currency_day
        UNIQUE (currency_id, type, day_of_week)
);

CREATE INDEX idx_holidays_currency_id ON holidays (currency_id);
CREATE INDEX idx_holidays_type ON holidays (type);
```

**Notes:**
- `holiday_date` is `NULL` when type = `REST_DAY`
- `day_of_week` is `NULL` when type = `HOLIDAY`
- Two partial unique constraints prevent duplicates for each type
- `ON DELETE CASCADE` — deleting a currency removes its holidays
- Path: `src/main/resources/db/migration/V5__Create_Holidays_Table.sql`

---

## 3. Java Classes

All classes under package `com.okcir.et.admin.holiday`.

### 3.1 Enum: `HolidayType.java`

```java
package com.okcir.et.admin.holiday;

public enum HolidayType {
    HOLIDAY,
    REST_DAY
}
```

Straightforward enum, no Lombok needed.

### 3.2 Entity: `Holiday.java`

Follows `Currency.java` pattern exactly:

- `@Entity` + `@Table(name = "holidays")`
- `@EntityListeners(AuditingEntityListener.class)`
- `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- Fields:
  - `id` — `Long`, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
  - `currency` — `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "currency_id", nullable = false)`
  - `type` — `@Enumerated(EnumType.STRING)`, `@Column(nullable = false, length = 10)`
  - `holidayDate` — `LocalDate`, `@Column(name = "holiday_date")`
  - `dayOfWeek` — `String`, `@Column(name = "day_of_week", length = 10)`
  - `createdAt` — `LocalDateTime`, `@CreatedDate @Column(name = "created_at", nullable = false, updatable = false)`
  - `updatedAt` — `LocalDateTime`, `@LastModifiedDate @Column(name = "updated_at", nullable = false)`

### 3.3 DTOs

**`HolidayRequestDto.java`** — Request body for create/update:
- `currencyId` — `Long`, `@NotNull(message = "Currency ID is required")`
- `type` — `String`, `@NotBlank(message = "Holiday type is required")`
- `holidayDate` — `LocalDate` (optional)
- `dayOfWeek` — `String`, `@Size(max = 10)` (optional)

**Custom validation logic** (handled in service, not annotation):
- If `type = "HOLIDAY"` → `holidayDate` is required, `dayOfWeek` ignored
- If `type = "REST_DAY"` → `dayOfWeek` is required, `holidayDate` ignored
- `dayOfWeek` must be a valid value: `MONDAY`–`SUNDAY`

**`HolidayResponseDto.java`** — Response body:
- `id` — `Long`
- `currencyId` — `Long`
- `currencyCode` — `String` (from `currency.getCode()`)
- `type` — `String`
- `holidayDate` — `LocalDate`
- `dayOfWeek` — `String`
- `createdAt` — `LocalDateTime`
- `updatedAt` — `LocalDateTime`

Package: `com.okcir.et.admin.holiday.dto`

### 3.4 Repository: `HolidayRepository.java`

```java
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    
    List<Holiday> findByCurrencyId(Long currencyId);
    
    boolean existsByCurrencyIdAndTypeAndHolidayDate(
        Long currencyId, HolidayType type, LocalDate holidayDate);
    
    boolean existsByCurrencyIdAndTypeAndDayOfWeek(
        Long currencyId, HolidayType type, String dayOfWeek);
    
    // For update duplicate check (exclude current holiday)
    boolean existsByCurrencyIdAndTypeAndHolidayDateAndIdNot(
        Long currencyId, HolidayType type, LocalDate holidayDate, Long id);
    
    boolean existsByCurrencyIdAndTypeAndDayOfWeekAndIdNot(
        Long currencyId, HolidayType type, String dayOfWeek, Long id);
}
```

### 3.5 Service: `HolidayService.java`

Follows `CurrencyService.java` pattern:

| Method | Transaction | Description |
|--------|-------------|-------------|
| `createHoliday(request)` | `@Transactional` | Validate type → check duplicate → build entity → save → return response |
| `getHolidayById(id)` | `readOnly` | Find or throw `ResourceNotFoundException("Holiday", id)` |
| `getAllHolidays()` | `readOnly` | Return all, mapped to response DTOs |
| `getHolidaysByCurrencyId(currencyId)` | `readOnly` | Filter by currency, verify currency exists |
| `updateHoliday(id, request)` | `@Transactional` | Find → validate type → check duplicate excluding self → update fields → save |
| `deleteHoliday(id)` | `@Transactional` | Check exists → `deleteById` |

**Dependencies:** `HolidayRepository` + `CurrencyRepository` (to validate currencyId)

**Validation in service:**
```java
private void validateType(HolidayRequestDto request) {
    HolidayType type = HolidayType.valueOf(request.getType().toUpperCase());
    if (type == HolidayType.HOLIDAY && request.getHolidayDate() == null) {
        throw new IllegalArgumentException("holidayDate is required for HOLIDAY type");
    }
    if (type == HolidayType.REST_DAY && request.getDayOfWeek() == null) {
        throw new IllegalArgumentException("dayOfWeek is required for REST_DAY type");
    }
    // Validate dayOfWeek values (MONDAY–SUNDAY)
}
```

**Mapper:**
```java
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
```

### 3.6 Controller: `HolidayController.java`

Follows `CurrencyController.java` pattern:

| Method | Endpoint | Response |
|--------|----------|----------|
| `POST` | `/api/admin/holidays` | `201 CREATED` with `ApiResponse.created(data)` |
| `GET` | `/api/admin/holidays` | `200 OK` with `ApiResponse.success(list)` |
| `GET` | `/api/admin/holidays/{id}` | `200 OK` with `ApiResponse.success(data)` |
| `GET` | `/api/admin/holidays/currency/{currencyId}` | `200 OK` with `ApiResponse.success(list)` |
| `PUT` | `/api/admin/holidays/{id}` | `200 OK` with `ApiResponse.success(message, data)` |
| `DELETE` | `/api/admin/holidays/{id}` | `200 OK` with `ApiResponse.success(message, null)` |

All return `ResponseEntity<ApiResponse<T>>`. Uses `@Valid @RequestBody` for POST/PUT.

---

## 4. Error Handling

Uses existing exceptions from `com.okcir.et.admin.common.exception`:

| Exception | When |
|-----------|------|
| `ResourceNotFoundException` | Currency not found, Holiday not found |
| `DuplicateResourceException` | Duplicate holiday date or rest day for same currency |
| `IllegalArgumentException` | Invalid type, missing required fields per type |
| `MethodArgumentNotValidException` | DTO validation failures (handled by `GlobalExceptionHandler` → 400) |

**Note:** `IllegalArgumentException` needs a new handler in `GlobalExceptionHandler`:

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.badRequest()
        .body(ApiResponse.error(400, ex.getMessage()));
}
```

---

## 5. Complete File List

### New files (10 files):

```
src/main/resources/db/migration/V5__Create_Holidays_Table.sql
src/main/java/com/okcir/et/admin/holiday/HolidayType.java
src/main/java/com/okcir/et/admin/holiday/Holiday.java
src/main/java/com/okcir/et/admin/holiday/HolidayRepository.java
src/main/java/com/okcir/et/admin/holiday/HolidayService.java
src/main/java/com/okcir/et/admin/holiday/HolidayController.java
src/main/java/com/okcir/et/admin/holiday/dto/HolidayRequestDto.java
src/main/java/com/okcir/et/admin/holiday/dto/HolidayResponseDto.java
```

### Modified files (1 file):

```
src/main/java/com/okcir/et/admin/common/GlobalExceptionHandler.java
  — Add @ExceptionHandler for IllegalArgumentException (400)
```

---

## 6. Implementation Order

1. **DB Migration** — `V5__Create_Holidays_Table.sql`
2. **Enum** — `HolidayType.java`
3. **Entity** — `Holiday.java`
4. **DTOs** — `HolidayRequestDto.java`, `HolidayResponseDto.java`
5. **Repository** — `HolidayRepository.java`
6. **Service** — `HolidayService.java`
7. **Controller** — `HolidayController.java`
8. **Exception Handler** — Patch `GlobalExceptionHandler.java`
9. **Compile check** — `./mvnw compile`
