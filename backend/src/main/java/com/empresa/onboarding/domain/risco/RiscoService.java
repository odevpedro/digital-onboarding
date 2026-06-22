package com.empresa.onboarding.domain.risco;

import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaOnboardingRepository;
import com.empresa.onboarding.integration.simulador.BureauCreditoSimulador;
import com.empresa.onboarding.integration.bacen.RegrasRegulatoriasFacade;
import com.empresa.onboarding.state.OnboardingStateMachine;
import com.empresa.onboarding.state.StatusProposta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class RiscoService {
    private static final Logger log = LoggerFactory.getLogger(RiscoService.class);
    private final AnaliseRiscoRepository repository;
    private final PropostaOnboardingRepository propostaRepository;
    private final BureauCreditoSimulador bureauSimulador;
    private final RegrasRegulatoriasFacade regrasFacade;
    private final OnboardingStateMachine stateMachine;

    public RiscoService(AnaliseRiscoRepository repository,
                        PropostaOnboardingRepository propostaRepository,
                        BureauCreditoSimulador bureauSimulador,
                        RegrasRegulatoriasFacade regrasFacade,
                        OnboardingStateMachine stateMachine) {
        this.repository = repository;
        this.propostaRepository = propostaRepository;
        this.bureauSimulador = bureauSimulador;
        this.regrasFacade = regrasFacade;
        this.stateMachine = stateMachine;
    }

    @Transactional
    public AnaliseRisco executarAnaliseRisco(String propostaId, BigDecimal rendaInformada) {
        PropostaOnboarding proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));

        stateMachine.transitir(proposta, StatusProposta.EM_ANALISE_RISCO, "SISTEMA",
                "Iniciando analise de risco");

        // Consulta bureau de credito
        var scoreBureau = bureauSimulador.consultar(proposta.getCpfCnpj());

        // Avaliacao regulatoria
        Map<String, Object> perfilRegulatorio = regrasFacade.avaliarPerfilRegulatorio(
                proposta.getCpfCnpj(), proposta.getNomeCompleto(), rendaInformada, "BRASIL", null);

        // Calcula score final
        int scoreFinal = (scoreBureau.score() + (Integer) perfilRegulatorio.get("scoreBase")) / 2;
        String nivelRisco = scoreFinal >= 650 ? "BAIXO" : scoreFinal >= 350 ? "MEDIO" : "ALTO";

        AnaliseRisco analise = new AnaliseRisco();
        analise.setId(UUID.randomUUID().toString());
        analise.setPropostaId(propostaId);
        analise.setScore(scoreFinal);
        analise.setNivelRisco(nivelRisco);
        analise.setRendaInformada(rendaInformada);
        analise.setRendaConfirmada(rendaInformada);
        analise.setPossuiRestricao(scoreBureau.score() < 300);
        analise.setPepIdentificado((Boolean) perfilRegulatorio.get("isPep"));
        analise.setAnaliseRealizadaEm(LocalDateTime.now());
        analise.setCorrelationId(proposta.getCorrelationId());

        proposta.setScoreRisco(scoreFinal);
        proposta.setNivelRisco(nivelRisco);
        proposta.setAtualizadoEm(LocalDateTime.now());

        if ("BAIXO".equals(nivelRisco) || "MEDIO".equals(nivelRisco)) {
            stateMachine.transitir(proposta, StatusProposta.ANALISE_RISCO_APROVADA, "SISTEMA",
                    "Score: " + scoreFinal + " | Nivel: " + nivelRisco);
        } else {
            stateMachine.transitir(proposta, StatusProposta.ANALISE_RISCO_REPROVADA, "SISTEMA",
                    "Score: " + scoreFinal + " | Nivel: " + nivelRisco + " | Acima do toleravel");
        }

        propostaRepository.save(proposta);
        repository.save(analise);
        log.info("Analise de risco concluida: propostaId={}, score={}, nivel={}", propostaId, scoreFinal, nivelRisco);
        return analise;
    }

    public AnaliseRisco buscarPorProposta(String propostaId) {
        return repository.findByPropostaId(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Analise de risco nao encontrada para proposta: " + propostaId));
    }
}
