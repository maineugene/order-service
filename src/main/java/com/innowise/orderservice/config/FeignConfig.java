package com.innowise.orderservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.innowise.orderservice.client")
public class FeignConfig {
}
