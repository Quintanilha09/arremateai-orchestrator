package com.arremateai.orchestrator.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        Map<String, Object> perfil,
        ContadoresDTO contadores,
        @JsonProperty("_servicosIndisponiveis")
        List<String> servicosIndisponiveis
) {
    public record ContadoresDTO(
            long favoritos,
            long meusAnuncios,
            long notificacoesNaoLidas
    ) {}
}
