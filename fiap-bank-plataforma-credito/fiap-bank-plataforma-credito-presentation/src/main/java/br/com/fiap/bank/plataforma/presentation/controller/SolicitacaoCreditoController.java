package br.com.fiap.bank.plataforma.presentation.controller;

import br.com.fiap.bank.plataforma.application.dto.DecisaoSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;
import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.application.service.SolicitacaoCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * API RESTful da esteira de credito.
 *
 * | Legado (acao, tudo POST)              | RESTful                                      |
 * |---------------------------------------|----------------------------------------------|
 * | POST /api/credito/solicitarCredito    | POST   /api/v1/solicitacoes          (201)   |
 * | POST /api/credito/listarTodasSol...   | GET    /api/v1/solicitacoes                  |
 * | POST /api/credito/consultarStatus...  | GET    /api/v1/solicitacoes?cpf={cpf}        |
 * | POST /api/credito/buscarSolicitacao.. | GET    /api/v1/solicitacoes/{id}             |
 * | POST /api/credito/aprovarSolicitacao  | PUT    /api/v1/solicitacoes/{id}/decisao     |
 * | POST /api/credito/reprovarSolicitacao | PUT    /api/v1/solicitacoes/{id}/decisao     |
 * | (nao existia)                         | DELETE /api/v1/solicitacoes/{id}     (204)   |
 */
@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoCreditoController {

    private final SolicitacaoCreditoService solicitacaoService;

    @PostMapping
    public ResponseEntity<SolicitacaoCreditoResponse> criar(@Valid @RequestBody SolicitarCreditoRequest request) {
        SolicitacaoCreditoResponse criada = solicitacaoService.solicitar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoCreditoResponse>> listar(@RequestParam(required = false) String cpf) {
        return ResponseEntity.ok(solicitacaoService.listar(Optional.ofNullable(cpf)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoCreditoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitacaoService.buscarPorId(id));
    }

    @PutMapping("/{id}/decisao")
    public ResponseEntity<SolicitacaoCreditoResponse> registrarDecisao(@PathVariable Long id,
                                                                       @Valid @RequestBody DecisaoSolicitacaoRequest request) {
        return ResponseEntity.ok(solicitacaoService.registrarDecisao(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        solicitacaoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
