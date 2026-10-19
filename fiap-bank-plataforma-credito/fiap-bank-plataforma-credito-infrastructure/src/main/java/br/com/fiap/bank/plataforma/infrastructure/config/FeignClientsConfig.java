package br.com.fiap.bank.plataforma.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita os clients Feign dentro da propria infraestrutura,
 * mantendo a presentation livre de qualquer detalhe de integracao.
 */
@Configuration
@EnableFeignClients(basePackages = "br.com.fiap.bank.plataforma.infrastructure.client")
public class FeignClientsConfig {
}
