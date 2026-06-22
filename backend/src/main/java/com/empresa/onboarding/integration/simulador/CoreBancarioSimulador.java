package com.empresa.onboarding.integration.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CoreBancarioSimulador {
    private static final Logger log = LoggerFactory.getLogger(CoreBancarioSimulador.class);
    private static final String ISPB = "00000000";

    public ContaCriada criarConta(String cpfCnpj, String nomeTitular, String tipoConta) {
        log.info("Criando conta no Core Bancario para: {}", nomeTitular);
        Random random = new Random(cpfCnpj.hashCode());
        String agencia = String.format("%04d", random.nextInt(10000));
        String numero = String.format("%08d", random.nextInt(100000000));
        String digito = String.valueOf(random.nextInt(10));
        return new ContaCriada(agencia, numero, digito, tipoConta, "ATIVA");
    }

    public void ativarConta(String agencia, String numeroConta) {
        log.info("Ativando conta {}/{} no Core Bancario", agencia, numeroConta);
    }

    public record ContaCriada(String agencia, String numeroConta, String digito, String tipoConta, String status) {}
}
