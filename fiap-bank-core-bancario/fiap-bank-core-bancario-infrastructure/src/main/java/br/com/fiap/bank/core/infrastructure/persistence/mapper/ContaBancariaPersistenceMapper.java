package br.com.fiap.bank.core.infrastructure.persistence.mapper;

import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.domain.valueobject.Saldo;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaBancariaEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Traduz o modelo relacional (JPA) para o modelo de dominio puro e vice-versa.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContaBancariaPersistenceMapper {

    public static ContaBancaria paraDominio(ContaBancariaEntity entity) {
        return ContaBancaria.reconstituir(
                entity.getId(),
                entity.getNumero(),
                entity.getAgencia(),
                Saldo.de(entity.getSaldo()),
                entity.getDataAbertura(),
                entity.getTipoConta(),
                entity.getStatus(),
                Cpf.de(entity.getTitular().getCpf())
        );
    }

    /** Copia para a entidade JPA apenas o estado que o dominio pode alterar. */
    public static void copiarEstadoMutavel(ContaBancaria conta, ContaBancariaEntity entity) {
        entity.setSaldo(conta.getSaldo().valor());
        entity.setStatus(conta.getStatus());
    }
}
