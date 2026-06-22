package com.empresa.onboarding.service;

import com.empresa.onboarding.domain.consentimento.ConsentimentoOpenFinance;
import com.empresa.onboarding.domain.consentimento.ConsentimentoOpenFinanceRepository;
import com.empresa.onboarding.domain.consentimento.ConsentimentoService;
import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaOnboardingRepository;
import com.empresa.onboarding.integration.bacen.RegrasRegulatoriasFacade;
import com.empresa.onboarding.state.OnboardingStateMachine;
import com.empresa.onboarding.state.StatusProposta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentimentoServiceTest {

    @Mock private ConsentimentoOpenFinanceRepository repository;
    @Mock private PropostaOnboardingRepository propostaRepository;
    @Mock private RegrasRegulatoriasFacade regrasFacade;
    @Mock private OnboardingStateMachine stateMachine;
    @InjectMocks private ConsentimentoService consentimentoService;

    private PropostaOnboarding criarProposta(String status) {
        PropostaOnboarding p = new PropostaOnboarding();
        p.setId(UUID.randomUUID().toString());
        p.setCpfCnpj("52998224725");
        p.setStatus(status);
        p.setCorrelationId(UUID.randomUUID().toString());
        return p;
    }

    @Test
    void deveTransitarParaAguardandoConsentimentoAoSolicitar() {
        PropostaOnboarding proposta = criarProposta(StatusProposta.ANALISE_RISCO_APROVADA.name());
        when(propostaRepository.findById(proposta.getId())).thenReturn(Optional.of(proposta));
        when(regrasFacade.validarDependenciasPermissoesOF(anyList())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(propostaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(stateMachine.transitir(any(), any(), anyString(), anyString()))
                .thenAnswer(inv -> inv.getArgument(1));

        ConsentimentoOpenFinance resultado = consentimentoService.solicitarConsentimento(
                proposta.getId(), List.of("CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ"));

        assertNotNull(resultado);
        assertEquals("AWAITING_AUTHORISATION", resultado.getStatus());
        verify(stateMachine).transitir(proposta, StatusProposta.AGUARDANDO_CONSENTIMENTO_OF,
                "SISTEMA", "Consentimento Open Finance solicitado");
    }

    @Test
    void deveAutorizarConsentimento() {
        PropostaOnboarding proposta = criarProposta(StatusProposta.AGUARDANDO_CONSENTIMENTO_OF.name());
        String consentId = "consent-" + UUID.randomUUID();
        ConsentimentoOpenFinance consentimento = new ConsentimentoOpenFinance();
        consentimento.setConsentId(consentId);
        consentimento.setPropostaId(proposta.getId());
        consentimento.setCpfCnpj(proposta.getCpfCnpj());
        consentimento.setStatus("AWAITING_AUTHORISATION");
        consentimento.setPermissoes("CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ");

        when(repository.findByConsentId(consentId)).thenReturn(Optional.of(consentimento));
        when(propostaRepository.findById(proposta.getId())).thenReturn(Optional.of(proposta));
        when(regrasFacade.validarConsentimentoOF(anyString(), anyString(), anyString(), anyList(), anyList()))
                .thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(propostaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(stateMachine.transitir(any(), any(), anyString(), anyString()))
                .thenAnswer(inv -> inv.getArgument(1));

        ConsentimentoOpenFinance resultado = consentimentoService.autorizarConsentimento(consentId);

        assertEquals("AUTHORISED", resultado.getStatus());
        verify(stateMachine).transitir(proposta, StatusProposta.CONSENTIMENTO_OF_AUTORIZADO,
                "SISTEMA", "Consentimento Open Finance autorizado: " + consentId);
    }
}
