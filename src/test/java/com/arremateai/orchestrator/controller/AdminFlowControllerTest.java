package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.AdminDashboardResponse;
import com.arremateai.orchestrator.dto.response.VendedorFlowResponse;
import com.arremateai.orchestrator.exception.GlobalExceptionHandler;
import com.arremateai.orchestrator.service.AdminFlowService;
import com.arremateai.orchestrator.service.VendedorFlowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminFlowController.class)
@Import(GlobalExceptionHandler.class)
class AdminFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminFlowService adminFlowService;

    @MockBean
    private VendedorFlowService vendedorFlowService;

    @Test
    void dashboardComAdmin() throws Exception {
        when(adminFlowService.montarDashboardAdmin(eq("a1"), eq("ADMIN")))
                .thenReturn(new AdminDashboardResponse(Map.of(), Map.of(), List.of(), List.of()));

        mockMvc.perform(get("/api/orchestrator/admin/dashboard")
                        .header("X-User-Id", "a1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void dashboardSemAdminRetorna403() throws Exception {
        mockMvc.perform(get("/api/orchestrator/admin/dashboard")
                        .header("X-User-Id", "u1")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboardSemUserIdRetorna403() throws Exception {
        mockMvc.perform(get("/api/orchestrator/admin/dashboard")
                        .header("X-User-Id", " ")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    void aprovarVendedor() throws Exception {
        UUID id = UUID.randomUUID();
        when(vendedorFlowService.aprovar(eq(id), any(), eq("a1"), eq("ADMIN")))
                .thenReturn(new VendedorFlowResponse(Map.of("status", "aprovado"), true));

        mockMvc.perform(put("/api/orchestrator/admin/vendedores/" + id + "/aprovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("obs", "ok")))
                        .header("X-User-Id", "a1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado.status").value("aprovado"));
    }

    @Test
    void aprovarVendedorSemBody() throws Exception {
        UUID id = UUID.randomUUID();
        when(vendedorFlowService.aprovar(eq(id), any(), any(), any()))
                .thenReturn(new VendedorFlowResponse(Map.of(), true));

        mockMvc.perform(put("/api/orchestrator/admin/vendedores/" + id + "/aprovar")
                        .header("X-User-Id", "a1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void rejeitarVendedor() throws Exception {
        UUID id = UUID.randomUUID();
        when(vendedorFlowService.rejeitar(eq(id), any(), eq("a1"), eq("ADMIN")))
                .thenReturn(new VendedorFlowResponse(Map.of("status", "rejeitado"), true));

        mockMvc.perform(put("/api/orchestrator/admin/vendedores/" + id + "/rejeitar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("motivo", "x")))
                        .header("X-User-Id", "a1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }
}
