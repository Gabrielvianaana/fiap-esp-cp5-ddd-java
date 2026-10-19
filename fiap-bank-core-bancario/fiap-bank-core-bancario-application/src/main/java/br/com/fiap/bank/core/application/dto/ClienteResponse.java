package br.com.fiap.bank.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representacao de saida do recurso cliente. Nunca expoe a entidade de dominio diretamente.
 */
public record ClienteResponse(
        UUID id,
        String cpf,
        String nome,
        String email,
        LocalDate dataNascimento,
        BigDecimal rendaMensal,
        String numeroConta
) {
}
