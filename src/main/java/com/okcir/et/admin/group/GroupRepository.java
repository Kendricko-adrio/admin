package com.okcir.et.admin.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);
}
