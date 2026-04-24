package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.ImovelDetalheResponse;
import com.arremateai.orchestrator.service.ImovelDetalheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImovelDetalheController.class)
class ImovelDetalheControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImovelDetalheService imovelDetalheService;

    @Test
    void obterDetalhe() throws Exception {
        UUID id = UUID.randomUUID();
        when(imovelDetalheService.montarDetalhe(eq(id), eq("u1")))
                .thenReturn(new ImovelDetalheResponse(Map.of("id", id.toString()), true, List.of()));

        mockMvc.perform(get("/api/orchestrator/imoveis/" + id)
                        .header("X-User-Id", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favoritado").value(true));
    }

    @Test
    void obterDetalheSemUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        when(imovelDetalheService.montarDetalhe(any(), any()))
                .thenReturn(new ImovelDetalheResponse(Map.of(), null, List.of()));

        mockMvc.perform(get("/api/orchestrator/imoveis/" + id))
                .andExpect(status().isOk());
    }
}
