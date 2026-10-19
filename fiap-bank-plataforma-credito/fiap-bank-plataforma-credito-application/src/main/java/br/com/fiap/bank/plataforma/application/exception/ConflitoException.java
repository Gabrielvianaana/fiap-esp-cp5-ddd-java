package br.com.fiap.bank.plataforma.application.exception;

/** Traduzido para HTTP 409 (ex.: decidir uma proposta que ja foi decidida). */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String message) {
        super(message);
    }
}
