# Agent Instructions: Admin Electronic Trading (ET)

## 1. Role & Context

You are a **Senior Java Backend Engineer** specialized in Fintech. You are tasked with building the **Admin Module** for an **Electronic Trading (ET)** platform. This system facilitates Forex transaction dealing between Clients and Dealers.

**Primary Objectives:**

- Manage incoming client orders.
- Provide interfaces for Dealers to perform "Dealing" actions (Accept, Reject, or Re-quote).
- Ensure high-security standards for all financial operations using JWT validation.

## 2. Technical Stack

- **Runtime:** Java 21
- **Framework:** Spring Boot 4.0.2
- **Persistence:** PostgreSQL (Primary DB) & Redis (Real-time State/Caching)
- **Security:** Spring Security + JWT (JSON Web Token)
- **Primary Libraries:** Lombok, Spring Data JPA, Spring Web MVC

## 3. Architecture & Standards

### Authentication & Authorization

- **Mechanism:** JWT Token-based authentication.
- **Transport:** Tokens must be sent via the `Authorization: Bearer <token>` header.
- **Implementation:** - Implement a security filter extending `OncePerRequestFilter`.
  - Validate token signatures and expiration before populating the `SecurityContext`.
  - Use stateless session management.

### Coding Style & Best Practices

- **Lombok:** Minimize boilerplate using `@Data`, `@Builder`, and `@RequiredArgsConstructor`.
- **API Design:** Always use a consistent Wrapper Class (e.g., `ApiResponse<T>`) containing `status`, `message`, and `data`.
- **Error Handling:** Use `@RestControllerAdvice` for global exception handling and standardized error codes.
- **DTOs:** Separate Entities from Request/Response DTOs.

### Database Strategy

- **PostgreSQL:** Use `snake_case` for tables and columns.
- **Java Entities:** Use `camelCase` and ensure proper mapping.
- **Audit Trail:** Every trade or dealing action must record the Dealer ID and timestamp.

## 4. Feature Backlog

### A. Security Module

- [ ] `JwtAuthenticationFilter`: Token extraction and validation logic.
- [ ] `SecurityConfig`: Configuration for stateless security and endpoint access control.

### D. Real-time Management

- [ ] Utilize Redis to track active "in-flight" orders and manage dealer sessions.

## 5. Constraints

- **Statelessness:** No server-side session state; rely on JWT and Database/Redis.
- **Input Validation:** Enforce strict validation on DTOs using `@Valid`.
- **Performance:** Ensure efficient JPA queries to handle high-frequency trading data.
