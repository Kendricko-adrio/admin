package com.okcir.et.admin.currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyPairRepository extends JpaRepository<CurrencyPair, Long> {

  boolean existsByBaseCurrencyIdAndQuoteCurrencyId(Long baseCurrencyId, Long quoteCurrencyId);

  boolean existsByBaseCurrencyIdAndQuoteCurrencyIdAndIdNot(Long baseCurrencyId, Long quoteCurrencyId, Long id);
}
