package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Propostas de demonstracao. Passam pelo mesmo caso de uso de uma proposta real (motor de regras incluso).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargaInicialService {

    private final SolicitacaoCreditoService solicitacaoService;
    private final SolicitacaoCreditoRepository repository;

    public void carregarSeNecessario() {
        if (!repository.listarTodas().isEmpty()) {
            return;
        }
        List.of(
                // Aprovacao automatica: parcela de R$ 625 <= 30% da renda
                new SolicitarCreditoRequest("12345678900", "Ana Silva", "EMPRESTIMO_PESSOAL",
                        new BigDecimal("15000.00"), 24, new BigDecimal("7500.00")),
                new SolicitarCreditoRequest("98765432100", "Carlos Eduardo Oliveira", "CREDITO_IMOBILIARIO",
                        new BigDecimal("350000.00"), 180, new BigDecimal("12000.00")),
                // Reprovacao automatica: restricao no SCR
                new SolicitarCreditoRequest("55544433399", "Mariana Souza", "CREDITO_AUTOMOTIVO",
                        new BigDecimal("45000.00"), 48, new BigDecimal("4200.00")),
                // Mesa de Operacoes: parcela de R$ 5.000 compromete mais de 30% da renda
                new SolicitarCreditoRequest("12345678900", "Ana Silva", "CREDITO_AUTOMOTIVO",
                        new BigDecimal("60000.00"), 12, new BigDecimal("7500.00"))
        ).forEach(request -> {
            try {
                solicitacaoService.solicitar(request);
            } catch (RuntimeException e) {
                log.warn(">>> Plataforma de Credito: falha ao carregar proposta de {}: {}", request.nomeCliente(), e.getMessage());
            }
        });
        log.info(">>> Plataforma de Credito: dados iniciais carregados.");
    }
}
