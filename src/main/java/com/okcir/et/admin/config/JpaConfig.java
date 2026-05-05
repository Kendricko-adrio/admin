package com.okcir.et.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
  // Enables @CreatedDate and @LastModifiedDate auto-population
}
