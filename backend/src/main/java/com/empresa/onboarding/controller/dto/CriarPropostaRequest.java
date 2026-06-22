package com.empresa.onboarding.controller.dto;

import com.empresa.onboarding.controller.dto.validation.CpfCnpj;
import jakarta.validation.constraints.NotBlank;

public class CriarPropostaRequest {
    @NotBlank(message = "CPF/CNPJ e obrigatorio")
    @CpfCnpj
    private String cpfCnpj;

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }
}
