package br.com.fiap.bank.plataforma.application.exception;

import br.com.fiap.bank.plataforma.domain.exception.DomainException;
import br.com.fiap.bank.plataforma.domain.exception.IntegracaoIndisponivelException;
import br.com.fiap.bank.plataforma.domain.exception.TransicaoStatusInvalidaException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

/**
 * Converte excecoes de dominio em excecoes de aplicacao, ja que a presentation nao enxerga o dominio.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TradutorExcecaoDominio {

    public static <T> T executar(Supplier<T> acao) {
        try {
            return acao.get();
        } catch (TransicaoStatusInvalidaException e) {
            throw new ConflitoException(e.getMessage());
        } catch (IntegracaoIndisponivelException e) {
            throw new ServicoIndisponivelException(e.getMessage());
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
