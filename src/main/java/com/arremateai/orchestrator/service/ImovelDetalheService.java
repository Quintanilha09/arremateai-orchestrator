package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.client.UserProfileClient;
import com.arremateai.orchestrator.dto.response.ImovelDetalheResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImovelDetalheService {

    private final PropertyCatalogClient propertyCatalogClient;
    private final UserProfileClient userProfileClient;

    public ImovelDetalheResponse montarDetalhe(UUID imovelId, String userId) {
        log.debug("Montando detalhe do imóvel {} para userId={}", imovelId, userId);

        List<String> servicosIndisponiveis = new CopyOnWriteArrayList<>();

        Mono<Map<String, Object>> imovelMono = propertyCatalogClient.buscarImovelPorId(imovelId);

        Mono<Boolean> favoritadoMono = montarFavoritadoMono(imovelId, userId, servicosIndisponiveis);

        var resultado = Mono.zip(imovelMono, favoritadoMono).block();

        return new ImovelDetalheResponse(
                resultado.getT1(),
                resultado.getT2(),
                servicosIndisponiveis
        );
    }

    private Mono<Boolean> montarFavoritadoMono(UUID imovelId, String userId,
                                                List<String> servicosIndisponiveis) {
        if (userId == null || userId.isBlank()) {
            return Mono.just(false);
        }

        return userProfileClient.verificarFavorito(imovelId, userId)
                .map(resposta -> Boolean.TRUE.equals(resposta.get("favoritado")))
                .onErrorResume(e -> {
                    log.warn("Falha ao verificar favorito (degradação parcial): {}", e.getMessage());
                    servicosIndisponiveis.add("user-profile");
                    return Mono.just(false);
                });
    }
}
