package br.com.fiap.bank.plataforma.domain.repository;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;

import java.util.List;
import java.util.Optional;

/** Porta de persistencia das propostas de credito. */
public interface SolicitacaoCreditoRepository {

    SolicitacaoCredito salvar(SolicitacaoCredito solicitacao);

    Optional<SolicitacaoCredito> buscarPorId(Long id);

    /** Todas as propostas, da mais recente para a mais antiga. */
    List<SolicitacaoCredito> listarTodas();

    /** Propostas de um cliente, da mais recente para a mais antiga. */
    List<SolicitacaoCredito> listarPorCpf(Cpf cpf);
}
