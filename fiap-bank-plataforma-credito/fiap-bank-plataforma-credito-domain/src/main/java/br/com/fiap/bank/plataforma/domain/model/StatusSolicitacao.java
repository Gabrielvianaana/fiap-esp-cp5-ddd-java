package br.com.fiap.bank.plataforma.domain.model;

public enum StatusSolicitacao {
    EM_ANALISE,
    APROVADO,
    REPROVADO,
    DOCUMENTACAO_PENDENTE,
    CANCELADO;

    /** Status em que a proposta ainda aguarda decisao (podendo ser decidida pela Mesa ou cancelada). */
    public boolean pendente() {
        return this == EM_ANALISE || this == DOCUMENTACAO_PENDENTE;
    }
}
