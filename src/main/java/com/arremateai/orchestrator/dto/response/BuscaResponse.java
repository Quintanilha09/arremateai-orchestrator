package com.arremateai.orchestrator.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record BuscaResponse(
        List<Object> imoveis,
        int page,
        int totalPages,
        long totalElements,
        @JsonProperty("_servicosIndisponiveis")
        List<String> servicosIndisponiveis
) {}
