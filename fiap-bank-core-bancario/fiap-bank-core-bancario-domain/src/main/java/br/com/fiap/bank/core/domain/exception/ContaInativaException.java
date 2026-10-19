package br.com.fiap.bank.core.domain.exception;

import br.com.fiap.bank.core.domain.model.StatusConta;

/**
 * Operacoes financeiras so podem ser realizadas em contas com status ATIVA.
 */
public class ContaInativaException extends DomainException {

    public ContaInativaException(String numeroConta, StatusConta status) {
        super("A conta " + numeroConta + " esta com status " + status + " e nao permite movimentacoes.");
    }
}
