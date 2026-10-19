package br.com.fiap.bank.core.domain.exception;

/**
 * Excecao base do dominio. Representa a quebra de uma invariante de negocio.
 * Nao conhece HTTP nem frameworks: a traducao para status de resposta e feita nas camadas externas.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
