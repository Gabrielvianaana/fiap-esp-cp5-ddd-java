package br.com.fiap.bank.core.domain.model;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import br.com.fiap.bank.core.domain.valueobject.Cpf;
import br.com.fiap.bank.core.domain.valueobject.Email;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Entidade de dominio (raiz de agregado) Cliente / correntista.
 * Classe Java pura: nenhuma anotacao JPA. Igualdade pela identidade (id), nao pelos atributos.
 * CPF e e-mail sao Value Objects que se auto-validam.
 */
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cliente {

    @EqualsAndHashCode.Include
    private final UUID id;
    private String nome;
    private Email email;
    private LocalDate dataNascimento;
    private final Cpf cpf;
    private BigDecimal rendaMensal;

    /** Conta bancaria vigente do cliente (pode nao existir). */
    @ToString.Exclude
    private final ContaBancaria conta;

    private Cliente(UUID id, String nome, Email email, LocalDate dataNascimento, Cpf cpf,
                    BigDecimal rendaMensal, ContaBancaria conta) {
        this.id = Objects.requireNonNull(id, "id e obrigatorio");
        this.cpf = Objects.requireNonNull(cpf, "cpf e obrigatorio");
        this.conta = conta;
        aplicarDadosCadastrais(nome, email, dataNascimento, rendaMensal);
    }

    /** Fabrica para cadastro de um novo correntista. */
    public static Cliente novo(String nome, Email email, LocalDate dataNascimento, Cpf cpf, BigDecimal rendaMensal) {
        return new Cliente(UUID.randomUUID(), nome, email, dataNascimento, cpf, rendaMensal, null);
    }

    /** Fabrica para reconstituir um cliente persistido (usada pelos adapters de persistencia). */
    public static Cliente reconstituir(UUID id, String nome, Email email, LocalDate dataNascimento, Cpf cpf,
                                       BigDecimal rendaMensal, ContaBancaria conta) {
        return new Cliente(id, nome, email, dataNascimento, cpf, rendaMensal, conta);
    }

    /** Substituicao integral dos dados cadastrais (o CPF e a identidade de negocio e nao muda). */
    public void atualizarCadastro(String nome, Email email, LocalDate dataNascimento, BigDecimal rendaMensal) {
        aplicarDadosCadastrais(nome, email, dataNascimento, rendaMensal);
    }

    public Optional<ContaBancaria> contaVigente() {
        return Optional.ofNullable(conta);
    }

    public boolean possuiContaNaoEncerrada() {
        return conta != null && !conta.estaEncerrada();
    }

    private void aplicarDadosCadastrais(String nome, Email email, LocalDate dataNascimento, BigDecimal rendaMensal) {
        if (nome == null || nome.isBlank() || nome.trim().length() < 3 || nome.trim().length() > 150) {
            throw new DadoInvalidoException("O nome deve ter entre 3 e 150 caracteres.");
        }
        if (email == null) {
            throw new DadoInvalidoException("O e-mail e obrigatorio.");
        }
        if (dataNascimento == null || !dataNascimento.isBefore(LocalDate.now())) {
            throw new DadoInvalidoException("A data de nascimento deve estar no passado.");
        }
        if (rendaMensal == null || rendaMensal.signum() < 0) {
            throw new DadoInvalidoException("A renda mensal nao pode ser negativa.");
        }
        this.nome = nome.trim();
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.rendaMensal = rendaMensal;
    }
}
