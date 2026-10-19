package br.com.fiap.bank.plataforma.domain.exception;

import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;

/**
 * Invariante do ciclo de vida: somente propostas pendentes de analise podem receber
 * decisao discricionaria da Mesa ou ser canceladas.
 */
public class TransicaoStatusInvalidaException extends DomainException {

    public TransicaoStatusInvalidaException(Long id, StatusSolicitacao atual, String operacao) {
        super("A solicitacao #" + id + " esta com status " + atual + " e nao pode ser " + operacao + ".");
    }
}
