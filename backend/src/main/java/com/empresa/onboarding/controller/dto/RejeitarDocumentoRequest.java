package com.empresa.onboarding.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class RejeitarDocumentoRequest {
    @NotBlank(message = "Motivo da rejeicao e obrigatorio")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
