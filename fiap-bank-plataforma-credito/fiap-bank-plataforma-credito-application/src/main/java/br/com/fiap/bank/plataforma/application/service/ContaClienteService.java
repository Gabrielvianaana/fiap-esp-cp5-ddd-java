package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.ContaClienteResponse;
import br.com.fiap.bank.plataforma.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static br.com.fiap.bank.plataforma.application.exception.TradutorExcecaoDominio.executar;

/**
 * Consulta da conta do cliente no Core Bancario (usada pelo app Mobile para exibir saldo).
 */
@Service
@RequiredArgsConstructor
public class ContaClienteService {

    private final CoreBancarioGateway coreBancarioGateway;

    public ContaClienteResponse consultar(String cpfInformado) {
        Cpf cpf = executar(() -> Cpf.de(cpfInformado));
        return executar(() -> coreBancarioGateway.consultarConta(cpf))
                .map(c -> new ContaClienteResponse(c.statusConta(), c.saldo(), c.agencia(), c.numeroConta()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma conta encontrada no Core Bancario para o CPF " + cpf.valor() + "."));
    }
}
