package br.com.fiap.bank.core.infrastructure.persistence.repository;

import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.infrastructure.persistence.entity.ContaBancariaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaBancariaJpaRepository extends JpaRepository<ContaBancariaEntity, UUID> {

    Optional<ContaBancariaEntity> findByNumero(String numero);

    /** SELECT ... FOR UPDATE: serializa movimentacoes concorrentes na mesma conta. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ContaBancariaEntity c where c.numero = :numero")
    Optional<ContaBancariaEntity> findByNumeroParaAtualizacao(@Param("numero") String numero);

    List<ContaBancariaEntity> findByTitularCpfOrderByDataAberturaDesc(String cpf);

    boolean existsByNumero(String numero);

    boolean existsByTitularCpfAndStatusNot(String cpf, StatusConta status);

    void deleteByTitularCpf(String cpf);
}
