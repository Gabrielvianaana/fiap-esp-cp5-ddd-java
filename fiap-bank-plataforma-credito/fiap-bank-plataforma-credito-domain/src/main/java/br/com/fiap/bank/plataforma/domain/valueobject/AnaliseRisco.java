package br.com.fiap.bank.plataforma.domain.valueobject;

import java.util.Optional;

/**
 * Value Object que consolida os indicadores coletados nas integracoes (SCR, Bureau, CRM e Core Bancario).
 *
 * @param restricaoScr    cliente possui restricoes/dividas vencidas no SCR do Banco Central
 * @param scoreBureau     score de credito no Bureau (0 a 1000)
 * @param historicoCrm    historico de relacionamento no CRM
 * @param contaCore       conta do cliente no Core Bancario (vazio se nao houver)
 * @param coreDisponivel  false quando o Core Bancario nao respondeu
 */
public record AnaliseRisco(boolean restricaoScr,
                           int scoreBureau,
                           HistoricoRelacionamento historicoCrm,
                           Optional<PosicaoContaCore> contaCore,
                           boolean coreDisponivel) {

    public AnaliseRisco {
        historicoCrm = historicoCrm == null ? HistoricoRelacionamento.semHistorico() : historicoCrm;
        contaCore = contaCore == null ? Optional.empty() : contaCore;
    }
}
