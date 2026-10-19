package br.com.fiap.bank.plataforma.infrastructure.client.core;

import br.com.fiap.bank.plataforma.domain.exception.IntegracaoIndisponivelException;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.domain.valueobject.PosicaoContaCore;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter (camada anticorrupcao) entre o contexto de Credito e o Core Bancario.
 *
 * CORRECAO DO LEGADO: o servico antigo engolia qualquer erro e devolvia SEMPRE a conta ficticia
 * "12345-6" com saldo R$ 12.500,00, para qualquer CPF. Isso aprovava credito com base em dados falsos.
 * Agora: 404 do Core = cliente sem conta; falha de comunicacao = IntegracaoIndisponivelException.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoreBancarioGatewayAdapter implements CoreBancarioGateway {

    private static final String SISTEMA = "Core Bancario";

    private final CoreBancarioFeignClient feignClient;

    @Override
    public Optional<PosicaoContaCore> consultarConta(Cpf cpf) {
        try {
            ContaCoreResponse conta = feignClient.buscarContaPorCpf(cpf.valor());
            return Optional.ofNullable(conta)
                    .map(c -> new PosicaoContaCore(c.numeroConta(), c.agencia(), c.saldo(), c.statusConta()));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            log.warn("[CORE BANCARIO] Falha ao consultar conta do CPF {}: {}", cpf.valor(), e.getMessage());
            throw new IntegracaoIndisponivelException(SISTEMA, e);
        }
    }
}
