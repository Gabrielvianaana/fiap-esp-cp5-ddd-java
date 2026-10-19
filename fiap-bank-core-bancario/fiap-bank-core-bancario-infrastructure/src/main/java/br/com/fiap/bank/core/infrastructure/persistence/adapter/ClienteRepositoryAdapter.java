package br.com.fiap.bank.core.infrastructure.persistence.adapter;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteEntity;
import br.com.fiap.bank.core.infrastructure.persistence.mapper.ClientePersistenceMapper;
import br.com.fiap.bank.core.infrastructure.persistence.repository.ClienteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter (Ports & Adapters): implementa a porta de dominio ClienteRepository usando Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpaRepository;

    @Override
    public Cliente salvar(Cliente cliente) {
        ClienteEntity entity = jpaRepository.findById(cliente.getId()).orElseGet(ClienteEntity::new);
        ClientePersistenceMapper.copiarParaEntidade(cliente, entity);
        return ClientePersistenceMapper.paraDominio(jpaRepository.save(entity));
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(ClientePersistenceMapper::paraDominio);
    }

    @Override
    public Optional<Cliente> buscarPorCpf(Cpf cpf) {
        return jpaRepository.findByCpf(cpf.valor()).map(ClientePersistenceMapper::paraDominio);
    }

    @Override
    public List<Cliente> listarTodos() {
        return jpaRepository.findAll().stream().map(ClientePersistenceMapper::paraDominio).toList();
    }

    @Override
    public boolean existePorCpf(Cpf cpf) {
        return jpaRepository.existsByCpf(cpf.valor());
    }

    @Override
    public void remover(UUID id) {
        jpaRepository.deleteById(id);
    }
}
