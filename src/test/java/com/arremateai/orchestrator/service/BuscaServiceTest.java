package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.client.UserProfileClient;
import com.arremateai.orchestrator.dto.response.BuscaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscaServiceTest {

    @Mock
    private PropertyCatalogClient propertyCatalogClient;
    @Mock
    private UserProfileClient userProfileClient;

    @InjectMocks
    private BuscaService service;

    @Test
    @DisplayName("Deve buscar sem userId sem enriquecer favoritos")
    void semUserId() {
        Map<String, String> params = Map.of("tipo", "CASA");
        Map<String, Object> page = Map.of(
                "content", List.of(Map.of("id", "1")),
                "number", 0,
                "totalPages", 1,
                "totalElements", 1L
        );
        when(propertyCatalogClient.buscarComFiltros(params)).thenReturn(Mono.just(page));

        BuscaResponse r = service.buscar(params, null);

        assertThat(r.imoveis()).hasSize(1);
        assertThat(r.page()).isZero();
        assertThat(r.totalPages()).isEqualTo(1);
        assertThat(r.totalElements()).isEqualTo(1L);
        assertThat(r.servicosIndisponiveis()).isEmpty();
    }

    @Test
    @DisplayName("Deve enriquecer com favoritado quando user-profile responde")
    void comFavoritos() {
        Map<String, String> params = Map.of();
        Map<String, Object> imovel = new HashMap<>();
        imovel.put("id", "imv1");
        Map<String, Object> page = Map.of("content", List.of(imovel));
        when(propertyCatalogClient.buscarComFiltros(params)).thenReturn(Mono.just(page));
        when(userProfileClient.listarFavoritos("u1"))
                .thenReturn(Mono.just(List.of(Map.of("imovelId", "imv1"))));

        BuscaResponse r = service.buscar(params, "u1");

        assertThat(((Map<?, ?>) r.imoveis().get(0)).get("favoritado")).isEqualTo(true);
    }

    @Test
    @DisplayName("Deve degradar quando user-profile falha")
    void userProfileFalha() {
        Map<String, String> params = Map.of();
        when(propertyCatalogClient.buscarComFiltros(params))
                .thenReturn(Mono.just(Map.of("content", List.of())));
        when(userProfileClient.listarFavoritos("u1"))
                .thenReturn(Mono.error(new RuntimeException("x")));

        BuscaResponse r = service.buscar(params, "u1");

        assertThat(r.servicosIndisponiveis()).contains("user-profile");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando content ausente ou inválido")
    void contentAusente() {
        Map<String, String> params = Map.of();
        when(propertyCatalogClient.buscarComFiltros(params)).thenReturn(Mono.just(Map.of()));

        BuscaResponse r = service.buscar(params, null);

        assertThat(r.imoveis()).isEmpty();
        assertThat(r.totalPages()).isZero();
        assertThat(r.totalElements()).isZero();
    }
}
