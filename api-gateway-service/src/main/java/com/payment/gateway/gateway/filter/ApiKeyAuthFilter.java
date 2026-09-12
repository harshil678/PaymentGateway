package com.payment.gateway.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;

    private static final String HEADER_KEY_PREFIX = "X-Api-Key-Prefix";
    private static final String HEADER_KEY_SECRET = "X-Api-Key-Secret";
    @Value("${services.merchant.keyValidation-url}")
    private String INTERNAL_VALIDATE_URL;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (path.startsWith("/merchants") && exchange.getRequest().getMethod().name().equals("POST")
                //&& path.contains("/keys")
        ) {
            return chain.filter(exchange);
        }

        String keyPrefix = exchange.getRequest().getHeaders().getFirst(HEADER_KEY_PREFIX);
        String keySecret = exchange.getRequest().getHeaders().getFirst(HEADER_KEY_SECRET);

        if (keyPrefix == null || keySecret == null) {
            return unauthorized(exchange, "Missing API key headers");
        }

        return webClient.post()
                .uri(INTERNAL_VALIDATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("keyPrefix", keyPrefix, "rawSecret", keySecret))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    Boolean valid = (Boolean) response.get("valid");
                    if (Boolean.TRUE.equals(valid)) {
                        return chain.filter(exchange);
                    }
                    return unauthorized(exchange, "Invalid API key");
                })
                .onErrorResume(e -> {
                    log.error("Error validating API key", e);
                    return unauthorized(exchange, "Authentication service unavailable");
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"error\":\"" + message + "\",\"status\":401}";
        var buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
