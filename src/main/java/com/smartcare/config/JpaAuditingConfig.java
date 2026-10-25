package com.smartcare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables @CreatedDate / @LastModifiedDate population on BaseEntity subclasses.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
