package br.com.fiap.bank.plataforma.presentation.adapter.legado.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Contrato historico: {"solicitacaoId": 1, "motivoReprovacao": "..."}.
 * Aceita tambem "id" e "parecerOperador", nomes documentados no README legado.
 */
public record LegadoReprovarSolicitacaoRequest(
        @JsonAlias("id")
        @NotNull(message = "O solicitacaoId e obrigatorio.")
        Long solicitacaoId,

        @JsonAlias("parecerOperador")
        @Size(max = 400, message = "O motivo deve ter no maximo 400 caracteres.")
        String motivoReprovacao
) {
}
