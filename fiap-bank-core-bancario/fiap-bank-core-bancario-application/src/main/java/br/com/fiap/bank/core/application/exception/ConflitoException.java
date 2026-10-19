package br.com.fiap.bank.core.application.exception;

/**
 * A requisicao e valida, mas conflita com o estado atual do recurso
 * (CPF duplicado, saldo insuficiente, conta inativa...). Traduzido para HTTP 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String message) {
        super(message);
    }
}
