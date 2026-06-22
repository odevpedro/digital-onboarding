package com.empresa.onboarding.domain.risco;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "analises_risco")
public class AnaliseRisco {
    @Id
    private String id;
    @Column(nullable = false)
    private String propostaId;
    private Integer score;
    private String nivelRisco;
    private BigDecimal rendaInformada;
    private BigDecimal rendaConfirmada;
    private BigDecimal patrimonioEstimado;
    private boolean possuiRestricao;
    private String detalhesRestricao;
    private boolean pepIdentificado;
    private String cargoPep;
    private boolean paisAltoRisco;
    private String pais;
    private LocalDateTime analiseRealizadaEm;
    private String correlationId;

    public AnaliseRisco() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPropostaId() { return propostaId; }
    public void setPropostaId(String propostaId) { this.propostaId = propostaId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getNivelRisco() { return nivelRisco; }
    public void setNivelRisco(String nivelRisco) { this.nivelRisco = nivelRisco; }
    public BigDecimal getRendaInformada() { return rendaInformada; }
    public void setRendaInformada(BigDecimal rendaInformada) { this.rendaInformada = rendaInformada; }
    public BigDecimal getRendaConfirmada() { return rendaConfirmada; }
    public void setRendaConfirmada(BigDecimal rendaConfirmada) { this.rendaConfirmada = rendaConfirmada; }
    public BigDecimal getPatrimonioEstimado() { return patrimonioEstimado; }
    public void setPatrimonioEstimado(BigDecimal patrimonioEstimado) { this.patrimonioEstimado = patrimonioEstimado; }
    public boolean isPossuiRestricao() { return possuiRestricao; }
    public void setPossuiRestricao(boolean possuiRestricao) { this.possuiRestricao = possuiRestricao; }
    public String getDetalhesRestricao() { return detalhesRestricao; }
    public void setDetalhesRestricao(String detalhesRestricao) { this.detalhesRestricao = detalhesRestricao; }
    public boolean isPepIdentificado() { return pepIdentificado; }
    public void setPepIdentificado(boolean pepIdentificado) { this.pepIdentificado = pepIdentificado; }
    public String getCargoPep() { return cargoPep; }
    public void setCargoPep(String cargoPep) { this.cargoPep = cargoPep; }
    public boolean isPaisAltoRisco() { return paisAltoRisco; }
    public void setPaisAltoRisco(boolean paisAltoRisco) { this.paisAltoRisco = paisAltoRisco; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public LocalDateTime getAnaliseRealizadaEm() { return analiseRealizadaEm; }
    public void setAnaliseRealizadaEm(LocalDateTime analiseRealizadaEm) { this.analiseRealizadaEm = analiseRealizadaEm; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
