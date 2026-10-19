package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.exception.IntegracaoIndisponivelException;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.domain.valueobject.PosicaoContaCore;

import java.util.Optional;

/** Porta para o Core Bancario (saldo, status e dados da conta do cliente). */
public interface CoreBancarioGateway {

    /**
     * @return a conta do cliente ou vazio se ele nao possuir conta
     * @throws IntegracaoIndisponivelException se o Core Bancario nao responder
     */
    Optional<PosicaoContaCore> consultarConta(Cpf cpf);
}
