package br.com.fiap.bank.plataforma.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Contrato de entrada de uma nova proposta de credito.
 * Os nomes dos campos sao os mesmos do contrato historico, para que o app Mobile continue funcionando.
 */
public record SolicitarCreditoRequest(

        @NotBlank(message = "O CPF do cliente e obrigatorio.")
        @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$",
                message = "O CPF deve conter 11 digitos numericos ou o formato XXX.XXX.XXX-XX.")
        String cpfCliente,

        @NotBlank(message = "O nome do cliente e obrigatorio.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String nomeCliente,

        @NotBlank(message = "O tipo de credito e obrigatorio.")
        @Size(max = 60, message = "O tipo de credito deve ter no maximo 60 caracteres.")
        String tipoCredito,

        @NotNull(message = "O valor solicitado e obrigatorio.")
        @DecimalMin(value = "0.00", inclusive = false, message = "O valor solicitado deve ser maior que zero.")
        BigDecimal valorSolicitado,

        @NotNull(message = "A quantidade de parcelas e obrigatoria.")
        @Min(value = 1, message = "A quantidade minima e de 1 parcela.")
        @Max(value = 420, message = "A quantidade maxima e de 420 parcelas.")
        Integer quantidadeParcelas,

        @NotNull(message = "A renda mensal e obrigatoria.")
        @PositiveOrZero(message = "A renda mensal deve ser maior ou igual a zero.")
        BigDecimal rendaMensal
) {
}
