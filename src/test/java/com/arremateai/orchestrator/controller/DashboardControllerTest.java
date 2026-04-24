package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.DashboardResponse;
import com.arremateai.orchestrator.exception.GlobalExceptionHandler;
import com.arremateai.orchestrator.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void obterDashboard() throws Exception {
        when(dashboardService.montarDashboard("u1"))
                .thenReturn(new DashboardResponse(
                        Map.of("nome", "Ana"),
                        new DashboardResponse.ContadoresDTO(2, 1, 3),
                        List.of()));

        mockMvc.perform(get("/api/orchestrator/dashboard").header("X-User-Id", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contadores.favoritos").value(2));
    }

    @Test
    void obterDashboardSemHeaderRetorna403() throws Exception {
        mockMvc.perform(get("/api/orchestrator/dashboard").header("X-User-Id", " "))
                .andExpect(status().isForbidden());
    }
}
