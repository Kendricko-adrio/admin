package com.okcir.et.admin.currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

  boolean existsByCode(String code);

  boolean existsByCodeAndIdNot(String code, Long id);
}
