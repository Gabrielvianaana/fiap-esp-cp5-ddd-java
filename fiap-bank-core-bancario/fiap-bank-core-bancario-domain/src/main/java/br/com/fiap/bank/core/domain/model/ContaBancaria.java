package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.ContaInativaException;
import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.domain.valueobject.Saldo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.regex.Pattern;

/**
 * Entidade de dominio (raiz de agregado) ContaBancaria.
 * Classe Java pura: sem anotacoes de persistencia. O estado so muda por metodos de negocio,
 * que protegem as invariantes (conta ativa para movimentar, saldo nunca negativo).
 * O titular e referenciado pela sua identidade de negocio (CPF), como recomenda o DDD
 * para referencias entre agregados.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContaBancaria {

    public static final String AGENCIA_PADRAO = "0001";
    private static final Pattern FORMATO_AGENCIA = Pattern.compile("^\\d{4}$");
    private static final Pattern FORMATO_NUMERO = Pattern.compile("^\\d{5}-\\d$");

    @EqualsAndHashCode.Include
    private final UUID id;
    private final String numero;
    private final String agencia;
    private Saldo saldo;
    private final LocalDate dataAbertura;
    private final TipoConta tipoConta;
    private StatusConta status;
    private final Cpf cpfTitular;

    private ContaBancaria(UUID id, String numero, String agencia, Saldo saldo, LocalDate dataAbertura,
                          TipoConta tipoConta, StatusConta status, Cpf cpfTitular) {
        this.id = Objects.requireNonNull(id, "id e obrigatorio");
        this.numero = validarNumero(numero);
        this.agencia = validarAgencia(agencia);
        this.saldo = Objects.requireNonNull(saldo, "saldo e obrigatorio");
        this.dataAbertura = Objects.requireNonNull(dataAbertura, "dataAbertura e obrigatoria");
        this.tipoConta = exigir(tipoConta, "O tipo da conta e obrigatorio (CORRENTE, POUPANCA ou SALARIO).");
        this.status = Objects.requireNonNull(status, "status e obrigatorio");
        this.cpfTitular = Objects.requireNonNull(cpfTitular, "cpfTitular e obrigatorio");
    }

    /** Fabrica para abertura de uma nova conta (status ATIVA, aberta hoje). */
    public static ContaBancaria abrir(String numero, String agencia, TipoConta tipoConta,
                                      Saldo saldoInicial, Cpf cpfTitular) {
        String agenciaEfetiva = (agencia == null || agencia.isBlank()) ? AGENCIA_PADRAO : agencia;
        return new ContaBancaria(UUID.randomUUID(), numero, agenciaEfetiva, saldoInicial, LocalDate.now(),
                tipoConta, StatusConta.ATIVA, cpfTitular);
    }

    /** Fabrica para reconstituir uma conta ja existente (usada pelos adapters de persistencia). */
    public static ContaBancaria reconstituir(UUID id, String numero, String agencia, Saldo saldo,
                                             LocalDate dataAbertura, TipoConta tipoConta,
                                             StatusConta status, Cpf cpfTitular) {
        return new ContaBancaria(id, numero, agencia, saldo, dataAbertura, tipoConta, status, cpfTitular);
    }

    /** Gera um numero de conta no formato NNNNN-D (digito verificador modulo 11). */
    public static String gerarNumero(RandomGenerator random) {
        int base = 10_000 + random.nextInt(90_000);
        String digitos = String.valueOf(base);
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * (6 - i);
        }
        int dv = soma % 11;
        return digitos + "-" + (dv >= 10 ? 0 : dv);
    }

    public void movimentar(TipoOperacao operacao, BigDecimal valor) {
        Objects.requireNonNull(operacao, "operacao e obrigatoria");
        switch (operacao) {
            case DEPOSITO -> depositar(valor);
            case SAQUE -> sacar(valor);
        }
    }

    public void depositar(BigDecimal valor) {
        garantirAtiva();
        this.saldo = saldo.adicionar(valor);
    }

    public void sacar(BigDecimal valor) {
        garantirAtiva();
        this.saldo = saldo.subtrair(valor);
    }

    /** Encerramento logico. Idempotente: encerrar uma conta ja encerrada nao gera erro. */
    public void encerrar() {
        this.status = StatusConta.ENCERRADA;
    }

    public boolean estaAtiva() {
        return status == StatusConta.ATIVA;
    }

    public boolean estaEncerrada() {
        return status == StatusConta.ENCERRADA;
    }

    private void garantirAtiva() {
        if (!estaAtiva()) {
            throw new ContaInativaException(numero, status);
        }
    }

    private static String validarAgencia(String agencia) {
        if (agencia == null || !FORMATO_AGENCIA.matcher(agencia).matches()) {
            throw new DadoInvalidoException("A agencia deve possuir exatamente 4 digitos numericos.");
        }
        return agencia;
    }

    private static String validarNumero(String numero) {
        if (numero == null || !FORMATO_NUMERO.matcher(numero).matches()) {
            throw new DadoInvalidoException("Numero de conta invalido. Formato esperado: NNNNN-D.");
        }
        return numero;
    }

    private static <T> T exigir(T valor, String mensagem) {
        if (valor == null) {
            throw new DadoInvalidoException(mensagem);
        }
        return valor;
    }
}
