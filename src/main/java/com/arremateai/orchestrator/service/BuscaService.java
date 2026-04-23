package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.client.UserProfileClient;
import com.arremateai.orchestrator.dto.response.BuscaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuscaService {

    private final PropertyCatalogClient propertyCatalogClient;
    private final UserProfileClient userProfileClient;

    public BuscaResponse buscar(Map<String, String> parametros, String userId) {
        log.debug("Executando busca com {} filtros, userId={}", parametros.size(), userId);

        List<String> servicosIndisponiveis = new CopyOnWriteArrayList<>();

        Mono<Map<String, Object>> resultadoMono = propertyCatalogClient.buscarComFiltros(parametros);
        Mono<Set<String>> favoritoIdsMono = montarFavoritoIdsMono(userId, servicosIndisponiveis);

        var resultado = Mono.zip(resultadoMono, favoritoIdsMono).block();

        Map<String, Object> paginacao = resultado.getT1();
        Set<String> favoritoIds = resultado.getT2();

        List<Object> conteudo = extrairConteudo(paginacao);
        enriquecerComFavoritos(conteudo, favoritoIds);

        return new BuscaResponse(
                conteudo,
                extrairInt(paginacao, "number"),
                extrairInt(paginacao, "totalPages"),
                extrairLong(paginacao, "totalElements"),
                servicosIndisponiveis
        );
    }

    private Mono<Set<String>> montarFavoritoIdsMono(String userId, List<String> servicosIndisponiveis) {
        if (userId == null || userId.isBlank()) {
            return Mono.just(Set.of());
        }

        return userProfileClient.listarFavoritos(userId)
                .map(this::extrairFavoritoIds)
                .onErrorResume(e -> {
                    log.warn("Falha ao buscar favoritos (degradação parcial): {}", e.getMessage());
                    servicosIndisponiveis.add("user-profile");
                    return Mono.just(Set.of());
                });
    }

    private Set<String> extrairFavoritoIds(List<Map<String, Object>> favoritos) {
        return favoritos.stream()
                .map(f -> String.valueOf(f.get("imovelId")))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private List<Object> extrairConteudo(Map<String, Object> paginacao) {
        Object content = paginacao.get("content");
        if (content instanceof List<?> lista) {
            return new ArrayList<>(lista);
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private void enriquecerComFavoritos(List<Object> conteudo, Set<String> favoritoIds) {
        if (favoritoIds.isEmpty()) {
            return;
        }

        conteudo.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .forEach(imovel -> {
                    String id = String.valueOf(imovel.get("id"));
                    imovel.put("favoritado", favoritoIds.contains(id));
                });
    }

    private int extrairInt(Map<String, Object> mapa, String chave) {
        Object valor = mapa.getOrDefault(chave, 0);
        return valor instanceof Number numero ? numero.intValue() : 0;
    }

    private long extrairLong(Map<String, Object> mapa, String chave) {
        Object valor = mapa.getOrDefault(chave, 0L);
        return valor instanceof Number numero ? numero.longValue() : 0L;
    }
}
