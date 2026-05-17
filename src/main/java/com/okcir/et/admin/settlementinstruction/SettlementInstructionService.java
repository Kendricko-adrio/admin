package com.okcir.et.admin.settlementinstruction;

import com.okcir.et.admin.account.Account;
import com.okcir.et.admin.account.AccountRepository;
import com.okcir.et.admin.common.exception.DuplicateResourceException;
import com.okcir.et.admin.common.exception.ResourceNotFoundException;
import com.okcir.et.admin.currency.Currency;
import com.okcir.et.admin.currency.CurrencyRepository;
import com.okcir.et.admin.settlementinstruction.dto.SettlementInstructionRequestDto;
import com.okcir.et.admin.settlementinstruction.dto.SettlementInstructionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementInstructionService {

  private final SettlementInstructionRepository settlementInstructionRepository;
  private final AccountRepository accountRepository;
  private final CurrencyRepository currencyRepository;

  @Transactional
  public SettlementInstructionResponseDto createSettlementInstruction(SettlementInstructionRequestDto request) {
    Account account = accountRepository.findById(request.getAccountId())
        .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountId()));

    Currency currency = currencyRepository.findById(request.getCcyId())
        .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCcyId()));

    if (settlementInstructionRepository.existsBySettlementNumber(request.getSettlementNumber())) {
      throw new DuplicateResourceException("Settlement number '" + request.getSettlementNumber() + "' already exists");
    }

    if (Boolean.TRUE.equals(request.getIsDefault())) {
      if (settlementInstructionRepository.existsByAccountIdAndCurrencyIdAndIsDefaultTrue(
          account.getId(), currency.getId())) {
        throw new DuplicateResourceException(
            "A default settlement instruction already exists for this account and currency");
      }
    }

    SettlementInstruction si = SettlementInstruction.builder()
        .account(account)
        .currency(currency)
        .settlementNumber(request.getSettlementNumber())
        .isDefault(request.getIsDefault())
        .build();

    SettlementInstruction saved = settlementInstructionRepository.save(si);
    return toResponseDto(saved);
  }

  @Transactional(readOnly = true)
  public List<SettlementInstructionResponseDto> getAllSettlementInstructions(Long accountId) {
    List<SettlementInstruction> instructions;
    if (accountId != null) {
      instructions = settlementInstructionRepository.findByAccountId(accountId);
    } else {
      instructions = settlementInstructionRepository.findAll();
    }
    return instructions.stream()
        .map(this::toResponseDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public SettlementInstructionResponseDto getSettlementInstructionById(Long id) {
    SettlementInstruction si = settlementInstructionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Settlement Instruction", id));
    return toResponseDto(si);
  }

  @Transactional
  public SettlementInstructionResponseDto updateSettlementInstruction(Long id, SettlementInstructionRequestDto request) {
    SettlementInstruction si = settlementInstructionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Settlement Instruction", id));

    Account account = accountRepository.findById(request.getAccountId())
        .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountId()));

    Currency currency = currencyRepository.findById(request.getCcyId())
        .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCcyId()));

    if (!si.getSettlementNumber().equals(request.getSettlementNumber())) {
      if (settlementInstructionRepository.existsBySettlementNumberAndIdNot(request.getSettlementNumber(), id)) {
        throw new DuplicateResourceException("Settlement number '" + request.getSettlementNumber() + "' already exists");
      }
    }

    if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(si.getIsDefault())) {
      if (settlementInstructionRepository.existsByAccountIdAndCurrencyIdAndIsDefaultTrueAndIdNot(
          account.getId(), currency.getId(), id)) {
        throw new DuplicateResourceException(
            "A default settlement instruction already exists for this account and currency");
      }
    }

    si.setAccount(account);
    si.setCurrency(currency);
    si.setSettlementNumber(request.getSettlementNumber());
    si.setIsDefault(request.getIsDefault());

    SettlementInstruction updated = settlementInstructionRepository.save(si);
    return toResponseDto(updated);
  }

  @Transactional
  public void deleteSettlementInstruction(Long id) {
    if (!settlementInstructionRepository.existsById(id)) {
      throw new ResourceNotFoundException("Settlement Instruction", id);
    }
    settlementInstructionRepository.deleteById(id);
  }

  private SettlementInstructionResponseDto toResponseDto(SettlementInstruction si) {
    return SettlementInstructionResponseDto.builder()
        .id(si.getId())
        .accountId(si.getAccount().getId())
        .ccyId(si.getCurrency().getId())
        .settlementNumber(si.getSettlementNumber())
        .isDefault(si.getIsDefault())
        .currencyCode(si.getCurrency().getCode())
        .createdAt(si.getCreatedAt())
        .updatedAt(si.getUpdatedAt())
        .build();
  }
}
