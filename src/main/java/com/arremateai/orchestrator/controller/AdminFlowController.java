package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.AdminDashboardResponse;
import com.arremateai.orchestrator.dto.response.VendedorFlowResponse;
import com.arremateai.orchestrator.exception.AcessoNegadoException;
import com.arremateai.orchestrator.service.AdminFlowService;
import com.arremateai.orchestrator.service.VendedorFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orchestrator/admin")
@RequiredArgsConstructor
public class AdminFlowController {

    private static final String ROLE_ADMIN = "ADMIN";

    private final AdminFlowService adminFlowService;
    private final VendedorFlowService vendedorFlowService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> obterDashboard(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {
        validarAcessoAdmin(userId, userRole);
        return ResponseEntity.ok(adminFlowService.montarDashboardAdmin(userId, userRole));
    }

    @PutMapping("/vendedores/{id}/aprovar")
    public ResponseEntity<VendedorFlowResponse> aprovarVendedor(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> dados,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {
        validarAcessoAdmin(userId, userRole);
        return ResponseEntity.ok(vendedorFlowService.aprovar(id,
                dados != null ? dados : Map.of(), userId, userRole));
    }

    @PutMapping("/vendedores/{id}/rejeitar")
    public ResponseEntity<VendedorFlowResponse> rejeitarVendedor(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> dados,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {
        validarAcessoAdmin(userId, userRole);
        return ResponseEntity.ok(vendedorFlowService.rejeitar(id,
                dados != null ? dados : Map.of(), userId, userRole));
    }

    private void validarAcessoAdmin(String userId, String userRole) {
        if (userId == null || userId.isBlank()) {
            throw new AcessoNegadoException("Header X-User-Id é obrigatório");
        }
        if (!ROLE_ADMIN.equalsIgnoreCase(userRole)) {
            throw new AcessoNegadoException("Acesso restrito a administradores");
        }
    }
}
