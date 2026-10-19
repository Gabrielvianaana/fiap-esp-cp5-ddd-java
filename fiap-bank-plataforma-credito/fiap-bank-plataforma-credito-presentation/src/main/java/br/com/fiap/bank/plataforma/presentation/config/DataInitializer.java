package br.com.fiap.bank.plataforma.presentation.config;

import br.com.fiap.bank.plataforma.application.service.CargaInicialService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Dispara a carga das propostas de demonstracao na subida da aplicacao. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CargaInicialService cargaInicialService;

    @Override
    public void run(String... args) {
        cargaInicialService.carregarSeNecessario();
    }
}
