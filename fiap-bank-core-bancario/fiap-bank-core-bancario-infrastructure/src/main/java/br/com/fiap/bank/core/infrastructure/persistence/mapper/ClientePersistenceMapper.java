package br.com.fiap.bank.core.infrastructure.persistence.mapper;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.domain.valueobject.Email;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ClienteEntity;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaBancariaEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClientePersistenceMapper {

    public static Cliente paraDominio(ClienteEntity entity) {
        List<ContaBancariaEntity> contas = entity.getContas() == null ? List.of() : entity.getContas();
        ContaBancaria contaVigente = contas.stream()
                .sorted(Comparator
                        .comparing((ContaBancariaEntity c) -> c.getStatus() == StatusConta.ENCERRADA)
                        .thenComparing(ContaBancariaEntity::getDataAbertura, Comparator.reverseOrder()))
                .findFirst()
                .map(ContaBancariaPersistenceMapper::paraDominio)
                .orElse(null);

        return Cliente.reconstituir(
                entity.getId(),
                entity.getNome(),
                Email.de(entity.getEmail()),
                entity.getDataNascimento(),
                Cpf.de(entity.getCpf()),
                entity.getRendaMensal(),
                contaVigente
        );
    }

    public static void copiarParaEntidade(Cliente cliente, ClienteEntity entity) {
        entity.setId(cliente.getId());
        entity.setCpf(cliente.getCpf().valor());
        entity.setNome(cliente.getNome());
        entity.setEmail(cliente.getEmail().valor());
        entity.setDataNascimento(cliente.getDataNascimento());
        entity.setRendaMensal(cliente.getRendaMensal());
    }
}
