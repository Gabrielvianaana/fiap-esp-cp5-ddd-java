package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.domain.valueobject.HistoricoRelacionamento;

/** Porta para o CRM corporativo (historico de relacionamento do cliente). */
public interface CrmGateway {

    HistoricoRelacionamento consultarHistorico(Cpf cpf);
}
