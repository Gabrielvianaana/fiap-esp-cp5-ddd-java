package br.com.fiap.bank.core.domain.exception;

import java.math.BigDecimal;

/**
 * Invariante financeira: o saldo de uma conta nunca pode ficar negativo.
 */
public class SaldoInsuficienteException extends DomainException {

    public SaldoInsuficienteException(BigDecimal saldoAtual, BigDecimal valorSolicitado) {
        super("Saldo insuficiente. Saldo atual: R$ " + saldoAtual.toPlainString()
                + " | valor solicitado: R$ " + valorSolicitado.toPlainString());
    }
}
