package com.empresa.onboarding.integration.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListaRestritivaSimulador {
    private static final Logger log = LoggerFactory.getLogger(ListaRestritivaSimulador.class);

    public ResultadoLista consultar(String cpfCnpj) {
        log.info("Consultando listas restritivas para: {}", cpfCnpj);
        int lastDigit = Character.getNumericValue(cpfCnpj.charAt(cpfCnpj.length() - 1));
        if (lastDigit == 0) {
            return new ResultadoLista(true, List.of("Nome presente na lista de PEPs internacionais"), "ALTO");
        }
        return new ResultadoLista(false, List.of(), "BAIXO");
    }

    public record ResultadoLista(boolean possuiRestricao, List<String> restricoes, String gravidade) {}
}
