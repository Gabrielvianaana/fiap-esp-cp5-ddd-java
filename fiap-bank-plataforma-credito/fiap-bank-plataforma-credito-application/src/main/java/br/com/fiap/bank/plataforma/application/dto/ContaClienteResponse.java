package br.com.fiap.bank.plataforma.application.dto;

import java.math.BigDecimal;

/**
 * Dados da conta do cliente no Core Bancario, no formato historico esperado pelo app Mobile (ClienteCoreInfo).
 */
public record ContaClienteResponse(String statusConta, BigDecimal saldo, String agencia, String numeroConta) {
}
