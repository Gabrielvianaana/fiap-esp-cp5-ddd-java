package br.com.fiap.bank.plataforma.infrastructure.persistence.repository;

import br.com.fiap.bank.plataforma.infrastructure.persistence.entity.SolicitacaoCreditoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoCreditoJpaRepository extends JpaRepository<SolicitacaoCreditoEntity, Long> {

    List<SolicitacaoCreditoEntity> findByCpfClienteOrderByDataSolicitacaoDesc(String cpfCliente);

    List<SolicitacaoCreditoEntity> findAllByOrderByDataSolicitacaoDesc();
}
