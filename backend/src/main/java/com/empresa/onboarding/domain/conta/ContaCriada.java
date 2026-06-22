package com.empresa.onboarding.domain.conta;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contas_criadas")
public class ContaCriada {
    @Id
    private String id;
    @Column(nullable = false)
    private String propostaId;
    private String agencia;
    private String numeroConta;
    private String digito;
    private String tipoConta;
    private String ispb;
    private String status;
    private String chavePix;
    private String tipoChavePix;
    private LocalDateTime ativadaEm;
    private String correlationId;

    public ContaCriada() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPropostaId() { return propostaId; }
    public void setPropostaId(String propostaId) { this.propostaId = propostaId; }
    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }
    public String getDigito() { return digito; }
    public void setDigito(String digito) { this.digito = digito; }
    public String getTipoConta() { return tipoConta; }
    public void setTipoConta(String tipoConta) { this.tipoConta = tipoConta; }
    public String getIspb() { return ispb; }
    public void setIspb(String ispb) { this.ispb = ispb; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }
    public String getTipoChavePix() { return tipoChavePix; }
    public void setTipoChavePix(String tipoChavePix) { this.tipoChavePix = tipoChavePix; }
    public LocalDateTime getAtivadaEm() { return ativadaEm; }
    public void setAtivadaEm(LocalDateTime ativadaEm) { this.ativadaEm = ativadaEm; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
