package com.empresa.onboarding.controller.dto;

import jakarta.validation.constraints.NotBlank;

public class AvancarPropostaRequest {
    @NotBlank(message = "novoStatus e obrigatorio")
    private String novoStatus;

    private String observacao;

    public String getNovoStatus() { return novoStatus; }
    public void setNovoStatus(String novoStatus) { this.novoStatus = novoStatus; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
