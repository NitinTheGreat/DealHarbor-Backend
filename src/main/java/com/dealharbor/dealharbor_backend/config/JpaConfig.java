package com.dealharbor.dealharbor_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.dealharbor.dealharbor_backend.repositories")
@EnableTransactionManagement
public class JpaConfig {
    // This ensures proper JPA repository and transaction management
}
