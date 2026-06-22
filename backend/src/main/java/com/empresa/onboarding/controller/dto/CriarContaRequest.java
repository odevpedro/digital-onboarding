package com.empresa.onboarding.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CriarContaRequest {
    @NotBlank(message = "Tipo de conta e obrigatorio")
    @Pattern(regexp = "CORRENTE|POUPANCA|SALARIO", message = "Tipo de conta deve ser CORRENTE, POUPANCA ou SALARIO")
    private String tipoConta;

    public String getTipoConta() { return tipoConta; }
    public void setTipoConta(String tipoConta) { this.tipoConta = tipoConta; }
}
