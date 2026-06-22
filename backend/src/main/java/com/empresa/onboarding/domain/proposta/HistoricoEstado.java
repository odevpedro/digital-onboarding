package com.empresa.onboarding.domain.proposta;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_estados_proposta")
public class HistoricoEstado {
    @Id
    private String id;
    @Column(nullable = false)
    private String propostaId;
    private String estadoAnterior;
    @Column(nullable = false)
    private String estadoNovo;
    private String etapa;
    private String usuarioResponsavel;
    private String observacao;
    private String correlationId;
    private LocalDateTime criadoEm;

    public HistoricoEstado() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPropostaId() { return propostaId; }
    public void setPropostaId(String propostaId) { this.propostaId = propostaId; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }
    public String getEstadoNovo() { return estadoNovo; }
    public void setEstadoNovo(String estadoNovo) { this.estadoNovo = estadoNovo; }
    public String getEtapa() { return etapa; }
    public void setEtapa(String etapa) { this.etapa = etapa; }
    public String getUsuarioResponsavel() { return usuarioResponsavel; }
    public void setUsuarioResponsavel(String usuarioResponsavel) { this.usuarioResponsavel = usuarioResponsavel; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
