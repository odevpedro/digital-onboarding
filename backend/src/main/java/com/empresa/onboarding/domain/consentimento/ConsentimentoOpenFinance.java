package com.empresa.onboarding.domain.consentimento;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "consentimentos_open_finance")
public class ConsentimentoOpenFinance {
    @Id
    private String id;
    @Column(nullable = false)
    private String propostaId;
    @Column(unique = true)
    private String consentId;
    @Column(nullable = false)
    private String cpfCnpj;
    @Column(nullable = false)
    private String status;
    @Column(columnDefinition = "TEXT")
    private String permissoes;
    private OffsetDateTime dataCriacao;
    private OffsetDateTime dataExpiracao;
    private OffsetDateTime dataAutorizacao;
    private String correlationId;

    public ConsentimentoOpenFinance() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPropostaId() { return propostaId; }
    public void setPropostaId(String propostaId) { this.propostaId = propostaId; }
    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }
    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPermissoes() { return permissoes; }
    public void setPermissoes(String permissoes) { this.permissoes = permissoes; }
    public OffsetDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(OffsetDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public OffsetDateTime getDataExpiracao() { return dataExpiracao; }
    public void setDataExpiracao(OffsetDateTime dataExpiracao) { this.dataExpiracao = dataExpiracao; }
    public OffsetDateTime getDataAutorizacao() { return dataAutorizacao; }
    public void setDataAutorizacao(OffsetDateTime dataAutorizacao) { this.dataAutorizacao = dataAutorizacao; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
