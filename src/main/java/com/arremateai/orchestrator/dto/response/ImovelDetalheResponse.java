package com.arremateai.orchestrator.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ImovelDetalheResponse(
        Map<String, Object> imovel,
        Boolean favoritado,
        @JsonProperty("_servicosIndisponiveis")
        List<String> servicosIndisponiveis
) {}
