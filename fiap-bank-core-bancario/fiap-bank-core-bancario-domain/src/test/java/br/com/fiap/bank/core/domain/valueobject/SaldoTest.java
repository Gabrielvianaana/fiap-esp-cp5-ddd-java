package br.com.fiap.bank.core.domain.valueobject;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaldoTest {

    @Test
    void deveSerImutavelAoAdicionar() {
        Saldo original = Saldo.de(new BigDecimal("100.00"));
        Saldo novo = original.adicionar(new BigDecimal("50"));

        assertEquals(new BigDecimal("100.00"), original.valor());
        assertEquals(new BigDecimal("150.00"), novo.valor());
    }

    @Test
    void deveTerIgualdadePorValorIndependenteDaEscala() {
        assertEquals(Saldo.de(new BigDecimal("10")), Saldo.de(new BigDecimal("10.00")));
    }

    @Test
    void naoDevePermitirSaldoNegativo() {
        assertThrows(DadoInvalidoException.class, () -> Saldo.de(new BigDecimal("-0.01")));
    }

    @Test
    void naoDeveSubtrairMaisQueOSaldo() {
        Saldo saldo = Saldo.de(new BigDecimal("10.00"));
        assertThrows(SaldoInsuficienteException.class, () -> saldo.subtrair(new BigDecimal("10.01")));
    }

    @Test
    void naoDeveAceitarMovimentacaoZeradaOuNegativa() {
        Saldo saldo = Saldo.zero();
        assertThrows(DadoInvalidoException.class, () -> saldo.adicionar(BigDecimal.ZERO));
        assertThrows(DadoInvalidoException.class, () -> saldo.adicionar(new BigDecimal("-1")));
    }
}
