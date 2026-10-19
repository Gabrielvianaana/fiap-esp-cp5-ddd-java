package br.com.fiap.bank.core.application.exception;

/** Dado de entrada rejeitado por uma regra do dominio. Traduzido para HTTP 400. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }
}
