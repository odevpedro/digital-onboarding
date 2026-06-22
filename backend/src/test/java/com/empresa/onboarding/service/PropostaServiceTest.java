package com.empresa.onboarding.service;

import com.empresa.onboarding.domain.proposta.HistoricoEstadoRepository;
import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaOnboardingRepository;
import com.empresa.onboarding.domain.proposta.PropostaService;
import com.empresa.onboarding.state.OnboardingStateMachine;
import com.empresa.onboarding.state.StatusProposta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

    private static void mockTransicao(OnboardingStateMachine stateMachine) {
        doAnswer(inv -> {
            PropostaOnboarding proposta = inv.getArgument(0);
            StatusProposta novoStatus = inv.getArgument(1);
            proposta.setStatus(novoStatus.name());
            return novoStatus;
        }).when(stateMachine).transitir(any(), any(), anyString(), anyString());
    }

    @Mock
    private PropostaOnboardingRepository repository;
    @Mock
    private HistoricoEstadoRepository historicoRepository;
    @Mock
    private OnboardingStateMachine stateMachine;
    @InjectMocks
    private PropostaService propostaService;
    @Captor
    private ArgumentCaptor<PropostaOnboarding> propostaCaptor;

    @Test
    void deveCriarProposta() {
        when(repository.existsByCpfCnpj(anyString())).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        PropostaOnboarding proposta = propostaService.criarProposta("12345678901", UUID.randomUUID().toString());

        assertNotNull(proposta.getId());
        assertEquals("12345678901", proposta.getCpfCnpj());
        assertEquals(StatusProposta.RASCUNHO.name(), proposta.getStatus());
        verify(repository).save(any(PropostaOnboarding.class));
    }

    @Test
    void naoDeveCriarPropostaDuplicada() {
        when(repository.existsByCpfCnpj(anyString())).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> propostaService.criarProposta("12345678901", null));
    }

    @Test
    void deveCancelarProposta() {
        mockTransicao(stateMachine);
        String id = UUID.randomUUID().toString();
        PropostaOnboarding proposta = new PropostaOnboarding();
        proposta.setId(id);
        proposta.setStatus(StatusProposta.RASCUNHO.name());
        when(repository.findById(id)).thenReturn(Optional.of(proposta));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        propostaService.cancelar(id, "Teste", "TESTE");
        verify(stateMachine).transitir(eq(proposta), eq(StatusProposta.CANCELADO), anyString(), anyString());
        verify(repository).save(propostaCaptor.capture());
        assertEquals(StatusProposta.CANCELADO.name(), propostaCaptor.getValue().getStatus());
    }
}
