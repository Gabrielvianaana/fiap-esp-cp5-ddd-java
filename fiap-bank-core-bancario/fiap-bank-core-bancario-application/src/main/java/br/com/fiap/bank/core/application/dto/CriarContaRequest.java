package br.com.fiap.bank.core.application.dto;

import br.com.fiap.bank.core.domain.model.TipoConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Contrato de entrada para abertura de conta.
 * agencia e opcional: quando ausente/nula assume "0001"; quando informada, exige 4 digitos.
 */
public record CriarContaRequest(

        @NotBlank(message = "O CPF do titular e obrigatorio.")
        @Pattern(regexp = ValidacaoPatterns.CPF, message = "O CPF deve conter 11 digitos numericos ou o formato XXX.XXX.XXX-XX.")
        String cpfCliente,

        @Pattern(regexp = ValidacaoPatterns.AGENCIA, message = "A agencia deve possuir exatamente 4 digitos numericos.")
        String agencia,

        @NotNull(message = "O tipo da conta e obrigatorio (CORRENTE, POUPANCA ou SALARIO).")
        TipoConta tipoConta,

        @NotNull(message = "O saldo inicial e obrigatorio.")
        @PositiveOrZero(message = "O saldo inicial nao pode ser negativo.")
        BigDecimal saldoInicial
) {

    public static final String AGENCIA_PADRAO = "0001";

    /** Aplica o default de agencia definido na especificacao. */
    public CriarContaRequest {
        if (agencia == null || agencia.isBlank()) {
            agencia = AGENCIA_PADRAO;
        }
    }
}
