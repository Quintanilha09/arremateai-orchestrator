package com.arremateai.orchestrator.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class VendorClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public VendorClient(WebClient vendorWebClient,
                        CircuitBreakerRegistry registry) {
        this.webClient = vendorWebClient;
        this.circuitBreaker = registry.circuitBreaker("vendor");
    }

    public Mono<Map<String, Object>> registrarVendedor(Map<String, Object> dados) {
        return webClient.post()
                .uri("/api/vendedores/registrar")
                .bodyValue(dados)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao registrar vendedor: {}", e.getMessage()));
    }

    public Mono<Map<String, Object>> aprovarVendedor(UUID vendedorId, Map<String, Object> dados,
                                                     String userId, String userRole) {
        return webClient.patch()
                .uri("/api/admin/vendedores/{id}/aprovar", vendedorId)
                .header("X-User-Id", userId)
                .header("X-User-Role", userRole)
                .bodyValue(dados)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao aprovar vendedor {}: {}", vendedorId, e.getMessage()));
    }

    public Mono<Map<String, Object>> rejeitarVendedor(UUID vendedorId, Map<String, Object> dados,
                                                      String userId, String userRole) {
        return webClient.patch()
                .uri("/api/admin/vendedores/{id}/rejeitar", vendedorId)
                .header("X-User-Id", userId)
                .header("X-User-Role", userRole)
                .bodyValue(dados)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao rejeitar vendedor {}: {}", vendedorId, e.getMessage()));
    }

    public Mono<Map<String, Object>> buscarEstatisticasAdmin(String userId, String userRole) {
        return webClient.get()
                .uri("/api/admin/vendedores/estatisticas")
                .header("X-User-Id", userId)
                .header("X-User-Role", userRole)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao buscar estatísticas de vendedores: {}", e.getMessage()));
    }

    public Mono<Map<String, Object>> buscarPendentes(int size, String userId, String userRole) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/admin/vendedores/pendentes")
                        .queryParam("size", size)
                        .build())
                .header("X-User-Id", userId)
                .header("X-User-Role", userRole)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao buscar vendedores pendentes: {}", e.getMessage()));
    }
}
