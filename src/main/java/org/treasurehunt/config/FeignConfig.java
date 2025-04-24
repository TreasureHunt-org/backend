package org.treasurehunt.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable OpenFeign clients
 */
@Configuration
@EnableFeignClients(basePackages = "org.treasurehunt.hunt.api.judge0")
public class FeignConfig {
    // No additional configuration needed
}