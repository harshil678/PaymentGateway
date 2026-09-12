package com.payment.gateway.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private volatile long windowStart = System.currentTimeMillis();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String keyPrefix = exchange.getRequest().getHeaders().getFirst("X-Api-Key-Prefix");
        if (keyPrefix == null) return chain.filter(exchange);

        resetWindowIfExpired();

        AtomicInteger count = requestCounts.computeIfAbsent(keyPrefix, k -> new AtomicInteger(0));

        if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for key prefix: {}", keyPrefix);
            return tooManyRequests(exchange);
        }

        return chain.filter(exchange);
    }

    private void resetWindowIfExpired() {
        long now = System.currentTimeMillis();
        if (now - windowStart > 60_000) {
            windowStart = now;
            requestCounts.clear();
        }
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"error\":\"Rate limit exceeded\",\"status\":429}";
        var buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
