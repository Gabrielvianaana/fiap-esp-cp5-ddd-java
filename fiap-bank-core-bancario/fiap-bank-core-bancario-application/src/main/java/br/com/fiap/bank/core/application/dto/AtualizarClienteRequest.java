package br.com.fiap.bank.core.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato de entrada do PUT /api/v1/clientes/{id} (substituicao integral dos dados cadastrais).
 * O CPF e a identidade de negocio do correntista: pode ser enviado (mesmo payload do cadastro),
 * mas precisa ser igual ao ja cadastrado.
 */
public record AtualizarClienteRequest(

        @Pattern(regexp = ValidacaoPatterns.CPF, message = "O CPF deve conter 11 digitos numericos ou o formato XXX.XXX.XXX-XX.")
        String cpf,

        @NotBlank(message = "O nome e obrigatorio.")
        @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio.")
        @Email(regexp = ValidacaoPatterns.EMAIL, message = "O e-mail informado e invalido.")
        String email,

        @NotNull(message = "A data de nascimento e obrigatoria.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dataNascimento,

        @NotNull(message = "A renda mensal e obrigatoria.")
        @PositiveOrZero(message = "A renda mensal deve ser maior ou igual a zero.")
        BigDecimal rendaMensal
) {
}
