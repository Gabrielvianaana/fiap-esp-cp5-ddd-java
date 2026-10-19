package br.com.fiap.bank.core.domain.repository;

import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.valueobject.Cpf;

import java.util.List;
import java.util.Optional;

/**
 * Porta (Port) de persistencia de contas bancarias.
 */
public interface ContaBancariaRepository {

    ContaBancaria salvar(ContaBancaria conta);

    Optional<ContaBancaria> buscarPorNumero(String numero);

    /**
     * Busca a conta aplicando bloqueio para atualizacao, garantindo que creditos e debitos
     * concorrentes na mesma conta sejam serializados (operacao atomica).
     */
    Optional<ContaBancaria> buscarPorNumeroParaAtualizacao(String numero);

    /** Conta vigente do titular: a nao encerrada, ou a mais recente caso todas estejam encerradas. */
    Optional<ContaBancaria> buscarPorCpfTitular(Cpf cpf);

    List<ContaBancaria> listarTodas();

    boolean existePorNumero(String numero);

    boolean existeContaNaoEncerradaParaTitular(Cpf cpf);

    void removerContasDoTitular(Cpf cpf);
}
