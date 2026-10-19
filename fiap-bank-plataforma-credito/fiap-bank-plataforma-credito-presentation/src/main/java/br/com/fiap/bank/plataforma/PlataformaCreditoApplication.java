package br.com.fiap.bank.plataforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap da Plataforma de Credito. No pacote raiz para que o scan alcance todas as camadas.
 * O @EnableFeignClients fica na infraestrutura (FeignClientsConfig), nao aqui.
 */
@SpringBootApplication
public class PlataformaCreditoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlataformaCreditoApplication.class, args);
    }
}
