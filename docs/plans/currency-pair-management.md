# Currency Pair Management — Implementation Plan

> **Branch:** `dev/ccy_pair`  
> **Package:** `com.okcir.et.admin`  
> **Scope:** Full CRUD for `Currency` and `CurrencyPair` entities  
> **Constraint:** Stateless JWT-backed architecture; all data mutations must record audit timestamps.

---

## 1. Overview

### Goal
Provide Admin users with REST endpoints to manage the master list of tradable currencies (ISO codes, names, symbols) and the currency pairs that define the Forex dealing universe (e.g., EURUSD). The module must enforce data integrity at the DB and application layers, derive the pair code automatically from the underlying base/quote currencies, and prevent logically invalid pairs (base == quote, duplicate combinations).

### Architecture
- **Persistence layer:** Flyway migration for schema, JPA Entities with `AuditingEntityListener`, Spring Data JPA Repositories.
- **Business layer:** Service classes with `@Transactional` demarcation, private `toResponseDto` mapper methods, and explicit exception translation to `ResourceNotFoundException` / `DuplicateResourceException`.
- **API layer:** REST Controllers under `/api/admin/currencies` and `/api/admin/currency-pairs`, returning the existing `ApiResponse<T>` wrapper.
- **Validation:** Jakarta Bean Validation (`@Valid`) on Request DTOs; additional programmatic checks in Service for cross-field rules (base ≠ quote, uniqueness of pair combination).

### Technology
- Java 21, Spring Boot 4.0.2
- PostgreSQL (primary), Flyway (schema versioning)
- Spring Data JPA, Lombok, Jakarta Validation

---

## 2. Files to Create

### Flyway Migration
- `src/main/resources/db/migration/V4__Create_Currency_And_CurrencyPair_Tables.sql`

### Currency Module
- `src/main/java/com/okcir/et/admin/currency/entity/Currency.java`
- `src/main/java/com/okcir/et/admin/currency/repository/CurrencyRepository.java`
- `src/main/java/com/okcir/et/admin/currency/service/CurrencyService.java`
- `src/main/java/com/okcir/et/admin/currency/controller/CurrencyController.java`
- `src/main/java/com/okcir/et/admin/currency/dto/CreateCurrencyRequest.java`
- `src/main/java/com/okcir/et/admin/currency/dto/UpdateCurrencyRequest.java`
- `src/main/java/com/okcir/et/admin/currency/dto/CurrencyResponse.java`

### CurrencyPair Module
- `src/main/java/com/okcir/et/admin/currency/entity/CurrencyPair.java`
- `src/main/java/com/okcir/et/admin/currency/repository/CurrencyPairRepository.java`
- `src/main/java/com/okcir/et/admin/currency/service/CurrencyPairService.java`
- `src/main/java/com/okcir/et/admin/currency/controller/CurrencyPairController.java`
- `src/main/java/com/okcir/et/admin/currency/dto/CreateCurrencyPairRequest.java`
- `src/main/java/com/okcir/et/admin/currency/dto/UpdateCurrencyPairRequest.java`
- `src/main/java/com/okcir/et/admin/currency/dto/CurrencyPairResponse.java`

### Existing Files Referenced (Do Not Modify)
- `com.okcir.et.admin.common.ApiResponse<T>`
- `com.okcir.et.admin.common.exception.ResourceNotFoundException`
- `com.okcir.et.admin.common.exception.DuplicateResourceException`

---

## 3. Sequential Tasks

### Task 1 — Flyway Migration (`V4__Create_Currency_And_CurrencyPair_Tables.sql`)

1. Create the `currencies` table:
   - `id` as `BIGSERIAL` primary key.
   - `code` as `VARCHAR(3)` with `NOT NULL` and `UNIQUE`.
   - `name` as `VARCHAR(100)` with `NOT NULL`.
   - `symbol` as `VARCHAR(10)` nullable.
   - `created_at` as `TIMESTAMP`.
   - `updated_at` as `TIMESTAMP`.
   - Add an index on `code` for fast lookups during pair derivation.

2. Create the `currency_pairs` table:
   - `id` as `BIGSERIAL` primary key.
   - `base_currency_id` as `BIGINT NOT NULL` with a foreign key to `currencies(id)` and `ON DELETE CASCADE`.
   - `quote_currency_id` as `BIGINT NOT NULL` with a foreign key to `currencies(id)` and `ON DELETE CASCADE`.
   - `pair_code` as `VARCHAR(6)` with `NOT NULL` and `UNIQUE`.
   - `rate` as `DECIMAL(18,8)` nullable (represents the current reference rate).
   - `created_at` as `TIMESTAMP`.
   - `updated_at` as `TIMESTAMP`.
   - Add a composite unique constraint on `(base_currency_id, quote_currency_id)`.
   - Add an index on `pair_code`.

3. Ensure `snake_case` naming is used consistently.

### Task 2 — Currency Entity

- Annotate with `@Entity`, `@Table(name = "currencies")`, `@EntityListeners(AuditingEntityListener.class)`.
- Use Lombok annotations: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Define `id` as `Long` with `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Map `code` with `@Column(name = "code", nullable = false, unique = true, length = 3)`.
- Map `name` with `@Column(name = "name", nullable = false, length = 100)`.
- Map `symbol` with `@Column(name = "symbol", length = 10)`.
- Add audit fields `createdAt` and `updatedAt` annotated with `@CreatedDate` and `@LastModifiedDate`, mapped via `@Column(name = "created_at")` and `@Column(name = "updated_at")`.

### Task 3 — Currency Repository

- Extend `JpaRepository<Currency, Long>`.
- Add `boolean existsByCode(String code)`.
- Add `boolean existsByCodeAndIdNot(String code, Long id)`.
- Add `Optional<Currency> findByCode(String code)` for use in CurrencyPair service derivation.

### Task 4 — Currency Request/Response DTOs

- **CreateCurrencyRequest:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. Fields: `code` (`@NotBlank`, `@Size(min=3,max=3)`), `name` (`@NotBlank`, `@Size(max=100)`), `symbol` (`@Size(max=10)`).
- **UpdateCurrencyRequest:** Same shape as create; all fields optional at annotation level because the update may be partial (service will apply only non-null values or require all—document the chosen contract). For consistency with existing patterns, require non-null for core fields in the request object and handle partial updates in service logic if needed.
- **CurrencyResponse:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. Fields: `id`, `code`, `name`, `symbol`, `createdAt`, `updatedAt`.

### Task 5 — Currency Service

- Annotate with `@Service` and `@RequiredArgsConstructor`.
- Inject `CurrencyRepository`.
- **Create:**
  - Validate that `code` does not already exist using `existsByCode`. If it does, throw `DuplicateResourceException`.
  - Build the `Currency` entity from the request DTO, save it, and return the mapped `CurrencyResponse`.
- **Get by ID:**
  - Fetch by id; if absent, throw `ResourceNotFoundException`.
  - Return mapped `CurrencyResponse`.
- **Get All:**
  - Use `findAll()`, map each to `CurrencyResponse`, return the list.
  - Mark method `@Transactional(readOnly = true)`.
- **Update:**
  - Fetch existing entity by id; if absent, throw `ResourceNotFoundException`.
  - If `code` is changing, validate uniqueness with `existsByCodeAndIdNot`. If duplicate, throw `DuplicateResourceException`.
  - Mutate entity fields from the request DTO, save, and return mapped response.
- **Delete:**
  - Fetch by id; if absent, throw `ResourceNotFoundException`.
  - Call `delete(entity)`.
- Include a private `CurrencyResponse toResponseDto(Currency entity)` mapper method.

### Task 6 — Currency Controller

- Annotate with `@RestController`, `@RequestMapping("/api/admin/currencies")`, `@RequiredArgsConstructor`.
- Inject `CurrencyService`.
- **POST** `/`:
  - Accept `@Valid @RequestBody CreateCurrencyRequest`.
  - Return `ApiResponse.created()` with the created `CurrencyResponse`.
- **GET** `/{id}`:
  - Accept `@PathVariable Long id`.
  - Return `ApiResponse.success()` with the `CurrencyResponse`.
- **GET** `/`:
  - Return `ApiResponse.success()` with `List<CurrencyResponse>`.
- **PUT** `/{id}`:
  - Accept `@PathVariable Long id` and `@Valid @RequestBody UpdateCurrencyRequest`.
  - Return `ApiResponse.success("Currency updated successfully", CurrencyResponse)`.
- **DELETE** `/{id}`:
  - Accept `@PathVariable Long id`.
  - Service deletes; controller returns `ApiResponse.success("Currency deleted successfully", null)`.

### Task 7 — CurrencyPair Entity

- Annotate with `@Entity`, `@Table(name = "currency_pairs")`, `@EntityListeners(AuditingEntityListener.class)`.
- Use Lombok annotations: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Define `id` as `Long` with `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Define `baseCurrency` as `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn(name = "base_currency_id", nullable = false)`.
- Define `quoteCurrency` as `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn(name = "quote_currency_id", nullable = false)`.
- Define `pairCode` as `@Column(name = "pair_code", nullable = false, unique = true, length = 6)`.
- Define `rate` as `@Column(name = "rate", precision = 18, scale = 8)` nullable.
- Add audit fields `createdAt` and `updatedAt` mapped to `created_at` and `updated_at`.

### Task 8 — CurrencyPair Repository

- Extend `JpaRepository<CurrencyPair, Long>`.
- Add `boolean existsByBaseCurrencyIdAndQuoteCurrencyId(Long baseCurrencyId, Long quoteCurrencyId)`.
- Add `boolean existsByBaseCurrencyIdAndQuoteCurrencyIdAndIdNot(Long baseCurrencyId, Long quoteCurrencyId, Long id)`.
- Add `boolean existsByPairCode(String pairCode)` as a guard for the unlikely collision scenario.
- Add `Optional<CurrencyPair> findByPairCode(String pairCode)`.

### Task 9 — CurrencyPair Request/Response DTOs

- **CreateCurrencyPairRequest:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. Fields: `baseCurrencyId` (`@NotNull`), `quoteCurrencyId` (`@NotNull`), `rate` (nullable, no validation annotation or `@DecimalMin`/`@DecimalMax` if desired).
- **UpdateCurrencyPairRequest:** Same shape; `rate` may be updated independently. `baseCurrencyId` and `quoteCurrencyId` can also be provided if the update allows re-pairing (document decision). If re-pairing is allowed, the service must re-derive `pairCode` and re-validate uniqueness.
- **CurrencyPairResponse:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`. Fields: `id`, `baseCurrencyId`, `quoteCurrencyId`, `pairCode`, `rate`, `createdAt`, `updatedAt`.

### Task 10 — CurrencyPair Service

- Annotate with `@Service` and `@RequiredArgsConstructor`.
- Inject `CurrencyPairRepository` and `CurrencyRepository`.
- **Create:**
  1. Validate `baseCurrencyId` ≠ `quoteCurrencyId`. If equal, throw `DuplicateResourceException` (or a domain-specific exception if introduced).
  2. Load `baseCurrency` and `quoteCurrency` from `CurrencyRepository` by id. If either missing, throw `ResourceNotFoundException`.
  3. Check `existsByBaseCurrencyIdAndQuoteCurrencyId`. If true, throw `DuplicateResourceException`.
  4. Derive `pairCode` by concatenating `baseCurrency.getCode()` + `quoteCurrency.getCode()` (6 characters).
  5. Build and save `CurrencyPair` with the derived `pairCode`, return mapped `CurrencyPairResponse`.
- **Get by ID:**
  - Fetch by id; if absent, throw `ResourceNotFoundException`.
  - Return mapped response.
- **Get All:**
  - `findAll()`, map to list, return.
  - Mark `@Transactional(readOnly = true)`.
- **Update:**
  1. Fetch existing pair by id; if absent, throw `ResourceNotFoundException`.
  2. If request changes base or quote currency, re-validate:
     - Both ids must resolve to existing currencies.
     - `baseCurrencyId` ≠ `quoteCurrencyId`.
     - `existsByBaseCurrencyIdAndQuoteCurrencyIdAndIdNot` must be false.
  3. Re-derive `pairCode` if currencies changed.
  4. Update `rate` if provided.
  5. Save and return mapped response.
- **Delete:**
  - Fetch by id; if absent, throw `ResourceNotFoundException`.
  - Delete and return.
- Include a private `CurrencyPairResponse toResponseDto(CurrencyPair entity)` mapper.

### Task 11 — CurrencyPair Controller

- Annotate with `@RestController`, `@RequestMapping("/api/admin/currency-pairs")`, `@RequiredArgsConstructor`.
- Inject `CurrencyPairService`.
- **POST** `/`:
  - Accept `@Valid @RequestBody CreateCurrencyPairRequest`.
  - Return `ApiResponse.created()` with `CurrencyPairResponse`.
- **GET** `/{id}`:
  - Return `ApiResponse.success()` with `CurrencyPairResponse`.
- **GET** `/`:
  - Return `ApiResponse.success()` with `List<CurrencyPairResponse>`.
- **PUT** `/{id}`:
  - Return `ApiResponse.success("Currency pair updated successfully", CurrencyPairResponse)`.
- **DELETE** `/{id}`:
  - Return `ApiResponse.success("Currency pair deleted successfully", null)`.

### Task 12 — Integration & Smoke Test Checklist (Manual / Postman)

1. Verify `V4` migration runs cleanly against an empty / migrated-up schema.
2. Create currencies (e.g., USD, EUR, JPY).
3. Attempt to create a duplicate currency code → expect 409-style error via `DuplicateResourceException` handler.
4. Create a valid currency pair (EUR/USD).
5. Attempt to create the same base+quote combination → expect duplicate error.
6. Attempt to create a pair with base == quote → expect validation error.
7. Delete a currency and verify that cascading removes associated currency pairs (due to `ON DELETE CASCADE`).
8. Update a pair’s rate without changing currencies → success.
9. Update a pair to swap base/quote (EUR/USD → USD/EUR) → success if unique, else duplicate error.
10. Verify `pair_code` is always 6 characters and equals `BASECODE` + `QUOTECODE`.

---

## 4. Validation Rules

### Currency-Level Validation
- `code`: Must be present, exactly 3 characters, uppercase or uppercase-normalized by the service. Unique across the table.
- `name`: Must be present, max 100 characters.
- `symbol`: Optional, max 10 characters.

### CurrencyPair-Level Validation
- `baseCurrencyId` and `quoteCurrencyId`: Must be non-null and must resolve to existing `Currency` records.
- `baseCurrencyId` must not equal `quoteCurrencyId`.
- The combination `(base_currency_id, quote_currency_id)` must be unique.
- `pairCode` is derived, not user-supplied, and must be exactly `baseCurrency.code + quoteCurrency.code` (6 chars).
- `rate`: Optional. If provided, must be a positive decimal (or zero if the business allows zero rates). Use `precision = 18, scale = 8`.

### Cross-Entity Validation
- Before creating or updating a `CurrencyPair`, both referenced `Currency` entities must exist. If either does not, throw `ResourceNotFoundException`.
- When a `Currency` is deleted, all `CurrencyPair` rows referencing it as base or quote are removed via DB-level cascade.

---

## 5. Edge Cases

| Edge Case | Mitigation |
|-----------|------------|
| **Duplicate ISO code on Currency create** | Guard with `existsByCode` → `DuplicateResourceException`. |
| **Duplicate ISO code on Currency update** | Guard with `existsByCodeAndIdNot` → `DuplicateResourceException`. |
| **Duplicate (base, quote) combination** | Composite unique constraint in DB + programmatic `existsByBaseCurrencyIdAndQuoteCurrencyId` check for user-friendly error messages. |
| **Base == Quote** | Programmatic check in service before any save; throw meaningful exception. |
| **Currency deletion leaves orphaned pairs** | `ON DELETE CASCADE` on both FKs in `currency_pairs`. |
| **Pair code collision** | Highly improbable because pair code is derived from unique 3-char currency codes and the combination is unique; still guarded by `pair_code UNIQUE` index. |
| **Rate precision loss** | Use `BigDecimal` in entity/DTO and `DECIMAL(18,8)` in DDL. |
| **Updating a pair to an existing reversed pair** | Allowed if the reversed combination is unique (e.g., EUR/USD and USD/EUR can coexist). If not desired, add an additional service-level check. |
| **Case sensitivity in currency codes** | Store uppercase; normalize input to uppercase in service before persistence and before derivation. |
| **Lazy-loading in mapper** | Ensure `toResponseDto` extracts `baseCurrency.getId()` and `quoteCurrency.getId()` within the transaction boundary. |

---

## Appendix A — URL Mapping Summary

| Method | Endpoint | Action |
|--------|----------|--------|
| POST | `/api/admin/currencies` | Create currency |
| GET | `/api/admin/currencies/{id}` | Get currency by ID |
| GET | `/api/admin/currencies` | List all currencies |
| PUT | `/api/admin/currencies/{id}` | Update currency |
| DELETE | `/api/admin/currencies/{id}` | Delete currency |
| POST | `/api/admin/currency-pairs` | Create currency pair |
| GET | `/api/admin/currency-pairs/{id}` | Get pair by ID |
| GET | `/api/admin/currency-pairs` | List all pairs |
| PUT | `/api/admin/currency-pairs/{id}` | Update pair |
| DELETE | `/api/admin/currency-pairs/{id}` | Delete pair |
