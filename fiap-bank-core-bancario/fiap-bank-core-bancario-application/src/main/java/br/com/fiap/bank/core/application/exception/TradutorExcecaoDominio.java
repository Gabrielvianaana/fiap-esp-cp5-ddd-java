package br.com.fiap.bank.core.application.exception;

import br.com.fiap.bank.core.domain.exception.ContaInativaException;
import br.com.fiap.bank.core.domain.exception.DomainException;
import br.com.fiap.bank.core.domain.exception.SaldoInsuficienteException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

/**
 * Anti-corruption entre camadas: a apresentacao nao enxerga o dominio, entao as excecoes
 * de dominio sao convertidas aqui em excecoes de aplicacao.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TradutorExcecaoDominio {

    public static <T> T executar(Supplier<T> acao) {
        try {
            return acao.get();
        } catch (SaldoInsuficienteException | ContaInativaException e) {
            throw new ConflitoException(e.getMessage());
        } catch (DomainException e) {
            throw new RegraNegocioException(e.getMessage());
        }
    }

    public static void executar(Runnable acao) {
        executar(() -> {
            acao.run();
            return null;
        });
    }
}
