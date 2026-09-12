package com.payment.gateway.gateway.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class GatewayConfig {

    @Value("${services.merchant.base-url}")
    private String merchantURL;
    @Value("${services.payment.base-url}")
    private String paymentURL;
    @Value("${services.notification.base-url}")
    private String notificationURL;
    @Value("${services.settlement.base-url}")
    private String settlementURL;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("merchant-service", r -> r
                        .path("/merchants/**")
                        .uri(merchantURL))
                .route("payment-service", r -> r
                        .path("/payments/**")
                        .uri(paymentURL))
                .route("notification-service", r -> r
                        .path("/notifications/**")
                        .uri(notificationURL))
                .route("settlement-service", r -> r
                        .path("/settlements/**")
                        .uri(settlementURL))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
