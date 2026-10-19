package br.com.fiap.bank.plataforma.domain.model;

import br.com.fiap.bank.plataforma.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.plataforma.domain.exception.TransicaoStatusInvalidaException;
import br.com.fiap.bank.plataforma.domain.valueobject.AnaliseRisco;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.domain.valueobject.ResultadoAnalise;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Raiz de agregado SolicitacaoCredito (proposta de credito).
 *
 * No legado esta classe era ao mesmo tempo @Entity JPA e modelo de negocio anemico, com setters
 * publicos e as regras espalhadas no service. Aqui ela e Java puro e o ciclo de vida
 * (analise automatica, decisao da Mesa, cancelamento) so muda por metodos que validam as transicoes.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SolicitacaoCredito {

    private static final int MAX_PARCELAS = 420;

    @EqualsAndHashCode.Include
    private final Long id;
    private final Cpf cpfCliente;
    private final String nomeCliente;
    private final String tipoCredito;
    private final BigDecimal valorSolicitado;
    private final Integer quantidadeParcelas;
    private final BigDecimal rendaMensal;
    private Integer scoreCredito;
    private StatusSolicitacao status;
    private String parecerOperador;
    private Boolean scrRestricoes;
    private Integer scoreBureau;
    private final LocalDateTime dataSolicitacao;
    private LocalDateTime dataAtualizacao;

    @SuppressWarnings("java:S107")
    private SolicitacaoCredito(Long id, Cpf cpfCliente, String nomeCliente, String tipoCredito,
                               BigDecimal valorSolicitado, Integer quantidadeParcelas, BigDecimal rendaMensal,
                               Integer scoreCredito, StatusSolicitacao status, String parecerOperador,
                               Boolean scrRestricoes, Integer scoreBureau,
                               LocalDateTime dataSolicitacao, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.cpfCliente = Objects.requireNonNull(cpfCliente, "cpfCliente e obrigatorio");
        this.nomeCliente = nomeCliente;
        this.tipoCredito = tipoCredito;
        this.valorSolicitado = valorSolicitado;
        this.quantidadeParcelas = quantidadeParcelas;
        this.rendaMensal = rendaMensal;
        this.scoreCredito = scoreCredito;
        this.status = Objects.requireNonNull(status, "status e obrigatorio");
        this.parecerOperador = parecerOperador;
        this.scrRestricoes = scrRestricoes;
        this.scoreBureau = scoreBureau;
        this.dataSolicitacao = Objects.requireNonNull(dataSolicitacao, "dataSolicitacao e obrigatoria");
        this.dataAtualizacao = dataAtualizacao;
    }

    /** Fabrica de uma nova proposta (ainda sem analise de risco). */
    public static SolicitacaoCredito nova(Cpf cpfCliente, String nomeCliente, String tipoCredito,
                                          BigDecimal valorSolicitado, Integer quantidadeParcelas, BigDecimal rendaMensal) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new DadoInvalidoException("O nome do cliente e obrigatorio.");
        }
        if (tipoCredito == null || tipoCredito.isBlank()) {
            throw new DadoInvalidoException("O tipo de credito e obrigatorio.");
        }
        if (valorSolicitado == null || valorSolicitado.signum() <= 0) {
            throw new DadoInvalidoException("O valor solicitado deve ser maior que zero.");
        }
        if (quantidadeParcelas == null || quantidadeParcelas < 1 || quantidadeParcelas > MAX_PARCELAS) {
            throw new DadoInvalidoException("A quantidade de parcelas deve estar entre 1 e " + MAX_PARCELAS + ".");
        }
        if (rendaMensal == null || rendaMensal.signum() < 0) {
            throw new DadoInvalidoException("A renda mensal nao pode ser negativa.");
        }
        LocalDateTime agora = LocalDateTime.now();
        return new SolicitacaoCredito(null, cpfCliente, nomeCliente.trim(), tipoCredito.trim(),
                valorSolicitado, quantidadeParcelas, rendaMensal, null, StatusSolicitacao.EM_ANALISE,
                "Proposta recebida e em processamento.", null, null, agora, agora);
    }

    /** Fabrica para reconstituir uma proposta persistida (usada pelos adapters). */
    @SuppressWarnings("java:S107")
    public static SolicitacaoCredito reconstituir(Long id, Cpf cpfCliente, String nomeCliente, String tipoCredito,
                                                  BigDecimal valorSolicitado, Integer quantidadeParcelas,
                                                  BigDecimal rendaMensal, Integer scoreCredito,
                                                  StatusSolicitacao status, String parecerOperador,
                                                  Boolean scrRestricoes, Integer scoreBureau,
                                                  LocalDateTime dataSolicitacao, LocalDateTime dataAtualizacao) {
        return new SolicitacaoCredito(id, cpfCliente, nomeCliente, tipoCredito, valorSolicitado, quantidadeParcelas,
                rendaMensal, scoreCredito, status, parecerOperador, scrRestricoes, scoreBureau,
                dataSolicitacao, dataAtualizacao);
    }

    /** Valor estimado da parcela (sem juros), mesma regra usada pelo motor e pela Mesa. */
    public BigDecimal parcelaEstimada() {
        return valorSolicitado.divide(BigDecimal.valueOf(quantidadeParcelas), 2, RoundingMode.HALF_UP);
    }

    /** Registra os indicadores coletados e a decisao automatica do motor de regras. */
    public void registrarAnaliseAutomatica(AnaliseRisco analise, ResultadoAnalise resultado) {
        this.scrRestricoes = analise.restricaoScr();
        this.scoreBureau = analise.scoreBureau();
        this.scoreCredito = analise.scoreBureau();
        this.status = resultado.status();
        this.parecerOperador = resultado.parecer();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void aprovarPelaMesa(String parecer) {
        garantirPendente("aprovada");
        this.status = StatusSolicitacao.APROVADO;
        this.parecerOperador = "Aprovado pela Mesa de Operacoes: " + textoOuPadrao(parecer, "OK");
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void reprovarPelaMesa(String motivo) {
        garantirPendente("reprovada");
        this.status = StatusSolicitacao.REPROVADO;
        this.parecerOperador = "Reprovado pela Mesa de Operacoes. Motivo: "
                + textoOuPadrao(motivo, "Inconformidade nos criterios de risco");
        this.dataAtualizacao = LocalDateTime.now();
    }

    /** Cancelamento logico (idempotente). */
    public void cancelar() {
        if (status == StatusSolicitacao.CANCELADO) {
            return;
        }
        garantirPendente("cancelada");
        this.status = StatusSolicitacao.CANCELADO;
        this.parecerOperador = "Solicitacao cancelada.";
        this.dataAtualizacao = LocalDateTime.now();
    }

    private void garantirPendente(String operacao) {
        if (!status.pendente()) {
            throw new TransicaoStatusInvalidaException(id, status, operacao);
        }
    }

    private static String textoOuPadrao(String texto, String padrao) {
        return (texto == null || texto.isBlank()) ? padrao : texto.trim();
    }
}
