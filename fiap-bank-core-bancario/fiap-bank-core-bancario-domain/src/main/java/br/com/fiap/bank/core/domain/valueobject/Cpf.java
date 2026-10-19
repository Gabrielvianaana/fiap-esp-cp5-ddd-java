package br.com.fiap.bank.core.domain.valueobject;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;

import java.util.regex.Pattern;

/**
 * Value Object CPF.
 * Aceita o formato limpo (11 digitos) ou o canonico com mascara (XXX.XXX.XXX-XX)
 * e sempre armazena a versao sanitizada (somente digitos).
 * Imutavel; igualdade por valor (equals/hashCode/toString gerados pelo record).
 */
public record Cpf(String valor) {

    private static final Pattern FORMATO_ACEITO = Pattern.compile("^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$");

    public Cpf {
        if (valor == null || !FORMATO_ACEITO.matcher(valor.trim()).matches()) {
            throw new DadoInvalidoException("CPF invalido: informe 11 digitos numericos ou o formato XXX.XXX.XXX-XX.");
        }
        valor = valor.replaceAll("\\D", "");
    }

    public static Cpf de(String valor) {
        return new Cpf(valor);
    }

    public String formatado() {
        return valor.substring(0, 3) + "." + valor.substring(3, 6) + "." + valor.substring(6, 9) + "-" + valor.substring(9);
    }

    @Override
    public String toString() {
        return valor;
    }
}
