package com.arremateai.orchestrator.dto.response;

import java.util.Map;

public record VendedorFlowResponse(
        Map<String, Object> resultado,
        boolean notificacaoEnviada
) {}
