package com.empresa.onboarding.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SolicitarConsentimentoRequest {
    @NotEmpty(message = "Ao menos uma permissao deve ser informada")
    private List<String> permissoes;

    public List<String> getPermissoes() { return permissoes; }
    public void setPermissoes(List<String> permissoes) { this.permissoes = permissoes; }
}
