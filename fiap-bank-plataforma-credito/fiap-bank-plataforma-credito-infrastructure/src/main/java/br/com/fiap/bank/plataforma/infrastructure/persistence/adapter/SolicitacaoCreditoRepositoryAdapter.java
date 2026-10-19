package br.com.fiap.bank.plataforma.infrastructure.persistence.adapter;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.infrastructure.persistence.mapper.SolicitacaoCreditoPersistenceMapper;
import br.com.fiap.bank.plataforma.infrastructure.persistence.repository.SolicitacaoCreditoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adapter que implementa a porta de dominio com Spring Data JPA. */
@Component
@RequiredArgsConstructor
public class SolicitacaoCreditoRepositoryAdapter implements SolicitacaoCreditoRepository {

    private final SolicitacaoCreditoJpaRepository jpaRepository;

    @Override
    public SolicitacaoCredito salvar(SolicitacaoCredito solicitacao) {
        return SolicitacaoCreditoPersistenceMapper.paraDominio(
                jpaRepository.save(SolicitacaoCreditoPersistenceMapper.paraEntidade(solicitacao)));
    }

    @Override
    public Optional<SolicitacaoCredito> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(SolicitacaoCreditoPersistenceMapper::paraDominio);
    }

    @Override
    public List<SolicitacaoCredito> listarTodas() {
        return jpaRepository.findAllByOrderByDataSolicitacaoDesc().stream()
                .map(SolicitacaoCreditoPersistenceMapper::paraDominio)
                .toList();
    }

    @Override
    public List<SolicitacaoCredito> listarPorCpf(Cpf cpf) {
        return jpaRepository.findByCpfClienteOrderByDataSolicitacaoDesc(cpf.valor()).stream()
                .map(SolicitacaoCreditoPersistenceMapper::paraDominio)
                .toList();
    }
}
