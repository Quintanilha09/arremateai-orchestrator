package com.arremateai.orchestrator.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class UserProfileClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public UserProfileClient(WebClient userProfileWebClient,
                             CircuitBreakerRegistry registry) {
        this.webClient = userProfileWebClient;
        this.circuitBreaker = registry.circuitBreaker("user-profile");
    }

    public Mono<Map<String, Object>> buscarPerfil(String userId) {
        return webClient.get()
                .uri("/api/perfil")
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao buscar perfil: {}", e.getMessage()));
    }

    public Mono<Map<String, Boolean>> verificarFavorito(UUID imovelId, String userId) {
        return webClient.get()
                .uri("/api/favoritos/{imovelId}/status", imovelId)
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Boolean>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao verificar favorito: {}", e.getMessage()));
    }

    public Mono<List<Map<String, Object>>> listarFavoritos(String userId) {
        return webClient.get()
                .uri("/api/favoritos")
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao listar favoritos: {}", e.getMessage()));
    }

    public Mono<Map<String, Long>> contarFavoritos(String userId) {
        return webClient.get()
                .uri("/api/favoritos/count")
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao contar favoritos: {}", e.getMessage()));
    }
}
