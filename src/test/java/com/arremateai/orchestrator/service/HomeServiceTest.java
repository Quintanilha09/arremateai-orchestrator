package com.arremateai.orchestrator.service;

import com.arremateai.orchestrator.client.PropertyCatalogClient;
import com.arremateai.orchestrator.dto.response.HomeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    @Mock
    private PropertyCatalogClient propertyCatalogClient;

    @InjectMocks
    private HomeService service;

    @Test
    @DisplayName("Deve compor home com destaques, recentes e estatisticas")
    void deveCompor() {
        when(propertyCatalogClient.buscarDestaques(6)).thenReturn(Mono.just(List.of((Object) "d1")));
        when(propertyCatalogClient.buscarRecentes(6)).thenReturn(Mono.just(List.of((Object) "r1")));
        when(propertyCatalogClient.buscarEstatisticas()).thenReturn(Mono.just(Map.of("total", 10)));

        HomeResponse response = service.montarHome();

        assertThat(response.destaques()).containsExactly("d1");
        assertThat(response.recentes()).containsExactly("r1");
        assertThat(response.estatisticas()).containsEntry("total", 10);
    }
}
