package br.com.fiap.bank.plataforma.infrastructure.client.core;

import java.math.BigDecimal;

/**
 * Contrato tipado da resposta do Core Bancario (GET /api/v1/contas/cliente/{cpf}).
 * Substitui o Map&lt;String, Object&gt; nao tipado usado no legado.
 */
public record ContaCoreResponse(
        String id,
        String numeroConta,
        String agencia,
        BigDecimal saldo,
        String tipoConta,
        String statusConta,
        String dataAbertura,
        String cpfTitular
) {
}
