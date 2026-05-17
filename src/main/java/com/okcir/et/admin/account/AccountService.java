package com.okcir.et.admin.account;

import com.okcir.et.admin.account.dto.AccountRequestDto;
import com.okcir.et.admin.account.dto.AccountResponseDto;
import com.okcir.et.admin.account.dto.AccountSummaryDto;
import com.okcir.et.admin.common.exception.DuplicateResourceException;
import com.okcir.et.admin.common.exception.ResourceNotFoundException;
import com.okcir.et.admin.currency.Currency;
import com.okcir.et.admin.currency.CurrencyRepository;
import com.okcir.et.admin.group.Group;
import com.okcir.et.admin.group.GroupRepository;
import com.okcir.et.admin.group.dto.GroupSummaryDto;
import com.okcir.et.admin.settlementinstruction.SettlementInstruction;
import com.okcir.et.admin.settlementinstruction.SettlementInstructionRepository;
import com.okcir.et.admin.settlementinstruction.dto.SettlementInstructionNestedRequestDto;
import com.okcir.et.admin.settlementinstruction.dto.SettlementInstructionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final GroupRepository groupRepository;
  private final CurrencyRepository currencyRepository;
  private final SettlementInstructionRepository settlementInstructionRepository;

  @Transactional
  public AccountResponseDto createAccount(AccountRequestDto request) {
    if (accountRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException("Account name '" + request.getName() + "' already exists");
    }

    Set<Group> groups = resolveGroups(request.getGroupIds());

    Account account = Account.builder()
        .name(request.getName())
        .description(request.getDescription())
        .counterparty(request.getCounterparty())
        .groups(groups)
        .build();

    if (request.getSettlementInstructions() != null && !request.getSettlementInstructions().isEmpty()) {
      for (SettlementInstructionNestedRequestDto nested : request.getSettlementInstructions()) {
        Currency currency = currencyRepository.findById(nested.getCcyId())
            .orElseThrow(() -> new ResourceNotFoundException("Currency", nested.getCcyId()));

        SettlementInstruction si = SettlementInstruction.builder()
            .account(account)
            .currency(currency)
            .settlementNumber(nested.getSettlementNumber())
            .isDefault(nested.getIsDefault())
            .build();

        account.getSettlementInstructions().add(si);
      }
    }

    Account saved = accountRepository.save(account);
    return toResponseDto(saved);
  }

  @Transactional(readOnly = true)
  public List<AccountResponseDto> getAllAccounts() {
    return accountRepository.findAll().stream()
        .map(this::toResponseDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public AccountResponseDto getAccountById(Long id) {
    Account account = accountRepository.findByIdWithDetails(id)
        .orElseThrow(() -> new ResourceNotFoundException("Account", id));
    return toResponseDto(account);
  }

  @Transactional
  public AccountResponseDto updateAccount(Long id, AccountRequestDto request) {
    Account account = accountRepository.findByIdWithDetails(id)
        .orElseThrow(() -> new ResourceNotFoundException("Account", id));

    if (accountRepository.existsByNameAndIdNot(request.getName(), id)) {
      throw new DuplicateResourceException("Account name '" + request.getName() + "' already exists");
    }

    account.setName(request.getName());
    account.setDescription(request.getDescription());
    account.setCounterparty(request.getCounterparty());

    Set<Group> groups = resolveGroups(request.getGroupIds());
    account.setGroups(groups);

    if (request.getSettlementInstructions() != null) {
      updateSettlementInstructions(account, request.getSettlementInstructions());
    }

    Account updated = accountRepository.save(account);
    return toResponseDto(updated);
  }

  @Transactional
  public void deleteAccount(Long id) {
    if (!accountRepository.existsById(id)) {
      throw new ResourceNotFoundException("Account", id);
    }
    accountRepository.deleteById(id);
  }

  private void updateSettlementInstructions(Account account, List<SettlementInstructionNestedRequestDto> nestedList) {
    Set<Long> incomingIds = new HashSet<>();

    for (SettlementInstructionNestedRequestDto nested : nestedList) {
      Currency currency = currencyRepository.findById(nested.getCcyId())
          .orElseThrow(() -> new ResourceNotFoundException("Currency", nested.getCcyId()));

      SettlementInstruction si;
      if (nested.getSettlementNumber() != null && account.getSettlementInstructions().stream()
          .anyMatch(existing -> existing.getSettlementNumber().equals(nested.getSettlementNumber()))) {
        si = account.getSettlementInstructions().stream()
            .filter(existing -> existing.getSettlementNumber().equals(nested.getSettlementNumber()))
            .findFirst()
            .orElseThrow();
        incomingIds.add(si.getId());

        if (Boolean.TRUE.equals(nested.getIsDefault())) {
          if (settlementInstructionRepository.existsByAccountIdAndCurrencyIdAndIsDefaultTrueAndIdNot(
              account.getId(), currency.getId(), si.getId())) {
            throw new DuplicateResourceException("A default settlement instruction already exists for this account and currency");
          }
        }

        si.setCurrency(currency);
        si.setIsDefault(nested.getIsDefault());
      } else {
        if (Boolean.TRUE.equals(nested.getIsDefault())) {
          if (settlementInstructionRepository.existsByAccountIdAndCurrencyIdAndIsDefaultTrue(
              account.getId(), currency.getId())) {
            throw new DuplicateResourceException("A default settlement instruction already exists for this account and currency");
          }
        }

        si = SettlementInstruction.builder()
            .account(account)
            .currency(currency)
            .settlementNumber(nested.getSettlementNumber())
            .isDefault(nested.getIsDefault())
            .build();
        account.getSettlementInstructions().add(si);
      }
    }

    account.getSettlementInstructions().removeIf(si -> !incomingIds.contains(si.getId()));
  }

  private Set<Group> resolveGroups(Set<Long> groupIds) {
    if (groupIds == null || groupIds.isEmpty()) {
      return new HashSet<>();
    }

    List<Group> found = groupRepository.findAllById(groupIds);

    if (found.size() != groupIds.size()) {
      Set<Long> foundIds = found.stream().map(Group::getId).collect(Collectors.toSet());
      Set<Long> missingIds = new HashSet<>(groupIds);
      missingIds.removeAll(foundIds);
      throw new ResourceNotFoundException("Group(s) not found with id(s): " + missingIds);
    }

    return new HashSet<>(found);
  }

  private AccountResponseDto toResponseDto(Account account) {
    List<GroupSummaryDto> groupDtos = account.getGroups().stream()
        .map(g -> GroupSummaryDto.builder()
            .id(g.getId())
            .name(g.getName())
            .build())
        .toList();

    List<SettlementInstructionResponseDto> siDtos = account.getSettlementInstructions().stream()
        .map(this::siToResponseDto)
        .toList();

    return AccountResponseDto.builder()
        .id(account.getId())
        .name(account.getName())
        .description(account.getDescription())
        .counterparty(account.getCounterparty())
        .groups(groupDtos)
        .settlementInstructions(siDtos)
        .createdAt(account.getCreatedAt())
        .updatedAt(account.getUpdatedAt())
        .build();
  }

  private SettlementInstructionResponseDto siToResponseDto(SettlementInstruction si) {
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
