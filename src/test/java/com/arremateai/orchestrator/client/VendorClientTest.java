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

class VendorClientTest {

    private MockWebServer server;
    private VendorClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.create(server.url("/").toString());
        client = new VendorClient(webClient, CircuitBreakerRegistry.ofDefaults());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private void enqueueJson(String body) {
        server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));
    }

    @Test
    void registrarVendedor() throws InterruptedException {
        enqueueJson("{\"id\":\"v1\"}");
        StepVerifier.create(client.registrarVendedor(Map.of("nome", "X")))
                .assertNext(m -> assertThat(m).containsEntry("id", "v1"))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/api/vendedores/registrar");
    }

    @Test
    void aprovarVendedor() throws InterruptedException {
        UUID id = UUID.randomUUID();
        enqueueJson("{\"status\":\"aprovado\"}");
        StepVerifier.create(client.aprovarVendedor(id, Map.of(), "admin1", "ADMIN"))
                .assertNext(m -> assertThat(m).containsEntry("status", "aprovado"))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PATCH");
        assertThat(req.getPath()).isEqualTo("/api/admin/vendedores/" + id + "/aprovar");
        assertThat(req.getHeader("X-User-Role")).isEqualTo("ADMIN");
    }

    @Test
    void rejeitarVendedor() throws InterruptedException {
        UUID id = UUID.randomUUID();
        enqueueJson("{\"status\":\"rejeitado\"}");
        StepVerifier.create(client.rejeitarVendedor(id, Map.of("motivo", "x"), "admin1", "ADMIN"))
                .assertNext(m -> assertThat(m).containsEntry("status", "rejeitado"))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/admin/vendedores/" + id + "/rejeitar");
    }

    @Test
    void buscarEstatisticasAdmin() throws InterruptedException {
        enqueueJson("{\"total\":3}");
        StepVerifier.create(client.buscarEstatisticasAdmin("admin1", "ADMIN"))
                .assertNext(m -> assertThat(m).containsEntry("total", 3))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/admin/vendedores/estatisticas");
    }

    @Test
    void buscarPendentes() throws InterruptedException {
        enqueueJson("{\"content\":[]}");
        StepVerifier.create(client.buscarPendentes(5, "admin1", "ADMIN"))
                .assertNext(m -> assertThat(m).containsKey("content"))
                .verifyComplete();
        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("/api/admin/vendedores/pendentes").contains("size=5");
    }
}
