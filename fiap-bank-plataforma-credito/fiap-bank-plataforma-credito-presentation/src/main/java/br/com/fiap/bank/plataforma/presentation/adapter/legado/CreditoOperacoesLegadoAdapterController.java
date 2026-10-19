package br.com.fiap.bank.plataforma.presentation.adapter.legado;

import br.com.fiap.bank.plataforma.application.dto.ContaClienteResponse;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;
import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.application.service.ContaClienteService;
import br.com.fiap.bank.plataforma.application.service.SolicitacaoCreditoService;
import br.com.fiap.bank.plataforma.presentation.adapter.legado.dto.LegadoAprovarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.presentation.adapter.legado.dto.LegadoBuscarSolicitacaoRequest;
import br.com.fiap.bank.plataforma.presentation.adapter.legado.dto.LegadoConsultarStatusRequest;
import br.com.fiap.bank.plataforma.presentation.adapter.legado.dto.LegadoReprovarSolicitacaoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * CONTROLLER ADAPTADOR (padrao Adapter) - retrocompatibilidade com os front-ends em producao.
 *
 * Preserva exatamente as rotas, verbos, payloads e status do contrato historico (/api/credito/*, tudo POST),
 * mas NAO tem regra de negocio: apenas traduz o contrato antigo e delega para os MESMOS casos de uso
 * usados pela API RESTful /api/v1. Assim legado e novo nunca divergem.
 *
 * Cada resposta sinaliza a descontinuacao (cabecalhos Deprecation e Link successor-version),
 * permitindo que os clientes migrem para a API RESTful no seu proprio ritmo (Strangler Fig).
 */
@Deprecated(since = "2.0")
@RestController
@RequestMapping("/api/credito")
@RequiredArgsConstructor
public class CreditoOperacoesLegadoAdapterController {

    private final SolicitacaoCreditoService solicitacaoService;
    private final ContaClienteService contaClienteService;

    /** Legado: POST /solicitarCredito (200). Novo: POST /api/v1/solicitacoes (201). */
    @PostMapping("/solicitarCredito")
    public ResponseEntity<SolicitacaoCreditoResponse> solicitarCredito(@Valid @RequestBody SolicitarCreditoRequest request) {
        return ok(solicitacaoService.solicitar(request), "/api/v1/solicitacoes");
    }

    /** Legado: POST /consultarStatusCredito. Novo: GET /api/v1/solicitacoes?cpf={cpf}. */
    @PostMapping("/consultarStatusCredito")
    public ResponseEntity<List<SolicitacaoCreditoResponse>> consultarStatusCredito(@Valid @RequestBody LegadoConsultarStatusRequest request) {
        return ok(solicitacaoService.listar(Optional.of(request.cpfCliente())), "/api/v1/solicitacoes?cpf={cpf}");
    }

    /** Legado: POST /buscarSolicitacaoPorId. Novo: GET /api/v1/solicitacoes/{id}. */
    @PostMapping("/buscarSolicitacaoPorId")
    public ResponseEntity<SolicitacaoCreditoResponse> buscarSolicitacaoPorId(@Valid @RequestBody LegadoBuscarSolicitacaoRequest request) {
        return ok(solicitacaoService.buscarPorId(request.solicitacaoId()), "/api/v1/solicitacoes/" + request.solicitacaoId());
    }

    /** Legado: POST /listarTodasSolicitacoes (corpo vazio). Novo: GET /api/v1/solicitacoes. */
    @PostMapping("/listarTodasSolicitacoes")
    public ResponseEntity<List<SolicitacaoCreditoResponse>> listarTodasSolicitacoes() {
        return ok(solicitacaoService.listar(Optional.empty()), "/api/v1/solicitacoes");
    }

    /** Legado: POST /aprovarSolicitacao. Novo: PUT /api/v1/solicitacoes/{id}/decisao. */
    @PostMapping("/aprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoResponse> aprovarSolicitacao(@Valid @RequestBody LegadoAprovarSolicitacaoRequest request) {
        return ok(solicitacaoService.aprovar(request.solicitacaoId(), request.parecerOperador()),
                "/api/v1/solicitacoes/" + request.solicitacaoId() + "/decisao");
    }

    /** Legado: POST /reprovarSolicitacao. Novo: PUT /api/v1/solicitacoes/{id}/decisao. */
    @PostMapping("/reprovarSolicitacao")
    public ResponseEntity<SolicitacaoCreditoResponse> reprovarSolicitacao(@Valid @RequestBody LegadoReprovarSolicitacaoRequest request) {
        return ok(solicitacaoService.reprovar(request.solicitacaoId(), request.motivoReprovacao()),
                "/api/v1/solicitacoes/" + request.solicitacaoId() + "/decisao");
    }

    /** Legado: POST /consultarClienteCore. Novo: GET /api/v1/clientes/{cpf}/conta. */
    @PostMapping("/consultarClienteCore")
    public ResponseEntity<ContaClienteResponse> consultarClienteCore(@Valid @RequestBody LegadoConsultarStatusRequest request) {
        return ok(contaClienteService.consultar(request.cpfCliente()), "/api/v1/clientes/{cpf}/conta");
    }

    private static <T> ResponseEntity<T> ok(T body, String sucessor) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Deprecation", "true");
        headers.add(HttpHeaders.LINK, "<" + sucessor + ">; rel=\"successor-version\"");
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
