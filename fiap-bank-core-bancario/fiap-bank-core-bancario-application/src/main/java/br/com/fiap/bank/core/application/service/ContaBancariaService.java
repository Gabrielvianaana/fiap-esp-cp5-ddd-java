package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.application.dto.AtualizarSaldoRequest;
import br.com.fiap.bank.core.application.dto.ContaResponse;
import br.com.fiap.bank.core.application.dto.CriarContaRequest;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.mapper.ContaDtoMapper;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.TipoOperacao;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaBancariaRepository;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.domain.valueobject.Saldo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.random.RandomGenerator;

import static br.com.fiap.bank.core.application.exception.TradutorExcecaoDominio.executar;

/**
 * Casos de uso do recurso Conta Bancaria.
 */
@Service
@RequiredArgsConstructor
public class ContaBancariaService {

    private static final int MAX_TENTATIVAS_NUMERO = 20;

    private final ContaBancariaRepository contaRepository;
    private final ClienteRepository clienteRepository;
    private final RandomGenerator random = new SecureRandom();

    @Transactional(readOnly = true)
    public List<ContaResponse> listar() {
        return contaRepository.listarTodas().stream().map(ContaDtoMapper::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorNumero(String numeroConta) {
        return ContaDtoMapper.paraResponse(obterPorNumero(numeroConta));
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorCpfTitular(String cpfInformado) {
        Cpf cpf = executar(() -> Cpf.de(cpfInformado));
        return contaRepository.buscarPorCpfTitular(cpf)
                .map(ContaDtoMapper::paraResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma conta encontrada para o CPF " + cpf.valor() + "."));
    }

    @Transactional
    public ContaResponse abrir(CriarContaRequest request) {
        Cpf cpf = executar(() -> Cpf.de(request.cpfCliente()));

        if (!clienteRepository.existePorCpf(cpf)) {
            throw new RecursoNaoEncontradoException("Cliente titular nao encontrado para o CPF " + cpf.valor() + ".");
        }
        if (contaRepository.existeContaNaoEncerradaParaTitular(cpf)) {
            throw new ConflitoException("O cliente " + cpf.valor() + " ja possui uma conta bancaria ativa.");
        }

        ContaBancaria conta = executar(() -> ContaBancaria.abrir(
                gerarNumeroUnico(),
                request.agencia(),
                request.tipoConta(),
                Saldo.de(request.saldoInicial()),
                cpf));

        return ContaDtoMapper.paraResponse(contaRepository.salvar(conta));
    }

    /**
     * Credito/debito atomico: a conta e carregada com bloqueio pessimista dentro da mesma transacao,
     * a invariante (saldo nao negativo / conta ativa) e validada pelo dominio e o novo saldo e gravado.
     */
    @Transactional
    public ContaResponse atualizarSaldo(String numeroConta, AtualizarSaldoRequest request) {
        ContaBancaria conta = contaRepository.buscarPorNumeroParaAtualizacao(numeroConta)
                .orElseThrow(() -> naoEncontrada(numeroConta));

        executar(() -> conta.movimentar(TipoOperacao.de(request.operacao()), request.valor()));

        return ContaDtoMapper.paraResponse(contaRepository.salvar(conta));
    }

    /** Inativacao logica e idempotente (a conta permanece no historico com status ENCERRADA). */
    @Transactional
    public void encerrar(String numeroConta) {
        ContaBancaria conta = contaRepository.buscarPorNumeroParaAtualizacao(numeroConta)
                .orElseThrow(() -> naoEncontrada(numeroConta));
        if (!conta.estaEncerrada()) {
            conta.encerrar();
            contaRepository.salvar(conta);
        }
    }

    private ContaBancaria obterPorNumero(String numeroConta) {
        return contaRepository.buscarPorNumero(numeroConta).orElseThrow(() -> naoEncontrada(numeroConta));
    }

    private RecursoNaoEncontradoException naoEncontrada(String numeroConta) {
        return new RecursoNaoEncontradoException("Conta " + numeroConta + " nao encontrada.");
    }

    private String gerarNumeroUnico() {
        for (int i = 0; i < MAX_TENTATIVAS_NUMERO; i++) {
            String numero = ContaBancaria.gerarNumero(random);
            if (!contaRepository.existePorNumero(numero)) {
                return numero;
            }
        }
        throw new IllegalStateException("Nao foi possivel gerar um numero de conta unico.");
    }
}
