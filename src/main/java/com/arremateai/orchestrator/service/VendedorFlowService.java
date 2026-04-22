package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.NotificationClient;
import com.arremateai.orchestrator.client.VendorClient;
import com.arremateai.orchestrator.dto.response.VendedorFlowResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendedorFlowService {

    private final VendorClient vendorClient;
    private final NotificationClient notificationClient;

    @Value("${services.admin.user-id:}")
    private String adminUserId;

    public VendedorFlowResponse registrar(Map<String, Object> dados) {
        log.debug("Iniciando registro de vendedor");

        Map<String, Object> resultado = vendorClient.registrarVendedor(dados).block();

        boolean notificacaoEnviada = enviarNotificacaoAdmin(
                "Novo vendedor pendente",
                "Um novo vendedor se registrou e aguarda aprovação",
                "VENDEDOR_PENDENTE"
        );

        return new VendedorFlowResponse(resultado, notificacaoEnviada);
    }

    public VendedorFlowResponse aprovar(UUID vendedorId, Map<String, Object> dados,
                                        String userId, String userRole) {
        log.debug("Aprovando vendedor {} por admin {}", vendedorId, userId);

        Map<String, Object> resultado = vendorClient
                .aprovarVendedor(vendedorId, dados, userId, userRole).block();

        boolean notificacaoEnviada = enviarNotificacaoVendedor(
                vendedorId,
                resultado,
                "Cadastro aprovado",
                "Seu cadastro como vendedor foi aprovado! Você já pode cadastrar imóveis.",
                "VENDEDOR_APROVADO"
        );

        return new VendedorFlowResponse(resultado, notificacaoEnviada);
    }

    public VendedorFlowResponse rejeitar(UUID vendedorId, Map<String, Object> dados,
                                         String userId, String userRole) {
        log.debug("Rejeitando vendedor {} por admin {}", vendedorId, userId);

        Map<String, Object> resultado = vendorClient
                .rejeitarVendedor(vendedorId, dados, userId, userRole).block();

        boolean notificacaoEnviada = enviarNotificacaoVendedor(
                vendedorId,
                resultado,
                "Cadastro rejeitado",
                "Seu cadastro como vendedor foi rejeitado. Verifique os detalhes.",
                "VENDEDOR_REJEITADO"
        );

        return new VendedorFlowResponse(resultado, notificacaoEnviada);
    }

    private boolean enviarNotificacaoAdmin(String titulo, String mensagem, String tipo) {
        if (adminUserId == null || adminUserId.isBlank()) {
            log.warn("Admin userId não configurado — notificação não enviada");
            return false;
        }

        try {
            notificationClient.criarNotificacaoInterna(
                    UUID.fromString(adminUserId), titulo, mensagem, tipo
            ).block();
            return true;
        } catch (Exception e) {
            log.warn("Falha ao enviar notificação admin (best-effort): {}", e.getMessage());
            return false;
        }
    }

    private boolean enviarNotificacaoVendedor(UUID vendedorId, Map<String, Object> vendedorData,
                                              String titulo, String mensagem, String tipo) {
        try {
            UUID targetUserId = extrairUserId(vendedorData, vendedorId);
            notificationClient.criarNotificacaoInterna(targetUserId, titulo, mensagem, tipo).block();
            return true;
        } catch (Exception e) {
            log.warn("Falha ao enviar notificação vendedor (best-effort): {}", e.getMessage());
            return false;
        }
    }

    private UUID extrairUserId(Map<String, Object> vendedorData, UUID fallbackId) {
        Object userId = vendedorData.get("userId");
        if (userId != null) {
            return UUID.fromString(String.valueOf(userId));
        }
        return fallbackId;
    }
}
