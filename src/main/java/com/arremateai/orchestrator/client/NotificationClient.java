package com.arremateai.orchestrator.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class NotificationClient {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    private final String apiKey;

    public NotificationClient(WebClient notificationWebClient,
                              CircuitBreakerRegistry registry,
                              @Value("${services.notification.api-key}") String apiKey) {
        this.webClient = notificationWebClient;
        this.circuitBreaker = registry.circuitBreaker("notification");
        this.apiKey = apiKey;
    }

    public Mono<Map<String, Object>> criarNotificacaoInterna(UUID userId, String titulo,
                                                             String mensagem, String tipo) {
        Map<String, Object> request = Map.of(
                "userId", userId.toString(),
                "titulo", titulo,
                "mensagem", mensagem,
                "tipo", tipo
        );

        return webClient.post()
                .uri("/api/notificacoes/interna")
                .header("X-Internal-Api-Key", apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.warn("Falha ao enviar notificação (best-effort): {}", e.getMessage()));
    }

    public Mono<Map<String, Long>> contarNaoLidas(String userId) {
        return webClient.get()
                .uri("/api/notificacoes/nao-lidas/count")
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {})
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(e -> log.warn("Falha ao contar notificações: {}", e.getMessage()));
    }
}
