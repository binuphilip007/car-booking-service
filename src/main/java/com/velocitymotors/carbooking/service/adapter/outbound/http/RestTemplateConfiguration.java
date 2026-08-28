package com.velocitymotors.carbooking.service.adapter.outbound.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${credit-card-validation.connect-timeout:2s}") Duration connectTimeout,
            @Value("${credit-card-validation.read-timeout:3s}") Duration readTimeout) {
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .additionalInterceptors(new TimingClientHttpRequestInterceptor())
                .build();
    }
}