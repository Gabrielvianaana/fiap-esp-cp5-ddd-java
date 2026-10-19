package br.com.fiap.bank.plataforma.domain.model;

import br.com.fiap.bank.plataforma.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.plataforma.domain.exception.TransicaoStatusInvalidaException;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitacaoCreditoTest {

    private SolicitacaoCredito pendente() {
        return SolicitacaoCredito.nova(Cpf.de("123.456.789-00"), "Ana Silva", "EMPRÉSTIMO PESSOAL",
                new BigDecimal("15000.00"), 24, new BigDecimal("7500.00"));
    }

    @Test
    void novaPropostaNasceEmAnaliseComCpfSanitizado() {
        SolicitacaoCredito s = pendente();
        assertEquals(StatusSolicitacao.EM_ANALISE, s.getStatus());
        assertEquals("12345678900", s.getCpfCliente().valor());
        assertEquals(new BigDecimal("625.00"), s.parcelaEstimada());
    }

    @Test
    void mesaAprovaPropostaPendente() {
        SolicitacaoCredito s = pendente();
        s.aprovarPelaMesa("Renda comprovada");
        assertEquals(StatusSolicitacao.APROVADO, s.getStatus());
        assertTrue(s.getParecerOperador().contains("Renda comprovada"));
    }

    @Test
    void naoPermiteDecidirPropostaJaDecidida() {
        SolicitacaoCredito s = pendente();
        s.reprovarPelaMesa("Margem insuficiente");
        assertThrows(TransicaoStatusInvalidaException.class, () -> s.aprovarPelaMesa("tentativa"));
        assertThrows(TransicaoStatusInvalidaException.class, s::cancelar);
    }

    @Test
    void cancelamentoEIdempotente() {
        SolicitacaoCredito s = pendente();
        s.cancelar();
        s.cancelar();
        assertEquals(StatusSolicitacao.CANCELADO, s.getStatus());
    }

    @Test
    void rejeitaParcelasZeradasQueGeravamDivisaoPorZeroNoLegado() {
        assertThrows(DadoInvalidoException.class, () -> SolicitacaoCredito.nova(Cpf.de("12345678900"), "Ana", "PESSOAL",
                new BigDecimal("1000"), 0, new BigDecimal("5000")));
    }
}
