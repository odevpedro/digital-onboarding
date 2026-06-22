package com.empresa.onboarding.integration.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReceitaFederalSimulador {
    private static final Logger log = LoggerFactory.getLogger(ReceitaFederalSimulador.class);
    private final Map<String, SituacaoFiscal> cache = new ConcurrentHashMap<>();

    public SituacaoFiscal consultar(String cpfCnpj) {
        return cache.computeIfAbsent(limpar(cpfCnpj), this::simular);
    }

    private String limpar(String cpfCnpj) {
        return cpfCnpj.replaceAll("[^0-9]", "");
    }

    private SituacaoFiscal simular(String cpfCnpj) {
        log.info("Consultando Receita Federal para: {}", cpfCnpj);
        int lastDigit = Character.getNumericValue(cpfCnpj.charAt(cpfCnpj.length() - 1));
        boolean regular = lastDigit != 0;
        return new SituacaoFiscal(
                cpfCnpj,
                regular ? "REGULAR" : "SUSPENSA",
                regular ? null : "CPF/CNPJ com situação irregular na Receita Federal",
                "BRASIL",
                cpfCnpj.length() == 11 ? "PF" : "PJ"
        );
    }

    public record SituacaoFiscal(String documento, String situacao, String motivo, String pais, String tipoPessoa) {}
}
