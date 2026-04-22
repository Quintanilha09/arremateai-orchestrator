package com.arremateai.orchestrator.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record AdminDashboardResponse(
        Map<String, Object> vendedores,
        Map<String, Object> imoveis,
        List<Object> vendedoresPendentes,
        @JsonProperty("_servicosIndisponiveis")
        List<String> servicosIndisponiveis
) {}
