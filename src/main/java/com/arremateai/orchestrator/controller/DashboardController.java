package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.DashboardResponse;
import com.arremateai.orchestrator.exception.AcessoNegadoException;
import com.arremateai.orchestrator.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> obterDashboard(
            @RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isBlank()) {
            throw new AcessoNegadoException("Header X-User-Id é obrigatório");
        }
        return ResponseEntity.ok(dashboardService.montarDashboard(userId));
    }
}
