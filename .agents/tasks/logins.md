# Task: Implementation of Hybrid Authentication with JWT and Permissions

## Context

I need to upgrade the Login Endpoint to return a JWT Token upon successful authentication. The application must handle two authentication modes based on a configuration flag and include user permissions in the token payload.

## Requirements (English)

1.  **Configuration**:
    - Add `auth.is-ldap` (boolean) in `application.properties`.
    - Add `jwt.secret` and `jwt.expiration` properties.
2.  **Authentication Logic**:
    - **IF `auth.is-ldap=true`**:
      - Perform a simulated REST API call to an internal LDAP service using `username` and `password`.
      - If successful, fetch dummy permissions for the user (e.g., `ROLE_USER`, `ROLE_ADMIN`).
    - **IF `auth.is-ldap=false`**:
      - Skip password validation. Validate only that the `username` is provided.
      - Assign a default set of permissions (e.g., `ROLE_GUEST`).
3.  **JWT Generation**:
    - Upon successful validation, generate a JWT token.
    - **Payload Requirements**: The token must include `username` as the subject and a custom claim for `permissions` (List of strings).
4.  **Response Structure**:
    - Return a JSON response containing the `access_token`, `token_type` (Bearer), and `expires_in`.
5.  **Security Component**:
    - Implement a `JwtUtils` or `JwtService` class to handle token creation and signing (using HMAC256 or similar).

## Technical Specifications

- **Language**: Java / Spring Boot
- **Library**: Use `jjwt` (Java JWT) or `auth0-jwt` for token handling.
- **DTOs**: `LoginRequest` (Input) and `LoginResponse` (Output with JWT details).

## Expected Deliverables

- `LoginRequest.java` & `LoginResponse.java`
- `JwtProvider.java` (Logic for generating tokens)
- `AuthService.java` (Logic for LDAP/Basic toggle + Permission mapping)
- `AuthController.java` (REST Endpoint)
