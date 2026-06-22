package com.empresa.onboarding.integration.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SerproDocumentSimulador {
    private static final Logger log = LoggerFactory.getLogger(SerproDocumentSimulador.class);

    public ValidacaoDocumento validarDocumento(String numero, String tipo) {
        log.info("Validando documento {} tipo {} no Serpro", numero, tipo);
        int lastDigit = Character.getNumericValue(numero.charAt(numero.length() - 1));
        return new ValidacaoDocumento(
                lastDigit != 9,
                lastDigit != 9 ? "Documento válido" : "Documento com suspeita de fraude",
                lastDigit == 9 ? "FRAUDE_SUSPEITA" : null
        );
    }

    public record ValidacaoDocumento(boolean valido, String mensagem, String alerta) {}
}
