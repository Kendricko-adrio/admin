package com.okcir.et.admin.accessright;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessRightRepository extends JpaRepository<AccessRight, Long> {
}
