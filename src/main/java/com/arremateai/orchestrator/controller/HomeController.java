package com.arremateai.orchestrator.controller;

import com.arremateai.orchestrator.dto.response.HomeResponse;
import com.arremateai.orchestrator.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/home")
    public ResponseEntity<HomeResponse> obterHome() {
        return ResponseEntity.ok(homeService.montarHome());
    }
}
