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
public class PropertyCatalogClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public PropertyCatalogClient(WebClient propertyCatalogWebClient,
                                 CircuitBreakerRegistry registry) {
        this.webClient = propertyCatalogWebClient;
        this.circuitBreaker = registry.circuitBreaker("property-catalog");
    }

    public Mono<List<Object>> buscarDestaques(int limite) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/imoveis/destaques")
                        .queryParam("limite", limite)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao buscar destaques: {}", e.getMessage()));
    }

    public Mono<List<Object>> buscarRecentes(int limite) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/imoveis/recentes")
                        .queryParam("limite", limite)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao buscar recentes: {}", e.getMessage()));
    }

    public Mono<Map<String, Object>> buscarEstatisticas() {
        return webClient.get()
                .uri("/api/imoveis/estatisticas")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao buscar estatísticas: {}", e.getMessage()));
    }

    public Mono<Map<String, Object>> buscarImovelPorId(UUID id) {
        return webClient.get()
                .uri("/api/imoveis/{id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao buscar imóvel {}: {}", id, e.getMessage()));
    }

    public Mono<Map<String, Object>> buscarComFiltros(Map<String, String> parametros) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/imoveis");
                    parametros.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha na busca com filtros: {}", e.getMessage()));
    }

    public Mono<Map<String, Object>> buscarMeusImoveis(String userId, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/imoveis/meus")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.error("Falha ao buscar meus imóveis: {}", e.getMessage()));
    }
}
