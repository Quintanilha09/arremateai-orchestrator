package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.HomeResponse;
import com.arremateai.orchestrator.service.HomeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeService homeService;

    @Test
    void obterHome() throws Exception {
        when(homeService.montarHome())
                .thenReturn(new HomeResponse(List.of("d"), List.of("r"), Map.of("total", 1)));

        mockMvc.perform(get("/api/orchestrator/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destaques[0]").value("d"));
    }
}
