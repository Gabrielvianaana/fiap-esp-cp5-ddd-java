package br.com.fiap.bank.core.domain.valueobject;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object Saldo (dinheiro em reais, 2 casas decimais).
 * Imutavel: adicionar/subtrair devolvem um NOVO Saldo. Invariante: nunca negativo.
 */
public record Saldo(BigDecimal valor) {

    public Saldo {
        if (valor == null) {
            throw new DadoInvalidoException("O saldo e obrigatorio.");
        }
        valor = valor.setScale(2, RoundingMode.HALF_EVEN);
        if (valor.signum() < 0) {
            throw new DadoInvalidoException("O saldo nao pode ser negativo.");
        }
    }

    public static Saldo zero() {
        return new Saldo(BigDecimal.ZERO);
    }

    public static Saldo de(BigDecimal valor) {
        return new Saldo(valor);
    }

    public Saldo adicionar(BigDecimal quantia) {
        validarQuantiaPositiva(quantia);
        return new Saldo(valor.add(quantia));
    }

    public Saldo subtrair(BigDecimal quantia) {
        validarQuantiaPositiva(quantia);
        if (valor.compareTo(quantia) < 0) {
            throw new SaldoInsuficienteException(valor, quantia);
        }
        return new Saldo(valor.subtract(quantia));
    }

    private static void validarQuantiaPositiva(BigDecimal quantia) {
        if (quantia == null || quantia.signum() <= 0) {
            throw new DadoInvalidoException("O valor da movimentacao deve ser maior que zero.");
        }
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}
