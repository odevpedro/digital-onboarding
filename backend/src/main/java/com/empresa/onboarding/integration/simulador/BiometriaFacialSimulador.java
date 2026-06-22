package com.empresa.onboarding.integration.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BiometriaFacialSimulador {
    private static final Logger log = LoggerFactory.getLogger(BiometriaFacialSimulador.class);

    public ResultadoBiometria verificar(String selfieBase64, String documentoSelfieBase64) {
        log.info("Verificando biometria facial");
        return new ResultadoBiometria(0.95, 0.85, "APROVADO", null);
    }

    public record ResultadoBiometria(double similaridade, double confianca, String resultado, String motivo) {}
}
