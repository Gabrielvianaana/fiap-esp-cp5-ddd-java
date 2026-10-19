package br.com.fiap.bank.core.domain.repository;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.valueobject.Cpf;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta (Port) de persistencia de clientes.
 * O dominio define O QUE precisa; a infraestrutura decide COMO (JPA, JDBC, memoria...).
 */
public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    Optional<Cliente> buscarPorCpf(Cpf cpf);

    List<Cliente> listarTodos();

    boolean existePorCpf(Cpf cpf);

    void remover(UUID id);
}
