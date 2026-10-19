package br.com.fiap.bank.core.infrastructure.persistence.adapter;

import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.domain.repository.ContaBancariaRepository;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteEntity;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaBancariaEntity;
import br.com.fiap.bank.core.infrastructure.persistence.mapper.ContaBancariaPersistenceMapper;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ClienteJpaRepository;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ContaBancariaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Adapter (Ports & Adapters): implementa a porta de dominio ContaBancariaRepository usando Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class ContaBancariaRepositoryAdapter implements ContaBancariaRepository {

    private final ContaBancariaJpaRepository jpaRepository;
    private final ClienteJpaRepository clienteJpaRepository;

    @Override
    public ContaBancaria salvar(ContaBancaria conta) {
        ContaBancariaEntity entity = jpaRepository.findById(conta.getId())
                .orElseGet(() -> novaEntidade(conta));
        ContaBancariaPersistenceMapper.copiarEstadoMutavel(conta, entity);
        return ContaBancariaPersistenceMapper.paraDominio(jpaRepository.save(entity));
    }

    @Override
    public Optional<ContaBancaria> buscarPorNumero(String numero) {
        return jpaRepository.findByNumero(numero).map(ContaBancariaPersistenceMapper::paraDominio);
    }

    @Override
    public Optional<ContaBancaria> buscarPorNumeroParaAtualizacao(String numero) {
        return jpaRepository.findByNumeroParaAtualizacao(numero).map(ContaBancariaPersistenceMapper::paraDominio);
    }

    @Override
    public Optional<ContaBancaria> buscarPorCpfTitular(Cpf cpf) {
        return jpaRepository.findByTitularCpfOrderByDataAberturaDesc(cpf.valor()).stream()
                .min(Comparator.comparing((ContaBancariaEntity c) -> c.getStatus() == StatusConta.ENCERRADA))
                .map(ContaBancariaPersistenceMapper::paraDominio);
    }

    @Override
    public List<ContaBancaria> listarTodas() {
        return jpaRepository.findAll().stream().map(ContaBancariaPersistenceMapper::paraDominio).toList();
    }

    @Override
    public boolean existePorNumero(String numero) {
        return jpaRepository.existsByNumero(numero);
    }

    @Override
    public boolean existeContaNaoEncerradaParaTitular(Cpf cpf) {
        return jpaRepository.existsByTitularCpfAndStatusNot(cpf.valor(), StatusConta.ENCERRADA);
    }

    @Override
    public void removerContasDoTitular(Cpf cpf) {
        jpaRepository.deleteByTitularCpf(cpf.valor());
    }

    private ContaBancariaEntity novaEntidade(ContaBancaria conta) {
        ClienteEntity titular = clienteJpaRepository.findByCpf(conta.getCpfTitular().valor())
                .orElseThrow(() -> new IllegalStateException(
                        "Titular nao encontrado para o CPF " + conta.getCpfTitular().valor()));
        return ContaBancariaEntity.builder()
                .id(conta.getId())
                .numero(conta.getNumero())
                .agencia(conta.getAgencia())
                .dataAbertura(conta.getDataAbertura())
                .tipoConta(conta.getTipoConta())
                .titular(titular)
                .build();
    }
}
