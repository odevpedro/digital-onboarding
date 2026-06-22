package com.empresa.onboarding.state;

import com.empresa.onboarding.domain.proposta.HistoricoEstado;
import com.empresa.onboarding.domain.proposta.HistoricoEstadoRepository;
import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class OnboardingStateMachine {

    private static final Map<StatusProposta, Set<StatusProposta>> TRANSICOES = new LinkedHashMap<>();
    private static final Set<StatusProposta> TERMINAIS = Set.of(StatusProposta.FINALIZADO, StatusProposta.CANCELADO);

    static {
        TRANSICOES.put(StatusProposta.RASCUNHO, Set.of(StatusProposta.DADOS_PESSOAIS_ENVIADOS));
        TRANSICOES.put(StatusProposta.DADOS_PESSOAIS_ENVIADOS, Set.of(StatusProposta.DOCUMENTOS_PENDENTES));
        TRANSICOES.put(StatusProposta.DOCUMENTOS_PENDENTES, Set.of(StatusProposta.DOCUMENTOS_ENVIADOS));
        TRANSICOES.put(StatusProposta.DOCUMENTOS_ENVIADOS, Set.of(StatusProposta.DOCUMENTOS_APROVADOS, StatusProposta.DOCUMENTOS_REJEITADOS));
        TRANSICOES.put(StatusProposta.DOCUMENTOS_REJEITADOS, Set.of(StatusProposta.DOCUMENTOS_ENVIADOS));
        TRANSICOES.put(StatusProposta.DOCUMENTOS_APROVADOS, Set.of(StatusProposta.EM_ANALISE_COMPLIANCE));
        TRANSICOES.put(StatusProposta.EM_ANALISE_COMPLIANCE, Set.of(StatusProposta.ANALISE_COMPLIANCE_APROVADA, StatusProposta.ANALISE_COMPLIANCE_REPROVADA));
        TRANSICOES.put(StatusProposta.ANALISE_COMPLIANCE_REPROVADA, Set.of(StatusProposta.EM_ANALISE_COMPLIANCE));
        TRANSICOES.put(StatusProposta.ANALISE_COMPLIANCE_APROVADA, Set.of(StatusProposta.EM_ANALISE_RISCO));
        TRANSICOES.put(StatusProposta.EM_ANALISE_RISCO, Set.of(StatusProposta.ANALISE_RISCO_APROVADA, StatusProposta.ANALISE_RISCO_REPROVADA));
        TRANSICOES.put(StatusProposta.ANALISE_RISCO_REPROVADA, Set.of(StatusProposta.EM_ANALISE_RISCO));
        TRANSICOES.put(StatusProposta.ANALISE_RISCO_APROVADA, Set.of(StatusProposta.AGUARDANDO_CONSENTIMENTO_OF));
        TRANSICOES.put(StatusProposta.AGUARDANDO_CONSENTIMENTO_OF, Set.of(StatusProposta.CONSENTIMENTO_OF_AUTORIZADO, StatusProposta.CONSENTIMENTO_OF_REJEITADO));
        TRANSICOES.put(StatusProposta.CONSENTIMENTO_OF_REJEITADO, Set.of(StatusProposta.AGUARDANDO_CONSENTIMENTO_OF));
        TRANSICOES.put(StatusProposta.CONSENTIMENTO_OF_AUTORIZADO, Set.of(StatusProposta.EM_ANALISE_INTEGRACAO_NUCLEO));
        TRANSICOES.put(StatusProposta.EM_ANALISE_INTEGRACAO_NUCLEO, Set.of(StatusProposta.INTEGRACAO_NUCLEO_APROVADA, StatusProposta.INTEGRACAO_NUCLEO_REPROVADA));
        TRANSICOES.put(StatusProposta.INTEGRACAO_NUCLEO_REPROVADA, Set.of(StatusProposta.EM_ANALISE_INTEGRACAO_NUCLEO));
        TRANSICOES.put(StatusProposta.INTEGRACAO_NUCLEO_APROVADA, Set.of(StatusProposta.AGUARDANDO_CRIACAO_CONTA));
        TRANSICOES.put(StatusProposta.AGUARDANDO_CRIACAO_CONTA, Set.of(StatusProposta.CONTA_CRIADA));
        TRANSICOES.put(StatusProposta.CONTA_CRIADA, Set.of(StatusProposta.CONTA_ATIVADA));
        TRANSICOES.put(StatusProposta.CONTA_ATIVADA, Set.of(StatusProposta.FINALIZADO));
    }

    private final HistoricoEstadoRepository historicoRepository;

    public OnboardingStateMachine(HistoricoEstadoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    public boolean isTransicaoValida(StatusProposta atual, StatusProposta novo) {
        if (atual == null) return false;
        if (novo == StatusProposta.CANCELADO) return true;
        Set<StatusProposta> permitidas = TRANSICOES.get(atual);
        return permitidas != null && permitidas.contains(novo);
    }

    public List<StatusProposta> transicoesPermitidas(StatusProposta atual) {
        if (atual == null) return List.of();
        Set<StatusProposta> permitidas = new LinkedHashSet<>(TRANSICOES.getOrDefault(atual, Set.of()));
        permitidas.add(StatusProposta.CANCELADO);
        return List.copyOf(permitidas);
    }

    @Transactional
    public StatusProposta transitir(PropostaOnboarding proposta, StatusProposta novoStatus, String usuario, String observacao) {
        StatusProposta atual = StatusProposta.valueOf(proposta.getStatus());
        if (TERMINAIS.contains(atual)) {
            throw new IllegalStateException("Proposta em estado terminal: " + atual);
        }
        if (!isTransicaoValida(atual, novoStatus)) {
            throw new IllegalStateException("Transicao invalida de " + atual + " para " + novoStatus);
        }

        String etapaAtual = mapearEtapa(novoStatus);
        String estadoAnterior = proposta.getStatus();

        proposta.setStatus(novoStatus.name());
        proposta.setEtapaAtual(etapaAtual);
        proposta.setAtualizadoEm(LocalDateTime.now());

        HistoricoEstado historico = new HistoricoEstado();
        historico.setId(UUID.randomUUID().toString());
        historico.setPropostaId(proposta.getId());
        historico.setEstadoAnterior(estadoAnterior);
        historico.setEstadoNovo(novoStatus.name());
        historico.setEtapa(etapaAtual);
        historico.setUsuarioResponsavel(usuario);
        historico.setObservacao(observacao);
        historico.setCorrelationId(proposta.getCorrelationId());
        historico.setCriadoEm(LocalDateTime.now());

        historicoRepository.save(historico);
        return novoStatus;
    }

    private String mapearEtapa(StatusProposta status) {
        return switch (status) {
            case RASCUNHO, DADOS_PESSOAIS_ENVIADOS -> EtapaOnboarding.DADOS_PESSOAIS.name();
            case DOCUMENTOS_PENDENTES, DOCUMENTOS_ENVIADOS, DOCUMENTOS_APROVADOS, DOCUMENTOS_REJEITADOS -> EtapaOnboarding.DOCUMENTOS.name();
            case EM_ANALISE_COMPLIANCE, ANALISE_COMPLIANCE_APROVADA, ANALISE_COMPLIANCE_REPROVADA -> EtapaOnboarding.COMPLIANCE.name();
            case EM_ANALISE_RISCO, ANALISE_RISCO_APROVADA, ANALISE_RISCO_REPROVADA -> EtapaOnboarding.ANALISE_RISCO.name();
            case AGUARDANDO_CONSENTIMENTO_OF, CONSENTIMENTO_OF_AUTORIZADO, CONSENTIMENTO_OF_REJEITADO -> EtapaOnboarding.CONSENTIMENTO_OPEN_FINANCE.name();
            case EM_ANALISE_INTEGRACAO_NUCLEO, INTEGRACAO_NUCLEO_APROVADA, INTEGRACAO_NUCLEO_REPROVADA -> EtapaOnboarding.INTEGRACAO_NUCLEO.name();
            case AGUARDANDO_CRIACAO_CONTA, CONTA_CRIADA -> EtapaOnboarding.CRIACAO_CONTA.name();
            case CONTA_ATIVADA -> EtapaOnboarding.ATIVACAO_CONTA.name();
            case FINALIZADO -> EtapaOnboarding.FINALIZADO.name();
            case CANCELADO -> EtapaOnboarding.DADOS_PESSOAIS.name();
        };
    }
}
