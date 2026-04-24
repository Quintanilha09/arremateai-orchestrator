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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyCatalogClientTest {

    private MockWebServer server;
    private PropertyCatalogClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.create(server.url("/").toString());
        client = new PropertyCatalogClient(webClient, CircuitBreakerRegistry.ofDefaults());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private void enqueueJson(String body) {
        server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));
    }

    @Test
    void buscarDestaques() throws InterruptedException {
        enqueueJson("[{\"id\":1}]");
        StepVerifier.create(client.buscarDestaques(5))
                .assertNext(list -> assertThat(list).hasSize(1))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/api/imoveis/destaques").contains("limite=5");
    }

    @Test
    void buscarRecentes() throws InterruptedException {
        enqueueJson("[]");
        StepVerifier.create(client.buscarRecentes(3))
                .assertNext(list -> assertThat(list).isEmpty())
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/api/imoveis/recentes").contains("limite=3");
    }

    @Test
    void buscarEstatisticas() throws InterruptedException {
        enqueueJson("{\"total\":10}");
        StepVerifier.create(client.buscarEstatisticas())
                .assertNext(m -> assertThat(m).containsEntry("total", 10))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/imoveis/estatisticas");
    }

    @Test
    void buscarImovelPorId() throws InterruptedException {
        UUID id = UUID.randomUUID();
        enqueueJson("{\"id\":\"" + id + "\"}");
        StepVerifier.create(client.buscarImovelPorId(id))
                .assertNext(m -> assertThat(m).containsEntry("id", id.toString()))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/imoveis/" + id);
    }

    @Test
    void buscarComFiltros() throws InterruptedException {
        enqueueJson("{\"content\":[]}");
        StepVerifier.create(client.buscarComFiltros(Map.of("tipo", "casa")))
                .assertNext(m -> assertThat(m).containsKey("content"))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).startsWith("/api/imoveis").contains("tipo=casa");
    }

    @Test
    void buscarMeusImoveis() throws InterruptedException {
        enqueueJson("{\"totalElements\":2}");
        StepVerifier.create(client.buscarMeusImoveis("u1", 0, 10))
                .assertNext(m -> assertThat(m).containsEntry("totalElements", 2))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/api/imoveis/meus").contains("page=0").contains("size=10");
        assertThat(req.getHeader("X-User-Id")).isEqualTo("u1");
    }
}
