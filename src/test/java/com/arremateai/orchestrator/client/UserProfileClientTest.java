package com.arremateai.orchestrator.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileClientTest {

    private MockWebServer server;
    private UserProfileClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.create(server.url("/").toString());
        client = new UserProfileClient(webClient, CircuitBreakerRegistry.ofDefaults());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private void enqueueJson(String body) {
        server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));
    }

    @Test
    void buscarPerfil() throws InterruptedException {
        enqueueJson("{\"nome\":\"Ana\"}");
        StepVerifier.create(client.buscarPerfil("u1"))
                .assertNext(m -> assertThat(m).containsEntry("nome", "Ana"))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/perfil");
        assertThat(req.getHeader("X-User-Id")).isEqualTo("u1");
    }

    @Test
    void verificarFavorito() throws InterruptedException {
        UUID id = UUID.randomUUID();
        enqueueJson("{\"favoritado\":true}");
        StepVerifier.create(client.verificarFavorito(id, "u1"))
                .assertNext(m -> assertThat(m).containsEntry("favoritado", true))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/favoritos/" + id + "/status");
    }

    @Test
    void listarFavoritos() throws InterruptedException {
        enqueueJson("[{\"id\":\"a\"}]");
        StepVerifier.create(client.listarFavoritos("u1"))
                .assertNext(list -> assertThat(list).hasSize(1))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/favoritos");
    }

    @Test
    void contarFavoritos() throws InterruptedException {
        enqueueJson("{\"total\":4}");
        StepVerifier.create(client.contarFavoritos("u1"))
                .assertNext(m -> assertThat(m).containsEntry("total", 4L))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/favoritos/count");
    }
}
