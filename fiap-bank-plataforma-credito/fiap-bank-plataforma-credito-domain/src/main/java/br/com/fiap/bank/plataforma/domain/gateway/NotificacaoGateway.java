package br.com.fiap.bank.plataforma.domain.gateway;

import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;

/** Porta para o Sistema de Notificacoes externo (e-mail/SMS). */
public interface NotificacaoGateway {

    void notificar(Cpf cpf, String mensagem);
}
