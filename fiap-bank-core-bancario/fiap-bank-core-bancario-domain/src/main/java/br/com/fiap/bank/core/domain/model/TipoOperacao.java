package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;

import java.util.Locale;

/**
 * Operacoes contabeis aceitas sobre o sub-recurso saldo.
 */
public enum TipoOperacao {
    DEPOSITO,
    SAQUE;

    /** Converte o texto recebido (case-insensitive) na operacao correspondente. */
    public static TipoOperacao de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DadoInvalidoException("A operacao e obrigatoria (DEPOSITO ou SAQUE).");
        }
        try {
            return TipoOperacao.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new DadoInvalidoException("Operacao invalida: '" + valor + "'. Valores aceitos: DEPOSITO ou SAQUE.");
        }
    }
}
