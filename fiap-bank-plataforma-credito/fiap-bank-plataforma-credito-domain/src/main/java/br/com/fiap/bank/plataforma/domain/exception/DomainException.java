package br.com.fiap.bank.plataforma.domain.exception;

/** Excecao base do dominio da Plataforma de Credito. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
