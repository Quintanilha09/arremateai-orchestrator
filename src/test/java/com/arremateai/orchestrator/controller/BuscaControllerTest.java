package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.BuscaResponse;
import com.arremateai.orchestrator.service.BuscaService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BuscaController.class)
class BuscaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BuscaService buscaService;

    @Test
    void buscarComUsuario() throws Exception {
        when(buscaService.buscar(any(), ArgumentMatchers.eq("u1")))
                .thenReturn(new BuscaResponse(List.of("i"), 0, 1, 1L, List.of()));

        mockMvc.perform(get("/api/orchestrator/busca")
                        .param("tipo", "casa")
                        .header("X-User-Id", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void buscarSemUsuario() throws Exception {
        when(buscaService.buscar(any(), ArgumentMatchers.isNull()))
                .thenReturn(new BuscaResponse(List.of(), 0, 0, 0L, List.of()));

        mockMvc.perform(get("/api/orchestrator/busca"))
                .andExpect(status().isOk());
    }
}
