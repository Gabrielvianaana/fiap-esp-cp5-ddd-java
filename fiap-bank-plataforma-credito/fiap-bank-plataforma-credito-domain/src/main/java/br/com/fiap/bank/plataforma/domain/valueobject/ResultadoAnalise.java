package br.com.fiap.bank.plataforma.domain.valueobject;

import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;

/** Decisao produzida pelo motor de regras. */
public record ResultadoAnalise(StatusSolicitacao status, String parecer) {
}
