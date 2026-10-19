package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.ContaBancaria;
import br.com.fiap.bank.core.domain.model.TipoConta;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaBancariaRepository;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.domain.valueobject.Email;
import br.com.fiap.bank.core.domain.valueobject.Saldo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Carga de dados de demonstracao (clientes e contas usados pela Plataforma de Credito e pelo app Mobile).
 * Os dados passam pelas fabricas do dominio, entao respeitam as mesmas invariantes de um cadastro real.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargaInicialService {

    private final ClienteRepository clienteRepository;
    private final ContaBancariaRepository contaRepository;

    @Transactional
    public void carregarSeNecessario() {
        cadastrar("12345678900", "Ana Silva", "ana.silva@fiapbank.com.br", LocalDate.of(1990, 5, 15),
                new BigDecimal("7500.00"), "12345-6", new BigDecimal("12500.00"));
        cadastrar("98765432100", "Carlos Eduardo Oliveira", "carlos.oliveira@fiapbank.com.br", LocalDate.of(1985, 11, 2),
                new BigDecimal("12000.00"), "78901-2", new BigDecimal("45800.50"));
    }

    private void cadastrar(String cpfTexto, String nome, String email, LocalDate nascimento,
                           BigDecimal renda, String numeroConta, BigDecimal saldo) {
        Cpf cpf = Cpf.de(cpfTexto);
        if (clienteRepository.existePorCpf(cpf)) {
            return;
        }
        clienteRepository.salvar(Cliente.novo(nome, Email.de(email), nascimento, cpf, renda));
        contaRepository.salvar(ContaBancaria.abrir(numeroConta, ContaBancaria.AGENCIA_PADRAO, TipoConta.CORRENTE,
                Saldo.de(saldo), cpf));
        log.info(">>> Core Bancario: cliente {} carregado com a conta {}", nome, numeroConta);
    }
}
