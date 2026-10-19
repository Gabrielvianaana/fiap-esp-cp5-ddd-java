package br.com.fiap.bank.plataforma.application.mapper;

import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SolicitacaoCreditoDtoMapper {

    public static SolicitacaoCreditoResponse paraResponse(SolicitacaoCredito s) {
        return new SolicitacaoCreditoResponse(
                s.getId(),
                s.getCpfCliente().valor(),
                s.getNomeCliente(),
                s.getTipoCredito(),
                s.getValorSolicitado(),
                s.getQuantidadeParcelas(),
                s.getRendaMensal(),
                s.getScoreCredito(),
                s.getStatus().name(),
                s.getParecerOperador(),
                s.getScrRestricoes(),
                s.getScoreBureau(),
                s.getDataSolicitacao(),
                s.getDataAtualizacao()
        );
    }
}
