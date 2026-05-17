package com.okcir.et.admin.settlementinstruction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementInstructionRepository extends JpaRepository<SettlementInstruction, Long> {

  List<SettlementInstruction> findByAccountId(Long accountId);

  boolean existsBySettlementNumber(String settlementNumber);

  boolean existsBySettlementNumberAndIdNot(String settlementNumber, Long id);

  boolean existsByAccountIdAndCurrencyIdAndIsDefaultTrue(Long accountId, Long currencyId);

  boolean existsByAccountIdAndCurrencyIdAndIsDefaultTrueAndIdNot(Long accountId, Long currencyId, Long id);
}
