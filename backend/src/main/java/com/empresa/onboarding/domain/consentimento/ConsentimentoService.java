package com.empresa.onboarding.domain.consentimento;

import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaOnboardingRepository;
import com.empresa.onboarding.integration.bacen.RegrasRegulatoriasFacade;
import com.empresa.onboarding.state.OnboardingStateMachine;
import com.empresa.onboarding.state.StatusProposta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConsentimentoService {
    private static final Logger log = LoggerFactory.getLogger(ConsentimentoService.class);
    private final ConsentimentoOpenFinanceRepository repository;
    private final PropostaOnboardingRepository propostaRepository;
    private final RegrasRegulatoriasFacade regrasFacade;
    private final OnboardingStateMachine stateMachine;

    public ConsentimentoService(ConsentimentoOpenFinanceRepository repository,
                                PropostaOnboardingRepository propostaRepository,
                                RegrasRegulatoriasFacade regrasFacade,
                                OnboardingStateMachine stateMachine) {
        this.repository = repository;
        this.propostaRepository = propostaRepository;
        this.regrasFacade = regrasFacade;
        this.stateMachine = stateMachine;
    }

    @Transactional
    public ConsentimentoOpenFinance solicitarConsentimento(String propostaId, List<String> permissoes) {
        PropostaOnboarding proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));

        // Valida dependencias de permissoes via bacen-regulatorio
        Optional<String> erroDependencias = regrasFacade.validarDependenciasPermissoesOF(permissoes);
        if (erroDependencias.isPresent()) {
            throw new IllegalArgumentException("Permissoes invalidas: " + erroDependencias.get());
        }

        ConsentimentoOpenFinance consentimento = new ConsentimentoOpenFinance();
        consentimento.setId(UUID.randomUUID().toString());
        consentimento.setPropostaId(propostaId);
        consentimento.setConsentId("consent-" + UUID.randomUUID());
        consentimento.setCpfCnpj(proposta.getCpfCnpj());
        consentimento.setStatus("AWAITING_AUTHORISATION");
        consentimento.setPermissoes(String.join(",", permissoes));
        consentimento.setDataCriacao(OffsetDateTime.now());
        consentimento.setDataExpiracao(OffsetDateTime.now().plusMonths(12));
        consentimento.setCorrelationId(proposta.getCorrelationId());

        repository.save(consentimento);

        stateMachine.transitir(proposta, StatusProposta.AGUARDANDO_CONSENTIMENTO_OF, "SISTEMA",
                "Consentimento Open Finance solicitado");
        proposta.setAtualizadoEm(OffsetDateTime.now().toLocalDateTime());
        propostaRepository.save(proposta);

        log.info("Consentimento Open Finance solicitado: propostaId={}", propostaId);
        return consentimento;
    }

    @Transactional
    public ConsentimentoOpenFinance autorizarConsentimento(String consentId) {
        ConsentimentoOpenFinance consentimento = repository.findByConsentId(consentId)
                .orElseThrow(() -> new IllegalArgumentException("Consentimento nao encontrado: " + consentId));

        String propostaId = consentimento.getPropostaId();
        PropostaOnboarding proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));

        // Valida via bacen-regulatorio se o consentimento pode ser usado
        List<String> permissoesList = List.of(consentimento.getPermissoes().split(","));
        Optional<String> erroUso = regrasFacade.validarConsentimentoOF(
                consentId, consentimento.getCpfCnpj(), "AUTHORISED",
                permissoesList, permissoesList);

        if (erroUso.isPresent()) {
            throw new IllegalStateException("Consentimento invalido: " + erroUso.get());
        }

        consentimento.setStatus("AUTHORISED");
        consentimento.setDataAutorizacao(OffsetDateTime.now());

        stateMachine.transitir(proposta, StatusProposta.CONSENTIMENTO_OF_AUTORIZADO, "SISTEMA",
                "Consentimento Open Finance autorizado: " + consentId);
        proposta.setAtualizadoEm(OffsetDateTime.now().toLocalDateTime());
        propostaRepository.save(proposta);

        repository.save(consentimento);
        log.info("Consentimento autorizado: consentId={}", consentId);
        return consentimento;
    }

    @Transactional
    public ConsentimentoOpenFinance rejeitarConsentimento(String consentId) {
        ConsentimentoOpenFinance consentimento = repository.findByConsentId(consentId)
                .orElseThrow(() -> new IllegalArgumentException("Consentimento nao encontrado: " + consentId));

        consentimento.setStatus("REJECTED");

        PropostaOnboarding proposta = propostaRepository.findById(consentimento.getPropostaId()).orElseThrow();
        stateMachine.transitir(proposta, StatusProposta.CONSENTIMENTO_OF_REJEITADO, "SISTEMA",
                "Consentimento Open Finance rejeitado");
        proposta.setAtualizadoEm(OffsetDateTime.now().toLocalDateTime());
        propostaRepository.save(proposta);

        repository.save(consentimento);
        return consentimento;
    }
}
