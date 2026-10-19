package br.com.fiap.bank.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap do Core Bancario.
 * Fica no pacote raiz (br.com.fiap.bank.core) para que o component scan, o scan de entidades JPA
 * e o de repositorios Spring Data alcancem os pacotes de todas as camadas sem que a presentation
 * precise importar nenhuma classe de domain ou infrastructure.
 */
@SpringBootApplication
public class CoreBancarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBancarioApplication.class, args);
    }
}
