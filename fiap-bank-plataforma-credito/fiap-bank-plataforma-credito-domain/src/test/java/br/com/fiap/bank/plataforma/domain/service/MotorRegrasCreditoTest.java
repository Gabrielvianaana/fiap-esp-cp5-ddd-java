package br.com.fiap.bank.plataforma.domain.service;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.domain.valueobject.AnaliseRisco;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.domain.valueobject.HistoricoRelacionamento;
import br.com.fiap.bank.plataforma.domain.valueobject.PosicaoContaCore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MotorRegrasCreditoTest {

    private final MotorRegrasCredito motor = new MotorRegrasCredito();
    private static final Optional<PosicaoContaCore> CONTA_ATIVA =
            Optional.of(new PosicaoContaCore("12345-6", "0001", new BigDecimal("12500.00"), "ATIVA"));

    private SolicitacaoCredito solicitacao(String valor, int parcelas, String renda) {
        return SolicitacaoCredito.nova(Cpf.de("12345678900"), "Ana Silva", "EMPRESTIMO_PESSOAL",
                new BigDecimal(valor), parcelas, new BigDecimal(renda));
    }

    private AnaliseRisco analise(boolean scr, int score, Optional<PosicaoContaCore> conta, boolean coreOk) {
        return new AnaliseRisco(scr, score, HistoricoRelacionamento.semHistorico(), conta, coreOk);
    }

    @Test
    void deveReprovarComRestricaoNoScr() {
        var resultado = motor.avaliar(solicitacao("1000", 10, "5000"), analise(true, 900, CONTA_ATIVA, true));
        assertEquals(StatusSolicitacao.REPROVADO, resultado.status());
    }

    @Test
    void deveReprovarComScoreBaixo() {
        var resultado = motor.avaliar(solicitacao("1000", 10, "5000"), analise(false, 499, CONTA_ATIVA, true));
        assertEquals(StatusSolicitacao.REPROVADO, resultado.status());
    }

    @Test
    void deveEncaminharParaMesaQuandoParcelaPassaDe30PorCentoDaRenda() {
        // 60.000 / 12 = 5.000 > 30% de 7.500 (2.250)
        var resultado = motor.avaliar(solicitacao("60000", 12, "7500"), analise(false, 805, CONTA_ATIVA, true));
        assertEquals(StatusSolicitacao.EM_ANALISE, resultado.status());
    }

    @Test
    void deveEncaminharParaMesaQuandoCoreIndisponivelEmVezDeAprovarComDadosFicticios() {
        var resultado = motor.avaliar(solicitacao("15000", 24, "7500"), analise(false, 805, Optional.empty(), false));
        assertEquals(StatusSolicitacao.EM_ANALISE, resultado.status());
    }

    @Test
    void deveAprovarAutomaticamenteQuandoTodasAsRegrasPassam() {
        // 15.000 / 24 = 625 <= 2.250
        var resultado = motor.avaliar(solicitacao("15000", 24, "7500"), analise(false, 805, CONTA_ATIVA, true));
        assertEquals(StatusSolicitacao.APROVADO, resultado.status());
    }
}
