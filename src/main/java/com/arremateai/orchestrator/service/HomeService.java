package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.dto.response.HomeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final PropertyCatalogClient propertyCatalogClient;

    public HomeResponse montarHome() {
        log.debug("Montando página home com chamadas paralelas");

        Mono<List<Object>> destaquesMono = propertyCatalogClient.buscarDestaques(6);
        Mono<List<Object>> recentesMono = propertyCatalogClient.buscarRecentes(6);
        Mono<Map<String, Object>> estatisticasMono = propertyCatalogClient.buscarEstatisticas();

        var resultado = Mono.zip(destaquesMono, recentesMono, estatisticasMono).block();

        return new HomeResponse(
                resultado.getT1(),
                resultado.getT2(),
                resultado.getT3()
        );
    }
}
