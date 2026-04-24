package com.arremateai.orchestrator.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Tratador global de exceções no formato RFC 7807 (application/problem+json).
 *
 * <p>Converte exceções não tratadas em respostas {@link ProblemDetail}
 * padronizadas, com tipo URN {@code urn:arremateai:error:*}, título, detalhe,
 * instância, timestamp e path. Preserva regras específicas do orquestrador
 * (RN-12 acesso negado; RN-13 serviço indisponível; RN-14 erros de downstream
 * via WebClient). Exceções 4xx são logadas em WARN; 5xx em ERROR. O corpo
 * de 5xx nunca expõe detalhes internos ou bodies brutos de serviços internos.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TYPE_PREFIX = "urn:arremateai:error:";
    private static final String DETALHE_INTERNO = "Erro interno no serviço de orquestração";
    private static final String DETALHE_VALIDACAO = "Um ou mais campos não passaram na validação.";

    @ExceptionHandler(AcessoNegadoException.class)
    public ProblemDetail handleAcessoNegado(AcessoNegadoException ex, HttpServletRequest requisicao) {
        log.warn("Acesso negado em {}: {}", requisicao.getRequestURI(), ex.getMessage());
        return construirProblema(HttpStatus.FORBIDDEN, "Acesso negado",
                ex.getMessage(), "forbidden", requisicao);
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ProblemDetail handleServicoIndisponivel(ServicoIndisponivelException ex,
                                                   HttpServletRequest requisicao) {
        log.error("Serviço downstream indisponível em {}: {}", requisicao.getRequestURI(),
                ex.getNomeServico(), ex);
        ProblemDetail problema = construirProblema(HttpStatus.SERVICE_UNAVAILABLE,
                "Serviço indisponível", ex.getMessage(), "service-unavailable", requisicao);
        problema.setProperty("service", ex.getNomeServico());
        return problema;
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ProblemDetail handleWebClient(WebClientResponseException ex, HttpServletRequest requisicao) {
        HttpStatusCode statusCode = ex.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        if (status.is5xxServerError()) {
            log.error("Erro 5xx de downstream em {}: status={}", requisicao.getRequestURI(),
                    statusCode.value(), ex);
            return construirProblema(HttpStatus.BAD_GATEWAY, "Erro em serviço downstream",
                    "Serviço downstream retornou erro.", "bad-gateway", requisicao);
        }
        log.warn("Erro {} de downstream em {}", statusCode.value(), requisicao.getRequestURI());
        return construirProblema(status, "Erro em chamada downstream",
                Optional.ofNullable(ex.getMessage()).orElse("Erro na chamada ao serviço downstream."),
                "downstream", requisicao);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest requisicao) {
        log.warn("Argumento inválido em {}: {}", requisicao.getRequestURI(), ex.getMessage());
        return construirProblema(HttpStatus.BAD_REQUEST, "Argumento inválido",
                ex.getMessage(), "illegal-argument", requisicao);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex, HttpServletRequest requisicao) {
        log.warn("Estado inválido em {}: {}", requisicao.getRequestURI(), ex.getMessage());
        return construirProblema(HttpStatus.CONFLICT, "Operação em conflito com o estado atual",
                ex.getMessage(), "conflict", requisicao);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest requisicao) {
        List<Map<String, String>> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(campo -> Map.of(
                        "field", campo.getField(),
                        "message", Optional.ofNullable(campo.getDefaultMessage()).orElse("inválido")))
                .toList();
        log.warn("Validação falhou em {}: {} erro(s)", requisicao.getRequestURI(), erros.size());
        ProblemDetail problema = construirProblema(HttpStatus.BAD_REQUEST,
                "Dados de entrada inválidos", DETALHE_VALIDACAO, "validation", requisicao);
        problema.setProperty("errors", erros);
        return problema;
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest requisicao) {
        String nomeHeader = ex.getHeaderName();
        if (nomeHeader != null && nomeHeader.startsWith("X-User-")) {
            log.warn("Header de autenticação ausente em {}: {}", requisicao.getRequestURI(), nomeHeader);
            return construirProblema(HttpStatus.UNAUTHORIZED, "Autenticação necessária",
                    "Header obrigatório ausente: " + nomeHeader, "unauthenticated", requisicao);
        }
        log.warn("Header obrigatório ausente em {}: {}", requisicao.getRequestURI(), nomeHeader);
        return construirProblema(HttpStatus.BAD_REQUEST, "Requisição inválida",
                ex.getMessage(), "illegal-argument", requisicao);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest requisicao) {
        log.error("Erro interno não tratado em {}", requisicao.getRequestURI(), ex);
        return construirProblema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor",
                DETALHE_INTERNO, "internal", requisicao);
    }

    private ProblemDetail construirProblema(HttpStatus status, String titulo, String detalhe,
                                             String tipoSufixo, HttpServletRequest requisicao) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status,
                detalhe == null ? "" : detalhe);
        problema.setTitle(titulo);
        problema.setType(URI.create(TYPE_PREFIX + tipoSufixo));
        problema.setInstance(URI.create(requisicao.getRequestURI()));
        problema.setProperty("timestamp", OffsetDateTime.now().toString());
        problema.setProperty("path", requisicao.getRequestURI());
        return problema;
    }
}
