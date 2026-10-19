package br.com.fiap.bank.plataforma.presentation.adapter.legado.dto;

import jakarta.validation.constraints.NotBlank;

/** Contrato historico: {"cpfCliente": "..."} (usado por consultarStatusCredito e consultarClienteCore). */
public record LegadoConsultarStatusRequest(
        @NotBlank(message = "O CPF do cliente e obrigatorio.")
        String cpfCliente
) {
}
