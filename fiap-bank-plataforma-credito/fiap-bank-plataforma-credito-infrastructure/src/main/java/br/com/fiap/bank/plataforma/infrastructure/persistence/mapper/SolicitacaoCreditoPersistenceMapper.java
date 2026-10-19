package br.com.fiap.bank.plataforma.infrastructure.persistence.mapper;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.infrastructure.persistence.entity.SolicitacaoCreditoEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SolicitacaoCreditoPersistenceMapper {

    public static SolicitacaoCredito paraDominio(SolicitacaoCreditoEntity e) {
        return SolicitacaoCredito.reconstituir(
                e.getId(),
                Cpf.de(e.getCpfCliente()),
                e.getNomeCliente(),
                e.getTipoCredito(),
                e.getValorSolicitado(),
                e.getQuantidadeParcelas(),
                e.getRendaMensal(),
                e.getScoreCredito(),
                e.getStatus(),
                e.getParecerOperador(),
                e.getScrRestricoes(),
                e.getScoreBureau(),
                e.getDataSolicitacao(),
                e.getDataAtualizacao()
        );
    }

    public static SolicitacaoCreditoEntity paraEntidade(SolicitacaoCredito s) {
        return SolicitacaoCreditoEntity.builder()
                .id(s.getId())
                .cpfCliente(s.getCpfCliente().valor())
                .nomeCliente(s.getNomeCliente())
                .tipoCredito(s.getTipoCredito())
                .valorSolicitado(s.getValorSolicitado())
                .quantidadeParcelas(s.getQuantidadeParcelas())
                .rendaMensal(s.getRendaMensal())
                .scoreCredito(s.getScoreCredito())
                .status(s.getStatus())
                .parecerOperador(s.getParecerOperador())
                .scrRestricoes(s.getScrRestricoes())
                .scoreBureau(s.getScoreBureau())
                .dataSolicitacao(s.getDataSolicitacao())
                .dataAtualizacao(s.getDataAtualizacao())
                .build();
    }
}
