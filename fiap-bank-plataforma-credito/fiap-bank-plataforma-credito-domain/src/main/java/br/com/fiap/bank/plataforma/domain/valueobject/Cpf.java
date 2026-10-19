package br.com.fiap.bank.plataforma.domain.valueobject;

import br.com.fiap.bank.plataforma.domain.exception.DadoInvalidoException;

import java.util.regex.Pattern;

/**
 * Value Object CPF do contexto de Credito (cada bounded context mantem o seu proprio modelo).
 * Aceita 11 digitos ou XXX.XXX.XXX-XX e guarda somente os digitos.
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

    @Override
    public String toString() {
        return valor;
    }
}
