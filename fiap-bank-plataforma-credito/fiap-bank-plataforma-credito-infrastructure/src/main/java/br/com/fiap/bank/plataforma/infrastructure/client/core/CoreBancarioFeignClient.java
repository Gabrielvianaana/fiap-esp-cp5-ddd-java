package br.com.fiap.bank.plataforma.infrastructure.client.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client declarativo (Spring Cloud OpenFeign) para a API RESTful do Core Bancario.
 */
@FeignClient(name = "coreBancarioClient", url = "${core.bancario.url:http://localhost:8080/api/v1}")
public interface CoreBancarioFeignClient {

    @GetMapping("/contas/cliente/{cpf}")
    ContaCoreResponse buscarContaPorCpf(@PathVariable("cpf") String cpf);
}
