package br.com.fiap.bank.mesa.dto;

/**
 * Corpo do PUT /api/v1/solicitacoes/{id}/decisao da Plataforma de Credito.
 * decisao: APROVADO ou REPROVADO; parecer: justificativa do operador.
 */
public record DecisaoSolicitacaoRequestDTO(
    String decisao,
    String parecer
) {

    public static DecisaoSolicitacaoRequestDTO aprovar(String parecer) {
        return new DecisaoSolicitacaoRequestDTO("APROVADO", parecer);
    }

    public static DecisaoSolicitacaoRequestDTO reprovar(String motivo) {
        return new DecisaoSolicitacaoRequestDTO("REPROVADO", motivo);
    }
}
