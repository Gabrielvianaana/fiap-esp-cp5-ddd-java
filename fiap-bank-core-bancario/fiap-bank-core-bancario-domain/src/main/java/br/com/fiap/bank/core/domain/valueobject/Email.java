package br.com.fiap.bank.core.domain.valueobject;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value Object E-mail. Normaliza para minusculas e valida a estrutura local@dominio.tld.
 */
public record Email(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (valor == null || !FORMATO.matcher(valor.trim()).matches()) {
            throw new DadoInvalidoException("E-mail invalido: " + valor);
        }
        valor = valor.trim().toLowerCase(Locale.ROOT);
    }

    public static Email de(String valor) {
        return new Email(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
