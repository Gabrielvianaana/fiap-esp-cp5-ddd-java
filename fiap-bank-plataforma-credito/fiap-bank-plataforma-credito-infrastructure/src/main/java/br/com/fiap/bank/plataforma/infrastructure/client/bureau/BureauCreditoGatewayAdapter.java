package br.com.fiap.bank.plataforma.infrastructure.client.bureau;

import br.com.fiap.bank.plataforma.domain.gateway.BureauCreditoGateway;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import org.springframework.stereotype.Component;

/**
 * Adapter simulado do Bureau de Credito (Serasa/SPC).
 * Gera um score deterministico entre 400 e 950 a partir dos digitos do CPF (mesma regra do legado).
 */
@Component
public class BureauCreditoGatewayAdapter implements BureauCreditoGateway {

    @Override
    public int consultarScore(Cpf cpf) {
        int soma = 0;
        for (char c : cpf.valor().toCharArray()) {
            soma += Character.getNumericValue(c);
        }
        return 400 + (soma * 9) % 551;
    }
}
