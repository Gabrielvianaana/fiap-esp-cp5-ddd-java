package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;

/** Porta para o Sistema de Informacoes de Credito (SCR) do Banco Central. */
public interface ScrGateway {

    boolean possuiRestricoes(Cpf cpf);
}
