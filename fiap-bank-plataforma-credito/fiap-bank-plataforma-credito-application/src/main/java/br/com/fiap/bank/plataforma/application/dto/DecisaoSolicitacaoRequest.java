package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Contrato do PUT /api/v1/solicitacoes/{id}/decisao.
 * A decisao da Mesa e modelada como um sub-recurso da solicitacao, no lugar das antigas
 * acoes POST /aprovarSolicitacao e POST /reprovarSolicitacao.
 */
public record DecisaoSolicitacaoRequest(

        @NotBlank(message = "A decisao e obrigatoria (APROVADO ou REPROVADO).")
        @Pattern(regexp = "(?i)^(APROVADO|REPROVADO)$", message = "Decisao invalida. Valores aceitos: APROVADO ou REPROVADO.")
        String decisao,

        @Size(max = 400, message = "O parecer deve ter no maximo 400 caracteres.")
        String parecer
) {

    public boolean aprovado() {
        return "APROVADO".equalsIgnoreCase(decisao);
    }
}
