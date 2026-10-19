package br.com.fiap.bank.core.application.exception;

/** O recurso solicitado nao existe (traduzido para HTTP 404 na camada de apresentacao). */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
