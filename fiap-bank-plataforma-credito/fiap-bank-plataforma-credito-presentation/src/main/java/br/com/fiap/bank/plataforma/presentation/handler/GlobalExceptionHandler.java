package br.com.fiap.bank.plataforma.presentation.handler;

import br.com.fiap.bank.plataforma.application.exception.ConflitoException;
import br.com.fiap.bank.plataforma.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.plataforma.application.exception.RegraNegocioException;
import br.com.fiap.bank.plataforma.application.exception.ServicoIndisponivelException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Tratamento global de excecoes (RFC 9457 - Problem Details).
 * No legado, "nao encontrado" virava IllegalArgumentException -> HTTP 500. Agora cada falha tem o status correto.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "campo", fe.getField(),
                        "mensagem", fe.getDefaultMessage() == null ? "valor invalido" : fe.getDefaultMessage()))
                .toList();
        ProblemDetail problema = criar(HttpStatus.BAD_REQUEST, "Dados de entrada invalidos",
                "Um ou mais campos nao passaram na validacao.", request);
        problema.setProperty("erros", erros);
        return responder(problema);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> tratarValidacaoParametros(HandlerMethodValidationException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.BAD_REQUEST, "Parametros invalidos", ex.getMessage(), request));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> tratarCorpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido",
                "O JSON enviado esta ausente, mal formatado ou contem valores incompativeis.", request));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> tratarTipoParametro(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.BAD_REQUEST, "Parametro invalido",
                "O valor '" + ex.getValue() + "' nao e valido para o parametro '" + ex.getName() + "'.", request));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ProblemDetail> tratarRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.BAD_REQUEST, "Regra de negocio violada", ex.getMessage(), request));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage(), request));
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ProblemDetail> tratarConflito(ConflitoException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.CONFLICT, "Conflito com o estado atual do recurso", ex.getMessage(), request));
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ProblemDetail> tratarIndisponivel(ServicoIndisponivelException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.SERVICE_UNAVAILABLE, "Servico dependente indisponivel", ex.getMessage(), request));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> tratarRotaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.NOT_FOUND, "Rota nao encontrada", "Nao existe recurso em " + request.getRequestURI() + ".", request));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return responder(criar(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP nao suportado", ex.getMessage(), request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(criar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request));
    }

    private ProblemDetail criar(HttpStatus status, String titulo, String detalhe, HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setInstance(URI.create(request.getRequestURI()));
        problema.setProperty("timestamp", OffsetDateTime.now().toString());
        return problema;
    }

    private ResponseEntity<ProblemDetail> responder(ProblemDetail problema) {
        return ResponseEntity.status(problema.getStatus()).body(problema);
    }
}
