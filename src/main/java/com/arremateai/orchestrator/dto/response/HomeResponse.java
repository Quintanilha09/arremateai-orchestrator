package com.arremateai.orchestrator.dto.response;

import java.util.List;
import java.util.Map;

public record HomeResponse(
        List<Object> destaques,
        List<Object> recentes,
        Map<String, Object> estatisticas
) {}
