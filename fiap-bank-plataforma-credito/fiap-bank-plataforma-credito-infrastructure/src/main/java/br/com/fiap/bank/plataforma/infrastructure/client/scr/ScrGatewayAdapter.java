package br.com.fiap.bank.plataforma.infrastructure.client.scr;

import br.com.fiap.bank.plataforma.domain.gateway.ScrGateway;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import org.springframework.stereotype.Component;

/**
 * Adapter simulado do SCR (Banco Central).
 * Regra de simulacao herdada do legado: CPFs terminados em 9 possuem restricao.
 * Em producao, aqui ficaria o client HTTP/mensageria para o SCR.
 */
@Component
public class ScrGatewayAdapter implements ScrGateway {

    @Override
    public boolean possuiRestricoes(Cpf cpf) {
        return cpf.valor().endsWith("9");
    }
}
