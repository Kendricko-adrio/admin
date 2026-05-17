package com.okcir.et.admin.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);

  @Query("SELECT a FROM Account a LEFT JOIN FETCH a.settlementInstructions si LEFT JOIN FETCH si.currency LEFT JOIN FETCH a.groups WHERE a.id = :id")
  Optional<Account> findByIdWithDetails(@Param("id") Long id);
}
