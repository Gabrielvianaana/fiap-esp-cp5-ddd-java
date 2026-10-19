package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;

/** Porta para o Bureau de Credito (Serasa/SPC). */
public interface BureauCreditoGateway {

    /** Score de risco do cliente, de 0 a 1000. */
    int consultarScore(Cpf cpf);
}
