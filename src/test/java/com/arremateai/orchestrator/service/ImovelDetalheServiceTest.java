package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.client.UserProfileClient;
import com.arremateai.orchestrator.dto.response.ImovelDetalheResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImovelDetalheServiceTest {

    @Mock
    private PropertyCatalogClient propertyCatalogClient;
    @Mock
    private UserProfileClient userProfileClient;

    @InjectMocks
    private ImovelDetalheService service;

    @Test
    @DisplayName("Deve retornar detalhe sem favoritado quando userId é nulo")
    void semUserId() {
        UUID id = UUID.randomUUID();
        when(propertyCatalogClient.buscarImovelPorId(id)).thenReturn(Mono.just(Map.of("id", id.toString())));

        ImovelDetalheResponse r = service.montarDetalhe(id, null);

        assertThat(r.favoritado()).isFalse();
        assertThat(r.servicosIndisponiveis()).isEmpty();
    }

    @Test
    @DisplayName("Deve marcar favoritado=true quando user-profile responde")
    void comFavorito() {
        UUID id = UUID.randomUUID();
        when(propertyCatalogClient.buscarImovelPorId(id)).thenReturn(Mono.just(Map.of()));
        when(userProfileClient.verificarFavorito(id, "u1"))
                .thenReturn(Mono.just(Map.of("favoritado", true)));

        ImovelDetalheResponse r = service.montarDetalhe(id, "u1");

        assertThat(r.favoritado()).isTrue();
    }

    @Test
    @DisplayName("Deve marcar user-profile indisponível quando client falha")
    void userProfileDown() {
        UUID id = UUID.randomUUID();
        when(propertyCatalogClient.buscarImovelPorId(id)).thenReturn(Mono.just(Map.of()));
        when(userProfileClient.verificarFavorito(id, "u1"))
                .thenReturn(Mono.error(new RuntimeException("timeout")));

        ImovelDetalheResponse r = service.montarDetalhe(id, "u1");

        assertThat(r.favoritado()).isFalse();
        assertThat(r.servicosIndisponiveis()).contains("user-profile");
    }
}
