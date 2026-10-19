package br.com.fiap.bank.plataforma.domain.service;

import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.model.StatusSolicitacao;
import br.com.fiap.bank.plataforma.domain.valueobject.AnaliseRisco;
import br.com.fiap.bank.plataforma.domain.valueobject.PosicaoContaCore;
import br.com.fiap.bank.plataforma.domain.valueobject.ResultadoAnalise;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Domain Service: motor de regras de concessao.
 * E Java puro e sem estado, por isso pode ser testado sem Spring, banco ou integracoes.
 *
 * Ordem de avaliacao:
 * 1. Restricao no SCR (Banco Central)            -> REPROVADO
 * 2. Score do Bureau abaixo do minimo            -> REPROVADO
 * 3. Pendencia interna no CRM                    -> EM_ANALISE (Mesa)
 * 4. Parcela compromete mais de 30% da renda     -> EM_ANALISE (Mesa)
 * 5. Core indisponivel / sem conta ativa          -> EM_ANALISE (Mesa)
 * 6. Caso contrario                              -> APROVADO
 */
public class MotorRegrasCredito {

    public static final int SCORE_MINIMO = 500;
    public static final BigDecimal LIMITE_COMPROMETIMENTO_RENDA = new BigDecimal("0.30");

    public ResultadoAnalise avaliar(SolicitacaoCredito solicitacao, AnaliseRisco analise) {
        if (analise.restricaoScr()) {
            return reprovado("Solicitacao reprovada automaticamente devido a restricoes ativas no SCR (Banco Central).");
        }
        if (analise.scoreBureau() < SCORE_MINIMO) {
            return reprovado("Solicitacao reprovada automaticamente. Score no Bureau de Credito insuficiente ("
                    + analise.scoreBureau() + ").");
        }
        if (analise.historicoCrm().possuiPendenciaInterna()) {
            return mesa("Encaminhado para a Mesa de Operacoes de Credito. Cliente possui pendencia interna no CRM.");
        }

        BigDecimal parcela = solicitacao.parcelaEstimada();
        BigDecimal limite = solicitacao.getRendaMensal().multiply(LIMITE_COMPROMETIMENTO_RENDA);
        if (parcela.compareTo(limite) > 0) {
            return mesa("Encaminhado para a Mesa de Operacoes de Credito. Parcela estimada (R$ " + parcela.toPlainString()
                    + ") compromete mais de 30% da renda informada.");
        }

        if (!analise.coreDisponivel()) {
            return mesa("Encaminhado para a Mesa de Operacoes de Credito. Core Bancario indisponivel para validar saldo e movimentacoes.");
        }
        Optional<PosicaoContaCore> conta = analise.contaCore();
        if (conta.isEmpty() || !conta.get().ativa()) {
            return mesa("Encaminhado para a Mesa de Operacoes de Credito. Cliente sem conta ativa no Core Bancario.");
        }

        return new ResultadoAnalise(StatusSolicitacao.APROVADO, "Solicitacao aprovada automaticamente pelo Motor de Credito FIAP Bank!");
    }

    private static ResultadoAnalise reprovado(String parecer) {
        return new ResultadoAnalise(StatusSolicitacao.REPROVADO, parecer);
    }

    private static ResultadoAnalise mesa(String parecer) {
        return new ResultadoAnalise(StatusSolicitacao.EM_ANALISE, parecer);
    }
}
