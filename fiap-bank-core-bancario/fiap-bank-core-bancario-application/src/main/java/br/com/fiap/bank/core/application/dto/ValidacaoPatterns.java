package br.com.fiap.bank.core.application.dto;

/**
 * Expressoes regulares compartilhadas pelas validacoes dos DTOs de entrada.
 */
public final class ValidacaoPatterns {

    /** 11 digitos limpos ou o formato canonico XXX.XXX.XXX-XX. */
    public static final String CPF = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$";

    /** Estrutura local@dominio.tld (o @Email padrao aceitaria "a@b", sem TLD). */
    public static final String EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /** Exatamente 4 digitos. */
    public static final String AGENCIA = "^\\d{4}$";

    /** DEPOSITO ou SAQUE, sem diferenciar maiusculas/minusculas. */
    public static final String OPERACAO = "(?i)^(DEPOSITO|SAQUE)$";

    private ValidacaoPatterns() {
    }
}
