package com.arremateai.orchestrator.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, Object>> tratarAcessoNegado(AcessoNegadoException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(montarErro(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> tratarServicoIndisponivel(ServicoIndisponivelException ex) {
        log.error("Serviço downstream indisponível: {}", ex.getNomeServico(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(montarErro(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage()));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, Object>> tratarErroWebClient(WebClientResponseException ex) {
        log.error("Erro na chamada downstream: {} {}", ex.getStatusCode(), ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status)
                .body(montarErro(status, ex.getResponseBodyAsString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroGenerico(Exception ex) {
        log.error("Erro interno no orchestrator", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(montarErro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no serviço de orquestração"));
    }

    private Map<String, Object> montarErro(HttpStatus status, String mensagem) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", mensagem
        );
    }
}
