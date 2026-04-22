package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.NotificationClient;
import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.client.UserProfileClient;
import com.arremateai.orchestrator.dto.response.DashboardResponse;
import com.arremateai.orchestrator.dto.response.DashboardResponse.ContadoresDTO;
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
public class DashboardService {

    private final UserProfileClient userProfileClient;
    private final PropertyCatalogClient propertyCatalogClient;
    private final NotificationClient notificationClient;

    public DashboardResponse montarDashboard(String userId) {
        log.debug("Montando dashboard para userId={}", userId);

        List<String> servicosIndisponiveis = new CopyOnWriteArrayList<>();

        Mono<Map<String, Object>> perfilMono = userProfileClient.buscarPerfil(userId)
                .onErrorResume(e -> {
                    log.warn("Falha ao buscar perfil: {}", e.getMessage());
                    servicosIndisponiveis.add("user-profile");
                    return Mono.just(Map.of());
                });

        Mono<Long> favoritosMono = userProfileClient.contarFavoritos(userId)
                .map(m -> extrairContagem(m, "total"))
                .onErrorResume(e -> {
                    log.warn("Falha ao contar favoritos: {}", e.getMessage());
                    servicosIndisponiveis.add("user-profile");
                    return Mono.just(0L);
                });

        Mono<Long> meusAnunciosMono = propertyCatalogClient.buscarMeusImoveis(userId, 0, 1)
                .map(m -> extrairContagem(m, "totalElements"))
                .onErrorResume(e -> {
                    log.warn("Falha ao contar meus anúncios: {}", e.getMessage());
                    servicosIndisponiveis.add("property-catalog");
                    return Mono.just(0L);
                });

        Mono<Long> notificacoesMono = notificationClient.contarNaoLidas(userId)
                .map(m -> extrairContagem(m, "total"))
                .onErrorResume(e -> {
                    log.warn("Falha ao contar notificações: {}", e.getMessage());
                    servicosIndisponiveis.add("notification");
                    return Mono.just(0L);
                });

        var resultado = Mono.zip(perfilMono, favoritosMono, meusAnunciosMono, notificacoesMono).block();

        return new DashboardResponse(
                resultado.getT1(),
                new ContadoresDTO(resultado.getT2(), resultado.getT3(), resultado.getT4()),
                servicosIndisponiveis
        );
    }

    private long extrairContagem(Map<String, ?> mapa, String chave) {
        Object valor = mapa.get(chave);
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        return 0L;
    }
}
