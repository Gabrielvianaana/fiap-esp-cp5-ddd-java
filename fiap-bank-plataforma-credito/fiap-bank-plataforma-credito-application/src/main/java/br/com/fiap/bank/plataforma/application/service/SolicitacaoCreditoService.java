package br.com.fiap.bank.plataforma.application.service;

import br.com.fiap.bank.plataforma.application.dto.DecisaoSolicitacaoRequest;
import br.com.fiap.bank.plataforma.application.dto.SolicitacaoCreditoResponse;
import br.com.fiap.bank.plataforma.application.dto.SolicitarCreditoRequest;
import br.com.fiap.bank.plataforma.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.plataforma.application.mapper.SolicitacaoCreditoDtoMapper;
import br.com.fiap.bank.plataforma.domain.exception.IntegracaoIndisponivelException;
import br.com.fiap.bank.plataforma.domain.gateway.BureauCreditoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.CoreBancarioGateway;
import br.com.fiap.bank.plataforma.domain.gateway.CrmGateway;
import br.com.fiap.bank.plataforma.domain.gateway.NotificacaoGateway;
import br.com.fiap.bank.plataforma.domain.gateway.ScrGateway;
import br.com.fiap.bank.plataforma.domain.model.SolicitacaoCredito;
import br.com.fiap.bank.plataforma.domain.repository.SolicitacaoCreditoRepository;
import br.com.fiap.bank.plataforma.domain.service.MotorRegrasCredito;
import br.com.fiap.bank.plataforma.domain.valueobject.AnaliseRisco;
import br.com.fiap.bank.plataforma.domain.valueobject.Cpf;
import br.com.fiap.bank.plataforma.domain.valueobject.PosicaoContaCore;
import br.com.fiap.bank.plataforma.domain.valueobject.ResultadoAnalise;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static br.com.fiap.bank.plataforma.application.exception.TradutorExcecaoDominio.executar;

/**
 * Casos de uso da esteira de credito: solicitar, consultar, decidir (Mesa) e cancelar.
 * Orquestra as portas de integracao e delega a decisao ao Domain Service MotorRegrasCredito.
 */
@Service
@RequiredArgsConstructor
public class SolicitacaoCreditoService {

    private final SolicitacaoCreditoRepository repository;
    private final ScrGateway scrGateway;
    private final BureauCreditoGateway bureauGateway;
    private final CrmGateway crmGateway;
    private final CoreBancarioGateway coreBancarioGateway;
    private final NotificacaoGateway notificacaoGateway;
    private final MotorRegrasCredito motorRegras = new MotorRegrasCredito();

    @Transactional
    public SolicitacaoCreditoResponse solicitar(SolicitarCreditoRequest request) {
        SolicitacaoCredito solicitacao = executar(() -> SolicitacaoCredito.nova(
                Cpf.de(request.cpfCliente()),
                request.nomeCliente(),
                request.tipoCredito(),
                request.valorSolicitado(),
                request.quantidadeParcelas(),
                request.rendaMensal()));

        AnaliseRisco analise = coletarIndicadores(solicitacao.getCpfCliente());
        ResultadoAnalise resultado = motorRegras.avaliar(solicitacao, analise);
        solicitacao.registrarAnaliseAutomatica(analise, resultado);

        SolicitacaoCredito salva = repository.salvar(solicitacao);
        notificacaoGateway.notificar(salva.getCpfCliente(),
                "Sua solicitacao de credito #" + salva.getId() + " esta no status: " + salva.getStatus());
        return SolicitacaoCreditoDtoMapper.paraResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCreditoResponse> listar(Optional<String> cpfFiltro) {
        List<SolicitacaoCredito> solicitacoes = cpfFiltro
                .filter(cpf -> !cpf.isBlank())
                .map(cpf -> repository.listarPorCpf(executar(() -> Cpf.de(cpf))))
                .orElseGet(repository::listarTodas);
        return solicitacoes.stream().map(SolicitacaoCreditoDtoMapper::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoCreditoResponse buscarPorId(Long id) {
        return SolicitacaoCreditoDtoMapper.paraResponse(obter(id));
    }

    /** Decisao discricionaria da Mesa de Operacoes (sub-recurso "decisao"). */
    @Transactional
    public SolicitacaoCreditoResponse registrarDecisao(Long id, DecisaoSolicitacaoRequest request) {
        return request.aprovado() ? aprovar(id, request.parecer()) : reprovar(id, request.parecer());
    }

    @Transactional
    public SolicitacaoCreditoResponse aprovar(Long id, String parecer) {
        SolicitacaoCredito solicitacao = obter(id);
        executar(() -> solicitacao.aprovarPelaMesa(parecer));
        SolicitacaoCredito salva = repository.salvar(solicitacao);
        notificacaoGateway.notificar(salva.getCpfCliente(),
                "PARABENS! Sua solicitacao de credito #" + salva.getId() + " foi APROVADA pela Mesa de Operacoes.");
        return SolicitacaoCreditoDtoMapper.paraResponse(salva);
    }

    @Transactional
    public SolicitacaoCreditoResponse reprovar(Long id, String motivo) {
        SolicitacaoCredito solicitacao = obter(id);
        executar(() -> solicitacao.reprovarPelaMesa(motivo));
        SolicitacaoCredito salva = repository.salvar(solicitacao);
        notificacaoGateway.notificar(salva.getCpfCliente(),
                "Sua solicitacao de credito #" + salva.getId() + " foi REPROVADA pela Mesa de Operacoes.");
        return SolicitacaoCreditoDtoMapper.paraResponse(salva);
    }

    /** Cancelamento logico (idempotente) de uma proposta ainda pendente. */
    @Transactional
    public void cancelar(Long id) {
        SolicitacaoCredito solicitacao = obter(id);
        executar(solicitacao::cancelar);
        repository.salvar(solicitacao);
    }

    private SolicitacaoCredito obter(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao de credito nao encontrada com ID: " + id));
    }

    private AnaliseRisco coletarIndicadores(Cpf cpf) {
        boolean restricaoScr = scrGateway.possuiRestricoes(cpf);
        int score = bureauGateway.consultarScore(cpf);
        var historico = crmGateway.consultarHistorico(cpf);

        Optional<PosicaoContaCore> conta = Optional.empty();
        boolean coreDisponivel = true;
        try {
            conta = coreBancarioGateway.consultarConta(cpf);
        } catch (IntegracaoIndisponivelException e) {
            coreDisponivel = false;
        }
        return new AnaliseRisco(restricaoScr, score, historico, conta, coreDisponivel);
    }
}
