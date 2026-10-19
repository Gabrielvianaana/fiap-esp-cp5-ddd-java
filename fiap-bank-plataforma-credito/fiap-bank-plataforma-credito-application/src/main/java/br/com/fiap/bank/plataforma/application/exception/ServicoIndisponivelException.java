package br.com.fiap.bank.plataforma.application.exception;

/** Um sistema externo nao respondeu. Traduzido para HTTP 503. */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String message) {
        super(message);
    }
}
