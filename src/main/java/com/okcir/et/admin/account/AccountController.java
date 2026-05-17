package com.okcir.et.admin.account;

import com.okcir.et.admin.account.dto.AccountRequestDto;
import com.okcir.et.admin.account.dto.AccountResponseDto;
import com.okcir.et.admin.common.ApiResponse;
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
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @PostMapping
  public ResponseEntity<ApiResponse<AccountResponseDto>> createAccount(
      @Valid @RequestBody AccountRequestDto request) {
    AccountResponseDto created = accountService.createAccount(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(created));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getAllAccounts() {
    List<AccountResponseDto> accounts = accountService.getAllAccounts();
    return ResponseEntity.ok(ApiResponse.success(accounts));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<AccountResponseDto>> getAccountById(@PathVariable Long id) {
    AccountResponseDto account = accountService.getAccountById(id);
    return ResponseEntity.ok(ApiResponse.success(account));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<AccountResponseDto>> updateAccount(
      @PathVariable Long id,
      @Valid @RequestBody AccountRequestDto request) {
    AccountResponseDto updated = accountService.updateAccount(id, request);
    return ResponseEntity.ok(ApiResponse.success("Account updated successfully", updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
    accountService.deleteAccount(id);
    return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
  }
}
