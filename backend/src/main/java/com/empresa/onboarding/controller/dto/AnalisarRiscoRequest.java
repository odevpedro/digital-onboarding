package com.empresa.onboarding.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AnalisarRiscoRequest {
    @NotNull(message = "Renda informada e obrigatoria")
    @DecimalMin(value = "0.01", message = "Renda informada deve ser maior que zero")
    private BigDecimal rendaInformada;

    public BigDecimal getRendaInformada() { return rendaInformada; }
    public void setRendaInformada(BigDecimal rendaInformada) { this.rendaInformada = rendaInformada; }
}
