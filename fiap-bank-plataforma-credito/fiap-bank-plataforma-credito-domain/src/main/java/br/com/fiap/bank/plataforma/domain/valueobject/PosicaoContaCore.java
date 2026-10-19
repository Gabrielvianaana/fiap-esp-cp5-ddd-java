package br.com.fiap.bank.plataforma.domain.valueobject;

import java.math.BigDecimal;

/**
 * Value Object com a visao que o contexto de Credito precisa da conta do cliente no Core Bancario
 * (camada anticorrupcao: o modelo externo e traduzido para este tipo pela infraestrutura).
 */
public record PosicaoContaCore(String numeroConta, String agencia, BigDecimal saldo, String statusConta) {

    public boolean ativa() {
        return "ATIVA".equalsIgnoreCase(statusConta);
    }
}
