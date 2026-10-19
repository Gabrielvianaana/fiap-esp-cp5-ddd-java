package br.com.fiap.bank.core.infrastructure.persistence.entity;

import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.domain.model.TipoConta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo relacional (JPA) da conta bancaria.
 * O campo {@code versao} habilita lock otimista como segunda linha de defesa para
 * atualizacoes concorrentes de saldo.
 */
@Entity
@Table(name = "tb_conta_bancaria", uniqueConstraints = @UniqueConstraint(name = "uk_conta_numero", columnNames = "numero"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaBancariaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 7)
    private String numero;

    @Column(nullable = false, length = 4)
    private String agencia;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(name = "data_abertura", nullable = false)
    private LocalDate dataAbertura;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false, length = 20)
    private TipoConta tipoConta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConta status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity titular;

    @Version
    private Long versao;
}
