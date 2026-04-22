package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.client.VendorClient;
import com.arremateai.orchestrator.dto.response.AdminDashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFlowService {

    private final VendorClient vendorClient;
    private final PropertyCatalogClient propertyCatalogClient;

    @SuppressWarnings("unchecked")
    public AdminDashboardResponse montarDashboardAdmin(String userId, String userRole) {
        log.debug("Montando dashboard admin para userId={}", userId);

        List<String> servicosIndisponiveis = new CopyOnWriteArrayList<>();

        Mono<Map<String, Object>> vendedoresStatsMono = vendorClient
                .buscarEstatisticasAdmin(userId, userRole)
                .onErrorResume(e -> {
                    log.warn("Falha ao buscar stats vendedores: {}", e.getMessage());
                    servicosIndisponiveis.add("vendor");
                    return Mono.just(Map.of());
                });

        Mono<Map<String, Object>> imoveisStatsMono = propertyCatalogClient.buscarEstatisticas()
                .onErrorResume(e -> {
                    log.warn("Falha ao buscar stats imóveis: {}", e.getMessage());
                    servicosIndisponiveis.add("property-catalog");
                    return Mono.just(Map.of());
                });

        Mono<Map<String, Object>> pendentesMono = vendorClient
                .buscarPendentes(5, userId, userRole)
                .onErrorResume(e -> {
                    log.warn("Falha ao buscar vendedores pendentes: {}", e.getMessage());
                    servicosIndisponiveis.add("vendor");
                    return Mono.just(Map.of());
                });

        var resultado = Mono.zip(vendedoresStatsMono, imoveisStatsMono, pendentesMono).block();

        List<Object> pendentes = extrairConteudo(resultado.getT3());

        return new AdminDashboardResponse(
                resultado.getT1(),
                resultado.getT2(),
                pendentes,
                servicosIndisponiveis
        );
    }

    @SuppressWarnings("unchecked")
    private List<Object> extrairConteudo(Map<String, Object> paginacao) {
        Object content = paginacao.get("content");
        if (content instanceof List<?> lista) {
            return (List<Object>) lista;
        }
        return List.of();
    }
}
