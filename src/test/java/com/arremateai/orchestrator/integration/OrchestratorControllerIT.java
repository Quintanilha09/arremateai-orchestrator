package com.arremateai.orchestrator.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ITs de contrato do orchestrator: verificam rotas, autenticação e RBAC
 * sem depender de serviços downstream (são testes que falham cedo na validação).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrchestratorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /actuator/health retorna 200")
    void deveResponderHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/orchestrator/admin/dashboard sem X-User-Id retorna 401")
    void deveRejeitarAdminDashboardSemUserId() throws Exception {
        mockMvc.perform(get("/api/orchestrator/admin/dashboard")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/orchestrator/admin/dashboard sem X-User-Role retorna 401")
    void deveRejeitarAdminDashboardSemUserRole() throws Exception {
        mockMvc.perform(get("/api/orchestrator/admin/dashboard")
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/orchestrator/admin/dashboard com role não-ADMIN retorna 403")
    void deveRejeitarAdminDashboardRoleInvalido() throws Exception {
        mockMvc.perform(get("/api/orchestrator/admin/dashboard")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /admin/vendedores/{id}/aprovar sem X-User-Id retorna 401")
    void deveRejeitarAprovarSemUserId() throws Exception {
        mockMvc.perform(put("/api/orchestrator/admin/vendedores/{id}/aprovar", UUID.randomUUID())
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /admin/vendedores/{id}/aprovar com role não-ADMIN retorna 403")
    void deveRejeitarAprovarRoleInvalido() throws Exception {
        mockMvc.perform(put("/api/orchestrator/admin/vendedores/{id}/aprovar", UUID.randomUUID())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /admin/vendedores/{id}/rejeitar com role não-ADMIN retorna 403")
    void deveRejeitarRejeitarRoleInvalido() throws Exception {
        mockMvc.perform(put("/api/orchestrator/admin/vendedores/{id}/rejeitar", UUID.randomUUID())
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "VENDOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/orchestrator/dashboard sem X-User-Id retorna 401")
    void deveRejeitarDashboardSemUserId() throws Exception {
        mockMvc.perform(get("/api/orchestrator/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /admin/vendedores/{id}/rejeitar sem X-User-Id retorna 401")
    void deveRejeitarRejeitarSemUserId() throws Exception {
        mockMvc.perform(put("/api/orchestrator/admin/vendedores/{id}/rejeitar", UUID.randomUUID())
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/orchestrator/vendedor/registrar com body vazio retorna 403")
    void deveRejeitarRegistrarBodyVazio() throws Exception {
        mockMvc.perform(post("/api/orchestrator/vendedor/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/orchestrator/dashboard com X-User-Id em branco retorna 403")
    void deveRejeitarDashboardUserIdBlank() throws Exception {
        mockMvc.perform(get("/api/orchestrator/dashboard")
                        .header("X-User-Id", ""))
                .andExpect(status().isForbidden());
    }
}
