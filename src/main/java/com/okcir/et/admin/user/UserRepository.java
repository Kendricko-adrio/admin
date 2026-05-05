package com.okcir.et.admin.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsernameAndIdNot(String username, Long id);

  boolean existsByEmailAndIdNot(String email, Long id);

  java.util.List<User> findByUsernameContainingIgnoreCase(String username);
}
