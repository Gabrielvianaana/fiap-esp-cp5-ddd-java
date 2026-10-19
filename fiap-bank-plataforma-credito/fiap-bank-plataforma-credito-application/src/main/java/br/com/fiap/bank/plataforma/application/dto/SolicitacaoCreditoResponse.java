package br.com.fiap.bank.plataforma.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representacao de saida da proposta.
 * Mantem exatamente os mesmos campos do contrato historico consumido pela Mesa (Thymeleaf) e pelo Mobile (Angular).
 */
public record SolicitacaoCreditoResponse(
        Long id,
        String cpfCliente,
        String nomeCliente,
        String tipoCredito,
        BigDecimal valorSolicitado,
        Integer quantidadeParcelas,
        BigDecimal rendaMensal,
        Integer scoreCredito,
        String status,
        String parecerOperador,
        Boolean scrRestricoes,
        Integer scoreBureau,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataAtualizacao
) {
}
