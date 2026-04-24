package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.client.VendorClient;
import com.arremateai.orchestrator.dto.response.AdminDashboardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminFlowServiceTest {

    @Mock
    private VendorClient vendorClient;
    @Mock
    private PropertyCatalogClient propertyCatalogClient;

    @InjectMocks
    private AdminFlowService service;

    @Test
    @DisplayName("Deve montar dashboard admin completo")
    void happyPath() {
        when(vendorClient.buscarEstatisticasAdmin("u1", "ADMIN"))
                .thenReturn(Mono.just(Map.of("total", 10)));
        when(propertyCatalogClient.buscarEstatisticas()).thenReturn(Mono.just(Map.of("total", 20)));
        when(vendorClient.buscarPendentes(5, "u1", "ADMIN"))
                .thenReturn(Mono.just(Map.of("content", List.of(Map.of("id", "v1")))));

        AdminDashboardResponse r = service.montarDashboardAdmin("u1", "ADMIN");

        assertThat(r.vendedores()).containsEntry("total", 10);
        assertThat(r.imoveis()).containsEntry("total", 20);
        assertThat(r.vendedoresPendentes()).hasSize(1);
        assertThat(r.servicosIndisponiveis()).isEmpty();
    }

    @Test
    @DisplayName("Deve degradar parcialmente quando todos falham")
    void todosFalham() {
        when(vendorClient.buscarEstatisticasAdmin("u1", "ADMIN"))
                .thenReturn(Mono.error(new RuntimeException("x")));
        when(propertyCatalogClient.buscarEstatisticas())
                .thenReturn(Mono.error(new RuntimeException("x")));
        when(vendorClient.buscarPendentes(5, "u1", "ADMIN"))
                .thenReturn(Mono.error(new RuntimeException("x")));

        AdminDashboardResponse r = service.montarDashboardAdmin("u1", "ADMIN");

        assertThat(r.servicosIndisponiveis()).contains("vendor", "property-catalog");
        assertThat(r.vendedoresPendentes()).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando content não é List")
    void contentInvalido() {
        when(vendorClient.buscarEstatisticasAdmin("u1", "ADMIN")).thenReturn(Mono.just(Map.of()));
        when(propertyCatalogClient.buscarEstatisticas()).thenReturn(Mono.just(Map.of()));
        when(vendorClient.buscarPendentes(5, "u1", "ADMIN")).thenReturn(Mono.just(Map.of()));

        AdminDashboardResponse r = service.montarDashboardAdmin("u1", "ADMIN");

        assertThat(r.vendedoresPendentes()).isEmpty();
    }
}
