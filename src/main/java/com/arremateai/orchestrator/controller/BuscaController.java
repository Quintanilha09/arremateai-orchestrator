package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.BuscaResponse;
import com.arremateai.orchestrator.service.BuscaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class BuscaController {

    private final BuscaService buscaService;

    @GetMapping("/busca")
    public ResponseEntity<BuscaResponse> buscar(
            @RequestParam Map<String, String> parametros,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(buscaService.buscar(parametros, userId));
    }
}
