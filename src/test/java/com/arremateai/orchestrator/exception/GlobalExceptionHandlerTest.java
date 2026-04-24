package com.arremateai.orchestrator.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@DisplayName("GlobalExceptionHandler — respostas RFC 7807 (ProblemDetail)")
class GlobalExceptionHandlerTest {

    private static final String URI_TESTE = "/api/orchestrator/journey";
    private static final String TIPO_PREFIXO = "urn:arremateai:error:";

    private GlobalExceptionHandler handler;
    private HttpServletRequest requisicao;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        requisicao = mock(HttpServletRequest.class);
        when(requisicao.getRequestURI()).thenReturn(URI_TESTE);
    }

    @Test
    @DisplayName("handleAcessoNegado → 403 forbidden (RN-12)")
    void handleAcessoNegadoDeveRetornar403() {
        ProblemDetail problema = handler.handleAcessoNegado(
                new AcessoNegadoException("usuário sem permissão"), requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problema.getTitle()).isEqualTo("Acesso negado");
        assertThat(problema.getDetail()).isEqualTo("usuário sem permissão");
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "forbidden");
        assertThat(problema.getInstance().toString()).isEqualTo(URI_TESTE);
        assertThat(problema.getProperties()).containsKeys("timestamp", "path");
    }

    @Test
    @DisplayName("handleServicoIndisponivel → 503 com propriedade service (RN-13)")
    void handleServicoIndisponivelDeveRetornar503ComService() {
        ProblemDetail problema = handler.handleServicoIndisponivel(
                new ServicoIndisponivelException("identity"), requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(problema.getTitle()).isEqualTo("Serviço indisponível");
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "service-unavailable");
        assertThat(problema.getProperties()).containsEntry("service", "identity");
    }

    @Test
    @DisplayName("handleWebClient → 5xx downstream vira 502 bad-gateway sem vazar body (RN-14)")
    void handleWebClientDeveMascarar5xxComo502() {
        WebClientResponseException ex = WebClientResponseException.create(
                500, "Internal Server Error", HttpHeaders.EMPTY,
                "stack trace do downstream".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        ProblemDetail problema = handler.handleWebClient(ex, requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(problema.getTitle()).isEqualTo("Erro em serviço downstream");
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "bad-gateway");
        assertThat(problema.getDetail()).doesNotContain("stack trace");
    }

    @Test
    @DisplayName("handleWebClient → propaga status 4xx do downstream como downstream")
    void handleWebClientDevePropagar4xxDoDownstream() {
        WebClientResponseException ex = WebClientResponseException.create(
                404, "Not Found", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);

        ProblemDetail problema = handler.handleWebClient(ex, requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "downstream");
    }

    @Test
    @DisplayName("handleIllegalArgument → 400 illegal-argument")
    void handleIllegalArgumentDeveRetornar400() {
        ProblemDetail problema = handler.handleIllegalArgument(
                new IllegalArgumentException("id inválido"), requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "illegal-argument");
    }

    @Test
    @DisplayName("handleIllegalState → 409 conflict")
    void handleIllegalStateDeveRetornar409() {
        ProblemDetail problema = handler.handleIllegalState(
                new IllegalStateException("jornada em andamento"), requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "conflict");
    }

    @Test
    @DisplayName("handleValidation → 400 validation com errors[]")
    @SuppressWarnings("unchecked")
    void handleValidationDeveRetornar400ComErros() {
        BindingResult br = mock(BindingResult.class);
        FieldError fe1 = new FieldError("obj", "campoA", "não pode ser vazio");
        FieldError fe2 = new FieldError("obj", "campoB", null);
        when(br.getFieldErrors()).thenReturn(List.of(fe1, fe2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(br);

        ProblemDetail problema = handler.handleValidation(ex, requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "validation");
        List<Map<String, String>> erros = (List<Map<String, String>>) problema.getProperties().get("errors");
        assertThat(erros).hasSize(2);
        assertThat(erros.get(0)).containsEntry("field", "campoA").containsEntry("message", "não pode ser vazio");
        assertThat(erros.get(1)).containsEntry("field", "campoB").containsEntry("message", "inválido");
    }

    @Test
    @DisplayName("handleMissingHeader → 401 unauthenticated para X-User-*")
    void handleMissingHeaderDeveRetornar401ParaXUser() {
        MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);
        when(ex.getHeaderName()).thenReturn("X-User-Id");
        when(ex.getMessage()).thenReturn("Required header 'X-User-Id' is not present.");

        ProblemDetail problema = handler.handleMissingHeader(ex, requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "unauthenticated");
        assertThat(problema.getDetail()).contains("X-User-Id");
    }

    @Test
    @DisplayName("handleMissingHeader → 400 illegal-argument para header comum")
    void handleMissingHeaderDeveRetornar400ParaHeaderComum() {
        MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);
        when(ex.getHeaderName()).thenReturn("Content-Type");
        when(ex.getMessage()).thenReturn("Required header 'Content-Type' is not present.");

        ProblemDetail problema = handler.handleMissingHeader(ex, requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "illegal-argument");
    }

    @Test
    @DisplayName("handleGeneric → 500 internal sem vazar stack")
    void handleGenericDeveRetornar500SemVazarDetalhes() {
        ProblemDetail problema = handler.handleGeneric(
                new RuntimeException("NullPointerException na linha 42"), requisicao);

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problema.getTitle()).isEqualTo("Erro interno do servidor");
        assertThat(problema.getType().toString()).isEqualTo(TIPO_PREFIXO + "internal");
        assertThat(problema.getDetail()).doesNotContain("NullPointerException");
    }

    @Test
    @DisplayName("handleAcessoNegado → aceita mensagem nula sem NPE")
    void handleAcessoNegadoDeveAceitarMensagemNula() {
        ProblemDetail problema = handler.handleAcessoNegado(
                new AcessoNegadoException(null), requisicao);

        assertThat(problema).isNotNull();
        assertThat(problema.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}
