package br.com.fiap.bank.plataforma.presentation.controller;

import br.com.fiap.bank.plataforma.application.dto.ContaClienteResponse;
import br.com.fiap.bank.plataforma.application.service.ContaClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sub-recurso "conta" do cliente: GET /api/v1/clientes/{cpf}/conta
 * (substitui a acao POST /api/credito/consultarClienteCore, que enviava o CPF no corpo de um POST).
 */
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ContaClienteController {

    private final ContaClienteService contaClienteService;

    @GetMapping("/{cpf}/conta")
    public ResponseEntity<ContaClienteResponse> consultarConta(@PathVariable String cpf) {
        return ResponseEntity.ok(contaClienteService.consultar(cpf));
    }
}
