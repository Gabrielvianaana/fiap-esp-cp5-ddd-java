package br.com.fiap.bank.core.application.mapper;

import br.com.fiap.bank.core.application.dto.ClienteResponse;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClienteDtoMapper {

    public static ClienteResponse paraResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getCpf().valor(),
                cliente.getNome(),
                cliente.getEmail().valor(),
                cliente.getDataNascimento(),
                cliente.getRendaMensal(),
                cliente.contaVigente().map(ContaBancaria::getNumero).orElse(null)
        );
    }
}
