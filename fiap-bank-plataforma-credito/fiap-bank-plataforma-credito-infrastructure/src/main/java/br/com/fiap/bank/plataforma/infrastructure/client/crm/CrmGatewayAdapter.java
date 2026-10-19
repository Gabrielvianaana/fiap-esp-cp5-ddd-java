package br.com.fiap.bank.plataforma.infrastructure.client.crm;

import br.com.fiap.bank.plataforma.domain.gateway.CrmGateway;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.domain.valueobject.HistoricoRelacionamento;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Adapter simulado do CRM corporativo.
 * O legado nao consultava o CRM, apesar de o fluxo de negocio exigir. Base simulada:
 * os clientes de demonstracao sao "da casa" e nenhum possui pendencia interna.
 */
@Component
public class CrmGatewayAdapter implements CrmGateway {

    private static final Set<String> CLIENTES_DA_CASA = Set.of("12345678900", "98765432100", "55544433399");

    @Override
    public HistoricoRelacionamento consultarHistorico(Cpf cpf) {
        return new HistoricoRelacionamento(CLIENTES_DA_CASA.contains(cpf.valor()), false);
    }
}
