package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.NotificationClient;
import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.client.UserProfileClient;
import com.arremateai.orchestrator.dto.response.DashboardResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserProfileClient userProfileClient;
    @Mock
    private PropertyCatalogClient propertyCatalogClient;
    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private DashboardService service;

    @Test
    @DisplayName("Deve montar dashboard completo quando todos os serviços respondem")
    void happyPath() {
        when(userProfileClient.buscarPerfil("u1")).thenReturn(Mono.just(Map.of("nome", "Ana")));
        when(userProfileClient.contarFavoritos("u1"))
                .thenReturn(Mono.just(Map.<String, Long>of("total", 5L)));
        when(propertyCatalogClient.buscarMeusImoveis("u1", 0, 1))
                .thenReturn(Mono.just(Map.of("totalElements", 3L)));
        when(notificationClient.contarNaoLidas("u1"))
                .thenReturn(Mono.just(Map.<String, Long>of("total", 2L)));

        DashboardResponse r = service.montarDashboard("u1");

        assertThat(r.perfil()).containsEntry("nome", "Ana");
        assertThat(r.contadores().favoritos()).isEqualTo(5L);
        assertThat(r.contadores().meusAnuncios()).isEqualTo(3L);
        assertThat(r.contadores().notificacoesNaoLidas()).isEqualTo(2L);
        assertThat(r.servicosIndisponiveis()).isEmpty();
    }

    @Test
    @DisplayName("Deve degradar parcialmente quando todos os serviços falham")
    void todosFalham() {
        when(userProfileClient.buscarPerfil("u1")).thenReturn(Mono.error(new RuntimeException("x")));
        when(userProfileClient.contarFavoritos("u1")).thenReturn(Mono.error(new RuntimeException("x")));
        when(propertyCatalogClient.buscarMeusImoveis("u1", 0, 1))
                .thenReturn(Mono.error(new RuntimeException("x")));
        when(notificationClient.contarNaoLidas("u1")).thenReturn(Mono.error(new RuntimeException("x")));

        DashboardResponse r = service.montarDashboard("u1");

        assertThat(r.perfil()).isEmpty();
        assertThat(r.contadores().favoritos()).isZero();
        assertThat(r.servicosIndisponiveis())
                .contains("user-profile", "property-catalog", "notification");
    }

    @Test
    @DisplayName("Deve retornar 0 quando contagem vem sem Number")
    void contagemInvalida() {
        when(userProfileClient.buscarPerfil("u1")).thenReturn(Mono.just(Map.of()));
        when(userProfileClient.contarFavoritos("u1")).thenReturn(Mono.just(Map.<String, Long>of()));
        when(propertyCatalogClient.buscarMeusImoveis("u1", 0, 1)).thenReturn(Mono.just(Map.of()));
        when(notificationClient.contarNaoLidas("u1")).thenReturn(Mono.just(Map.<String, Long>of()));

        DashboardResponse r = service.montarDashboard("u1");

        assertThat(r.contadores().favoritos()).isZero();
    }
}
