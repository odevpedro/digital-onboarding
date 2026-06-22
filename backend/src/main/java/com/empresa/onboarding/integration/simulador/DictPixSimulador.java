package com.empresa.onboarding.integration.simulador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DictPixSimulador {
    private static final Logger log = LoggerFactory.getLogger(DictPixSimulador.class);

    public ChavePixRegistrada registrarChave(String chave, String tipo, String ispb, String agencia, String conta) {
        log.info("Registrando chave Pix {} tipo {} no DICT", chave, tipo);
        return new ChavePixRegistrada(chave, tipo, ispb, agencia, conta, "ATIVA");
    }

    public record ChavePixRegistrada(String chave, String tipo, String ispb, String agencia, String conta, String status) {}
}
