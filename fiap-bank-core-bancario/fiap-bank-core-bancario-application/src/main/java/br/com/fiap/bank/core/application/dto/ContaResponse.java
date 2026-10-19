package br.com.fiap.bank.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representacao de saida do recurso conta.
 * Os nomes numeroConta/agencia/saldo/statusConta mantem compatibilidade com o contrato
 * que a Plataforma de Credito e o app Mobile ja consomem.
 */
public record ContaResponse(
        UUID id,
        String numeroConta,
        String agencia,
        BigDecimal saldo,
        String tipoConta,
        String statusConta,
        LocalDate dataAbertura,
        String cpfTitular
) {
}
