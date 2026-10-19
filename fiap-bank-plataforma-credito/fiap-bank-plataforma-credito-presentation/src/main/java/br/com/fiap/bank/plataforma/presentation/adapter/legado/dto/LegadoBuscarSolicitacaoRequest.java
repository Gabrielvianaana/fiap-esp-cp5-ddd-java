package br.com.fiap.bank.plataforma.presentation.adapter.legado.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato historico: {"solicitacaoId": 1}.
 * O README legado documentava {"id": 1}; aceitamos os dois nomes para nao quebrar nenhum consumidor.
 */
public record LegadoBuscarSolicitacaoRequest(
        @JsonAlias("id")
        @NotNull(message = "O solicitacaoId e obrigatorio.")
        Long solicitacaoId
) {
}
