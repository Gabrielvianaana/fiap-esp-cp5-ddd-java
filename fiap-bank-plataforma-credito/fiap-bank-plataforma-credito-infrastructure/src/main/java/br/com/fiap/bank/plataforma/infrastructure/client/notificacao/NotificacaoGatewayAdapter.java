package br.com.fiap.bank.plataforma.infrastructure.client.notificacao;

import br.com.fiap.bank.plataforma.domain.gateway.NotificacaoGateway;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter simulado do Sistema de Notificacoes (e-mail/SMS). Registra o envio em log.
 */
@Slf4j
@Component
public class NotificacaoGatewayAdapter implements NotificacaoGateway {

    @Override
    public void notificar(Cpf cpf, String mensagem) {
        log.info("[SISTEMA DE NOTIFICACOES] E-mail/SMS enviado para o CPF {}: {}", cpf.valor(), mensagem);
    }
}
