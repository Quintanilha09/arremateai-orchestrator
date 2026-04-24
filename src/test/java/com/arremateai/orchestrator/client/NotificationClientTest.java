package com.arremateai.orchestrator.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationClientTest {

    private MockWebServer server;
    private NotificationClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.create(server.url("/").toString());
        client = new NotificationClient(webClient, CircuitBreakerRegistry.ofDefaults(), "secret-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    @DisplayName("Deve enviar notificação interna com X-Internal-Api-Key")
    void criarNotificacaoInterna() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("{\"id\":\"abc\"}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(client.criarNotificacaoInterna(UUID.randomUUID(), "t", "m", "TIPO"))
                .assertNext(m -> assertThat(m).containsEntry("id", "abc"))
                .verifyComplete();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/notificacoes/interna");
        assertThat(req.getHeader("X-Internal-Api-Key")).isEqualTo("secret-key");
    }

    @Test
    @DisplayName("Deve contar notificações não lidas")
    void contarNaoLidas() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setBody("{\"total\":7}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(client.contarNaoLidas("u1"))
                .assertNext(m -> assertThat(m).containsEntry("total", 7L))
                .verifyComplete();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/notificacoes/nao-lidas/count");
        assertThat(req.getHeader("X-User-Id")).isEqualTo("u1");
    }

    @Test
    @DisplayName("Deve propagar erro quando servidor responde 5xx")
    void erroServidor() {
        server.enqueue(new MockResponse().setResponseCode(500));

        StepVerifier.create(client.contarNaoLidas("u1"))
                .expectError()
                .verify();
    }
}
