package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.VendedorFlowResponse;
import com.arremateai.orchestrator.exception.GlobalExceptionHandler;
import com.arremateai.orchestrator.service.VendedorFlowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VendedorFlowController.class)
@Import(GlobalExceptionHandler.class)
class VendedorFlowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VendedorFlowService vendedorFlowService;

    @Test
    void registrar() throws Exception {
        when(vendedorFlowService.registrar(any()))
                .thenReturn(new VendedorFlowResponse(Map.of("id", "v1"), true));

        mockMvc.perform(post("/api/orchestrator/vendedor/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nome", "X"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacaoEnviada").value(true));
    }

    @Test
    void registrarVazioRetorna403() throws Exception {
        mockMvc.perform(post("/api/orchestrator/vendedor/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
