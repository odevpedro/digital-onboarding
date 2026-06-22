package com.empresa.onboarding.domain.compliance;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validacoes_compliance")
public class ValidacaoCompliance {
    @Id
    private String id;
    @Column(nullable = false)
    private String propostaId;
    @Column(nullable = false)
    private String tipoValidacao;
    @Column(nullable = false)
    private String status;
    private String resultado;
    @Column(columnDefinition = "TEXT")
    private String detalhes;
    private LocalDateTime realizadoEm;
    private LocalDateTime analisadoEm;
    private String analistaResponsavel;
    private String correlationId;

    public ValidacaoCompliance() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPropostaId() { return propostaId; }
    public void setPropostaId(String propostaId) { this.propostaId = propostaId; }
    public String getTipoValidacao() { return tipoValidacao; }
    public void setTipoValidacao(String tipoValidacao) { this.tipoValidacao = tipoValidacao; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public LocalDateTime getRealizadoEm() { return realizadoEm; }
    public void setRealizadoEm(LocalDateTime realizadoEm) { this.realizadoEm = realizadoEm; }
    public LocalDateTime getAnalisadoEm() { return analisadoEm; }
    public void setAnalisadoEm(LocalDateTime analisadoEm) { this.analisadoEm = analisadoEm; }
    public String getAnalistaResponsavel() { return analistaResponsavel; }
    public void setAnalistaResponsavel(String analistaResponsavel) { this.analistaResponsavel = analistaResponsavel; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
