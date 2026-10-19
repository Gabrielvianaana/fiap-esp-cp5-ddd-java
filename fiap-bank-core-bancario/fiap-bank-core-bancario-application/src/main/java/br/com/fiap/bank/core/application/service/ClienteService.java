package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.application.dto.AtualizarClienteRequest;
import br.com.fiap.bank.core.application.dto.ClienteResponse;
import br.com.fiap.bank.core.application.dto.CriarClienteRequest;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.exception.RegraNegocioException;
import br.com.fiap.bank.core.application.mapper.ClienteDtoMapper;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaBancariaRepository;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static br.com.fiap.bank.core.application.exception.TradutorExcecaoDominio.executar;

/**
 * Casos de uso do recurso Cliente. Orquestra o dominio e as portas de repositorio;
 * as regras de negocio propriamente ditas ficam nas entidades e value objects.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContaBancariaRepository contaRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.listarTodos().stream().map(ClienteDtoMapper::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(UUID id) {
        return ClienteDtoMapper.paraResponse(obterPorId(id));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorCpf(String cpfInformado) {
        Cpf cpf = executar(() -> Cpf.de(cpfInformado));
        return clienteRepository.buscarPorCpf(cpf)
                .map(ClienteDtoMapper::paraResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado para o CPF " + cpf.valor() + "."));
    }

    @Transactional
    public ClienteResponse criar(CriarClienteRequest request) {
        Cliente cliente = executar(() -> Cliente.novo(
                request.nome(),
                Email.de(request.email()),
                request.dataNascimento(),
                Cpf.de(request.cpf()),
                request.rendaMensal()));

        if (clienteRepository.existePorCpf(cliente.getCpf())) {
            throw new ConflitoException("Ja existe um cliente cadastrado com o CPF " + cliente.getCpf().valor() + ".");
        }
        return ClienteDtoMapper.paraResponse(clienteRepository.salvar(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(UUID id, AtualizarClienteRequest request) {
        Cliente cliente = obterPorId(id);

        if (request.cpf() != null && !executar(() -> Cpf.de(request.cpf())).equals(cliente.getCpf())) {
            throw new RegraNegocioException("O CPF e a identidade do correntista e nao pode ser alterado.");
        }

        executar(() -> cliente.atualizarCadastro(
                request.nome(),
                Email.de(request.email()),
                request.dataNascimento(),
                request.rendaMensal()));

        return ClienteDtoMapper.paraResponse(clienteRepository.salvar(cliente));
    }

    @Transactional
    public void remover(UUID id) {
        Cliente cliente = obterPorId(id);

        if (contaRepository.existeContaNaoEncerradaParaTitular(cliente.getCpf())) {
            throw new ConflitoException("O cliente possui conta bancaria ativa. Encerre a conta antes de remover o cadastro.");
        }
        contaRepository.removerContasDoTitular(cliente.getCpf());
        clienteRepository.remover(id);
    }

    private Cliente obterPorId(UUID id) {
        return clienteRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com o id " + id + "."));
    }
}
