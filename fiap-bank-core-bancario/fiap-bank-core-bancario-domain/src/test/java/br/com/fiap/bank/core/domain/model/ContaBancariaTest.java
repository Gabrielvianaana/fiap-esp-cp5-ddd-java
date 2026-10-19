package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.ContaInativaException;
import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.domain.valueobject.Saldo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContaBancariaTest {

    private ContaBancaria novaConta(String saldoInicial) {
        return ContaBancaria.abrir("12345-6", null, TipoConta.CORRENTE,
                Saldo.de(new BigDecimal(saldoInicial)), Cpf.de("12345678900"));
    }

    @Test
    void deveAbrirContaAtivaComAgenciaPadrao() {
        ContaBancaria conta = novaConta("100.00");

        assertEquals(ContaBancaria.AGENCIA_PADRAO, conta.getAgencia());
        assertEquals(StatusConta.ATIVA, conta.getStatus());
    }

    @Test
    void deveDepositarESacar() {
        ContaBancaria conta = novaConta("100.00");

        conta.movimentar(TipoOperacao.de("deposito"), new BigDecimal("50.00"));
        conta.movimentar(TipoOperacao.de("SAQUE"), new BigDecimal("30.00"));

        assertEquals(new BigDecimal("120.00"), conta.getSaldo().valor());
    }

    @Test
    void naoDevePermitirSaqueMaiorQueOSaldo() {
        ContaBancaria conta = novaConta("10.00");
        assertThrows(SaldoInsuficienteException.class, () -> conta.sacar(new BigDecimal("10.50")));
    }

    @Test
    void contaEncerradaNaoMovimentaEEncerramentoEIdempotente() {
        ContaBancaria conta = novaConta("10.00");
        conta.encerrar();
        conta.encerrar();

        assertTrue(conta.estaEncerrada());
        assertThrows(ContaInativaException.class, () -> conta.depositar(BigDecimal.ONE));
    }

    @Test
    void deveRejeitarOperacaoDesconhecidaEAgenciaInvalida() {
        assertThrows(DadoInvalidoException.class, () -> TipoOperacao.de("TRANSFERENCIA"));
        assertThrows(DadoInvalidoException.class, () -> ContaBancaria.abrir("12345-6", "12", TipoConta.CORRENTE,
                Saldo.zero(), Cpf.de("12345678900")));
    }

    @Test
    void deveGerarNumeroNoFormatoEsperado() {
        String numero = ContaBancaria.gerarNumero(new Random(42));
        assertTrue(numero.matches("\\d{5}-\\d"), numero);
    }
}
