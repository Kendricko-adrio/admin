package com.okcir.et.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

  @NotBlank(message = "Username is required")
  private String username;

  private String password; // only used when auth.is-ldap=true
}
