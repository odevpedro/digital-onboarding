package com.empresa.onboarding.integration.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PepGafiSimulador {
    private static final Logger log = LoggerFactory.getLogger(PepGafiSimulador.class);

    public ResultadoPep consultar(String cpfCnpj, String nomeCompleto) {
        log.info("Consultando PEP/GAFI para: {}", nomeCompleto);
        return new ResultadoPep(false, null, false, null);
    }

    public record ResultadoPep(boolean isPep, String cargoPep, boolean isPaisAltoRisco, String paisAltoRisco) {}
}
