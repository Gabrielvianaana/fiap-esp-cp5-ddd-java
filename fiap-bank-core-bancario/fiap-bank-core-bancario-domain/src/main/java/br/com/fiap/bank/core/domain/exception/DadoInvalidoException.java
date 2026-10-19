package br.com.fiap.bank.core.domain.exception;

/**
 * Um dado informado viola uma regra de formacao do dominio (CPF, e-mail, valor, agencia...).
 */
public class DadoInvalidoException extends DomainException {

    public DadoInvalidoException(String message) {
        super(message);
    }
}
