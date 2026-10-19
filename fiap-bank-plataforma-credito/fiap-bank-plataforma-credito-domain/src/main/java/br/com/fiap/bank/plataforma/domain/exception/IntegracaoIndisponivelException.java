package br.com.fiap.bank.plataforma.domain.exception;

/**
 * Um sistema externo necessario para a analise (ex.: Core Bancario) nao respondeu.
 * Diferente do legado, nao mascaramos a falha com dados ficticios.
 */
public class IntegracaoIndisponivelException extends DomainException {

    public IntegracaoIndisponivelException(String sistema, Throwable causa) {
        super("O sistema " + sistema + " esta indisponivel no momento.");
        initCause(causa);
    }
}
