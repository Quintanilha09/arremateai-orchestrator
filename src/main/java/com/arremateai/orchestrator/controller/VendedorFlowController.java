package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.VendedorFlowResponse;
import com.arremateai.orchestrator.exception.AcessoNegadoException;
import com.arremateai.orchestrator.service.VendedorFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orchestrator/vendedor")
@RequiredArgsConstructor
public class VendedorFlowController {

    private final VendedorFlowService vendedorFlowService;

    @PostMapping("/registrar")
    public ResponseEntity<VendedorFlowResponse> registrar(
            @RequestBody Map<String, Object> dados) {
        if (dados == null || dados.isEmpty()) {
            throw new AcessoNegadoException("Dados de registro são obrigatórios");
        }
        return ResponseEntity.ok(vendedorFlowService.registrar(dados));
    }
}
