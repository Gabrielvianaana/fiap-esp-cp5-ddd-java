package br.com.fiap.bank.core.application.mapper;

import br.com.fiap.bank.core.application.dto.ContaResponse;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContaDtoMapper {

    public static ContaResponse paraResponse(ContaBancaria conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getNumero(),
                conta.getAgencia(),
                conta.getSaldo().valor(),
                conta.getTipoConta().name(),
                conta.getStatus().name(),
                conta.getDataAbertura(),
                conta.getCpfTitular().valor()
        );
    }
}
