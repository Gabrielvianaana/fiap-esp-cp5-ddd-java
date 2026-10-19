package br.com.fiap.bank.mesa.client;

import br.com.fiap.bank.mesa.dto.DecisaoSolicitacaoRequestDTO;
import br.com.fiap.bank.mesa.dto.SolicitacaoCreditoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Client da Plataforma de Credito, migrado das rotas de acao (POST /api/credito/*)
 * para a API RESTful /api/v1/solicitacoes. As telas Thymeleaf nao mudaram.
 * (As rotas antigas continuam disponiveis na Plataforma via Controller Adaptador.)
 */
@FeignClient(name = "plataformaCreditoClient", url = "${plataforma.credito.url:http://localhost:8081}")
public interface PlataformaCreditoFeignClient {

    @GetMapping("/api/v1/solicitacoes")
    List<SolicitacaoCreditoDTO> listarSolicitacoes();

    @GetMapping("/api/v1/solicitacoes/{id}")
    SolicitacaoCreditoDTO buscarSolicitacao(@PathVariable("id") Long id);

    @PutMapping("/api/v1/solicitacoes/{id}/decisao")
    SolicitacaoCreditoDTO registrarDecisao(@PathVariable("id") Long id, @RequestBody DecisaoSolicitacaoRequestDTO decisao);
}
