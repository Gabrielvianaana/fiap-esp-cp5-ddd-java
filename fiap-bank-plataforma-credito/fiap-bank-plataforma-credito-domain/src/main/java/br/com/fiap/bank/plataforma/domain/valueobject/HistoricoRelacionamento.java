package br.com.fiap.bank.plataforma.domain.valueobject;

/**
 * Value Object com o historico do cliente no CRM corporativo.
 */
public record HistoricoRelacionamento(boolean clienteDaCasa, boolean possuiPendenciaInterna) {

    public static HistoricoRelacionamento semHistorico() {
        return new HistoricoRelacionamento(false, false);
    }
}
