package br.com.fiap.bank.core.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Contrato de entrada do PUT /api/v1/contas/{numeroConta}/saldo.
 */
public record AtualizarSaldoRequest(

        @NotBlank(message = "A operacao e obrigatoria (DEPOSITO ou SAQUE).")
        @Pattern(regexp = ValidacaoPatterns.OPERACAO, message = "Operacao invalida. Valores aceitos: DEPOSITO ou SAQUE.")
        String operacao,

        @NotNull(message = "O valor e obrigatorio.")
        @DecimalMin(value = "0.00", inclusive = false, message = "O valor deve ser estritamente maior que zero.")
        BigDecimal valor
) {
}
