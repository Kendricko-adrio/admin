package com.okcir.et.admin.accessright;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessRightRepository extends JpaRepository<AccessRight, Long> {

  List<AccessRight> findAllByOrderByCodeAsc();

  boolean existsByParentCode(String parentCode);
}
