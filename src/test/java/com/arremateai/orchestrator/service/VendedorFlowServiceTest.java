package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.NotificationClient;
import com.arremateai.orchestrator.client.VendorClient;
import com.arremateai.orchestrator.dto.response.VendedorFlowResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendedorFlowServiceTest {

    @Mock
    private VendorClient vendorClient;
    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private VendedorFlowService service;

    private final UUID adminId = UUID.randomUUID();
    private final UUID vendedorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "adminUserId", adminId.toString());
    }

    @Test
    @DisplayName("Deve registrar vendedor e notificar admin")
    void registrarComAdmin() {
        Map<String, Object> dados = Map.of("nome", "X");
        when(vendorClient.registrarVendedor(dados)).thenReturn(Mono.just(Map.of("id", "v1")));
        when(notificationClient.criarNotificacaoInterna(any(UUID.class), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(Map.of()));

        VendedorFlowResponse r = service.registrar(dados);

        assertThat(r.notificacaoEnviada()).isTrue();
        verify(notificationClient).criarNotificacaoInterna(adminId, "Novo vendedor pendente",
                "Um novo vendedor se registrou e aguarda aprovação", "VENDEDOR_PENDENTE");
    }

    @Test
    @DisplayName("Deve registrar sem notificar quando adminUserId não configurado")
    void registrarSemAdmin() {
        ReflectionTestUtils.setField(service, "adminUserId", "");
        when(vendorClient.registrarVendedor(any())).thenReturn(Mono.just(Map.of()));

        VendedorFlowResponse r = service.registrar(Map.of());

        assertThat(r.notificacaoEnviada()).isFalse();
        verifyNoInteractions(notificationClient);
    }

    @Test
    @DisplayName("Deve considerar notificação não enviada quando client falha")
    void registrarNotificacaoFalha() {
        when(vendorClient.registrarVendedor(any())).thenReturn(Mono.just(Map.of()));
        when(notificationClient.criarNotificacaoInterna(any(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("x")));

        VendedorFlowResponse r = service.registrar(Map.of());

        assertThat(r.notificacaoEnviada()).isFalse();
    }

    @Test
    @DisplayName("Deve aprovar e notificar vendedor usando userId do resultado")
    void aprovarComUserIdNoResultado() {
        UUID userId = UUID.randomUUID();
        when(vendorClient.aprovarVendedor(vendedorId, Map.of(), "admin", "ADMIN"))
                .thenReturn(Mono.just(Map.of("userId", userId.toString())));
        when(notificationClient.criarNotificacaoInterna(any(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(Map.of()));

        VendedorFlowResponse r = service.aprovar(vendedorId, Map.of(), "admin", "ADMIN");

        assertThat(r.notificacaoEnviada()).isTrue();
        verify(notificationClient).criarNotificacaoInterna(userId, "Cadastro aprovado",
                "Seu cadastro como vendedor foi aprovado! Você já pode cadastrar imóveis.", "VENDEDOR_APROVADO");
    }

    @Test
    @DisplayName("Deve aprovar usando fallback para vendedorId quando userId ausente")
    void aprovarFallback() {
        when(vendorClient.aprovarVendedor(vendedorId, Map.of(), "admin", "ADMIN"))
                .thenReturn(Mono.just(Map.of()));
        when(notificationClient.criarNotificacaoInterna(any(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(Map.of()));

        service.aprovar(vendedorId, Map.of(), "admin", "ADMIN");

        verify(notificationClient).criarNotificacaoInterna(vendedorId, "Cadastro aprovado",
                "Seu cadastro como vendedor foi aprovado! Você já pode cadastrar imóveis.", "VENDEDOR_APROVADO");
    }

    @Test
    @DisplayName("Deve rejeitar e notificar vendedor")
    void rejeitar() {
        when(vendorClient.rejeitarVendedor(vendedorId, Map.of(), "admin", "ADMIN"))
                .thenReturn(Mono.just(Map.of()));
        when(notificationClient.criarNotificacaoInterna(any(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(Map.of()));

        VendedorFlowResponse r = service.rejeitar(vendedorId, Map.of(), "admin", "ADMIN");

        assertThat(r.notificacaoEnviada()).isTrue();
        verify(notificationClient).criarNotificacaoInterna(vendedorId, "Cadastro rejeitado",
                "Seu cadastro como vendedor foi rejeitado. Verifique os detalhes.", "VENDEDOR_REJEITADO");
    }

    @Test
    @DisplayName("Deve marcar notificacaoEnviada=false quando envio ao vendedor falha")
    void aprovarNotificacaoFalha() {
        when(vendorClient.aprovarVendedor(vendedorId, Map.of(), "admin", "ADMIN"))
                .thenReturn(Mono.just(Map.of()));
        when(notificationClient.criarNotificacaoInterna(any(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("x")));

        VendedorFlowResponse r = service.aprovar(vendedorId, Map.of(), "admin", "ADMIN");

        assertThat(r.notificacaoEnviada()).isFalse();
    }
}
