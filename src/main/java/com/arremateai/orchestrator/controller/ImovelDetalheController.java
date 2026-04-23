package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.ImovelDetalheResponse;
import com.arremateai.orchestrator.service.ImovelDetalheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class ImovelDetalheController {

    private final ImovelDetalheService imovelDetalheService;

    @GetMapping("/imoveis/{id}")
    public ResponseEntity<ImovelDetalheResponse> obterDetalhe(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(imovelDetalheService.montarDetalhe(id, userId));
    }
}
