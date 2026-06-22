package com.empresa.onboarding.state;

import com.empresa.onboarding.domain.proposta.HistoricoEstadoRepository;
import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OnboardingStateMachineTest {

    @Mock
    private HistoricoEstadoRepository historicoRepository;

    private OnboardingStateMachine stateMachine;
    private PropostaOnboarding proposta;

    @BeforeEach
    void setUp() {
        stateMachine = new OnboardingStateMachine(historicoRepository);
        proposta = new PropostaOnboarding();
        proposta.setId(UUID.randomUUID().toString());
        proposta.setStatus(StatusProposta.RASCUNHO.name());
        proposta.setCorrelationId(UUID.randomUUID().toString());
    }

    @Test
    void devePermitirTransicaoDeRascunhoParaDadosPessoais() {
        assertTrue(stateMachine.isTransicaoValida(StatusProposta.RASCUNHO, StatusProposta.DADOS_PESSOAIS_ENVIADOS));
    }

    @Test
    void naoDevePermitirTransicaoInvalida() {
        assertFalse(stateMachine.isTransicaoValida(StatusProposta.RASCUNHO, StatusProposta.FINALIZADO));
    }

    @Test
    void devePermitirCancelamentoDeQualquerEstado() {
        for (StatusProposta status : StatusProposta.values()) {
            if (status != StatusProposta.CANCELADO && status != StatusProposta.FINALIZADO) {
                assertTrue(stateMachine.isTransicaoValida(status, StatusProposta.CANCELADO),
                        "Deveria permitir cancelamento de " + status);
            }
        }
    }

    @Test
    void naoDevePermitirTransicaoDeEstadoTerminal() {
        proposta.setStatus(StatusProposta.FINALIZADO.name());
        assertThrows(IllegalStateException.class, () ->
                stateMachine.transitir(proposta, StatusProposta.CANCELADO, "TESTE", "Teste"));
    }

    @Test
    void testFluxoCompleto() {
        StatusProposta[] fluxo = {
                StatusProposta.DADOS_PESSOAIS_ENVIADOS,
                StatusProposta.DOCUMENTOS_PENDENTES,
                StatusProposta.DOCUMENTOS_ENVIADOS,
                StatusProposta.DOCUMENTOS_APROVADOS,
                StatusProposta.EM_ANALISE_COMPLIANCE,
                StatusProposta.ANALISE_COMPLIANCE_APROVADA,
                StatusProposta.EM_ANALISE_RISCO,
                StatusProposta.ANALISE_RISCO_APROVADA,
                StatusProposta.AGUARDANDO_CONSENTIMENTO_OF,
                StatusProposta.CONSENTIMENTO_OF_AUTORIZADO,
                StatusProposta.EM_ANALISE_INTEGRACAO_NUCLEO,
                StatusProposta.INTEGRACAO_NUCLEO_APROVADA,
                StatusProposta.AGUARDANDO_CRIACAO_CONTA,
                StatusProposta.CONTA_CRIADA,
                StatusProposta.CONTA_ATIVADA,
                StatusProposta.FINALIZADO
        };
        for (StatusProposta novo : fluxo) {
            assertTrue(stateMachine.isTransicaoValida(
                    StatusProposta.valueOf(proposta.getStatus()), novo));
            proposta.setStatus(novo.name());
        }
    }

    @Test
    void deveListarTransicoesPermitidas() {
        var transicoes = stateMachine.transicoesPermitidas(StatusProposta.RASCUNHO);
        assertTrue(transicoes.contains(StatusProposta.DADOS_PESSOAIS_ENVIADOS));
        assertTrue(transicoes.contains(StatusProposta.CANCELADO));
        assertEquals(2, transicoes.size());
    }
}
