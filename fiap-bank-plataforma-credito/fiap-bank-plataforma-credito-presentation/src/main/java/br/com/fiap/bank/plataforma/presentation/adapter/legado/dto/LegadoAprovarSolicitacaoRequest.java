package br.com.fiap.bank.plataforma.presentation.adapter.legado.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Contrato historico: {"solicitacaoId": 1, "parecerOperador": "..."}. */
public record LegadoAprovarSolicitacaoRequest(
        @JsonAlias("id")
        @NotNull(message = "O solicitacaoId e obrigatorio.")
        Long solicitacaoId,

        @Size(max = 400, message = "O parecer deve ter no maximo 400 caracteres.")
        String parecerOperador
) {
}
