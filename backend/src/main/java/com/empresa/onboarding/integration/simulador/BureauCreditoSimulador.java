package com.empresa.onboarding.integration.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BureauCreditoSimulador {
    private static final Logger log = LoggerFactory.getLogger(BureauCreditoSimulador.class);

    public ScoreCredito consultar(String cpfCnpj) {
        log.info("Consultando bureau de credito para: {}", cpfCnpj);
        int lastDigit = Character.getNumericValue(cpfCnpj.charAt(cpfCnpj.length() - 1));
        int score = switch (lastDigit) {
            case 0, 1 -> 300;
            case 2, 3 -> 450;
            case 4, 5 -> 600;
            case 6, 7 -> 750;
            default -> 850;
        };
        return new ScoreCredito(score, score >= 600 ? "BAIXO" : "ALTO",
                score >= 600 ? null : "Score abaixo do minimo aceitavel");
    }

    public record ScoreCredito(int score, String nivelRisco, String mensagem) {}
}
