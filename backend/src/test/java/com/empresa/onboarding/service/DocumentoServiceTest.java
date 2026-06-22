package com.empresa.onboarding.service;

import com.empresa.onboarding.domain.documentos.Documento;
import com.empresa.onboarding.domain.documentos.DocumentoRepository;
import com.empresa.onboarding.domain.documentos.DocumentoService;
import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaOnboardingRepository;
import com.empresa.onboarding.integration.simulador.SerproDocumentSimulador;
import com.empresa.onboarding.integration.storage.MinioStorageService;
import com.empresa.onboarding.state.OnboardingStateMachine;
import com.empresa.onboarding.state.StatusProposta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class DocumentoServiceTest {

    @Mock private DocumentoRepository repository;
    @Mock private PropostaOnboardingRepository propostaRepository;
    @Mock private MinioStorageService storageService;
    @Mock private SerproDocumentSimulador serproSimulador;
    @Mock private OnboardingStateMachine stateMachine;
    @InjectMocks private DocumentoService documentoService;
    @Captor private ArgumentCaptor<PropostaOnboarding> propostaCaptor;
    @Captor private ArgumentCaptor<Documento> documentCaptor;

    private PropostaOnboarding criarProposta(String status) {
        PropostaOnboarding p = new PropostaOnboarding();
        p.setId(UUID.randomUUID().toString());
        p.setCpfCnpj("52998224725");
        p.setStatus(status);
        p.setCorrelationId(UUID.randomUUID().toString());
        return p;
    }

    private Documento criarDocumento(String id, String status, String propostaId) {
        Documento d = new Documento();
        d.setId(id);
        d.setPropostaId(propostaId);
        d.setTipo("RG");
        d.setStatus(status);
        return d;
    }

    @Test
    void deveTransitarViaDocumentosEnviadosAoAprovarDePendentes() {
        String propostaId = UUID.randomUUID().toString();
        String docId = UUID.randomUUID().toString();
        PropostaOnboarding proposta = criarProposta(StatusProposta.DOCUMENTOS_PENDENTES.name());
        proposta.setId(propostaId);
        Documento doc = criarDocumento(docId, "PENDENTE", propostaId);

        when(repository.findById(docId)).thenReturn(Optional.of(doc));
        // Use same doc reference so setStatus("APROVADO") reflects in the list
        when(repository.findByPropostaId(propostaId)).thenReturn(List.of(
                criarDocumento("d1", "APROVADO", propostaId),
                doc
        ));
        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(propostaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(stateMachine.transitir(any(), any(), anyString(), anyString()))
                .thenAnswer(inv -> {
                    PropostaOnboarding p = inv.getArgument(0);
                    StatusProposta s = inv.getArgument(1);
                    p.setStatus(s.name());
                    return s;
                });

        Documento resultado = documentoService.aprovarDocumento(docId);

        assertEquals("APROVADO", resultado.getStatus());
        verify(stateMachine).transitir(proposta, StatusProposta.DOCUMENTOS_ENVIADOS, "SISTEMA", "Todos os documentos enviados");
        verify(stateMachine).transitir(proposta, StatusProposta.DOCUMENTOS_APROVADOS, "SISTEMA", "Todos os documentos aprovados");
    }

    @Test
    void deveTransitarViaDocumentosEnviadosAoRejeitarDePendentes() {
        String propostaId = UUID.randomUUID().toString();
        String docId = UUID.randomUUID().toString();
        PropostaOnboarding proposta = criarProposta(StatusProposta.DOCUMENTOS_PENDENTES.name());
        proposta.setId(propostaId);
        Documento doc = criarDocumento(docId, "PENDENTE", propostaId);

        when(repository.findById(docId)).thenReturn(Optional.of(doc));
        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(propostaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(stateMachine.transitir(any(), any(), anyString(), anyString()))
                .thenAnswer(inv -> {
                    PropostaOnboarding p = inv.getArgument(0);
                    StatusProposta s = inv.getArgument(1);
                    p.setStatus(s.name());
                    return s;
                });

        Documento resultado = documentoService.rejeitarDocumento(docId, "Documento ilegivel");

        assertEquals("REJEITADO", resultado.getStatus());
        verify(stateMachine).transitir(proposta, StatusProposta.DOCUMENTOS_ENVIADOS, "SISTEMA", "Todos os documentos enviados");
        verify(stateMachine).transitir(proposta, StatusProposta.DOCUMENTOS_REJEITADOS, "SISTEMA", "Documento rejeitado: Documento ilegivel");
    }

    @Test
    void deveAprovarDiretamenteQuandoJaEmDocumentosEnviados() {
        String propostaId = UUID.randomUUID().toString();
        String docId = UUID.randomUUID().toString();
        PropostaOnboarding proposta = criarProposta(StatusProposta.DOCUMENTOS_ENVIADOS.name());
        proposta.setId(propostaId);
        Documento doc = criarDocumento(docId, "PENDENTE", propostaId);

        when(repository.findById(docId)).thenReturn(Optional.of(doc));
        // Only this doc in the list (same reference) — will be APROVADO after setStatus
        when(repository.findByPropostaId(propostaId)).thenReturn(List.of(doc));
        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(propostaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(stateMachine.transitir(any(), any(), anyString(), anyString()))
                .thenAnswer(inv -> {
                    PropostaOnboarding p = inv.getArgument(0);
                    StatusProposta s = inv.getArgument(1);
                    p.setStatus(s.name());
                    return s;
                });

        documentoService.aprovarDocumento(docId);

        verify(stateMachine, never()).transitir(any(), eq(StatusProposta.DOCUMENTOS_ENVIADOS), anyString(), anyString());
        verify(stateMachine).transitir(proposta, StatusProposta.DOCUMENTOS_APROVADOS, "SISTEMA", "Todos os documentos aprovados");
    }
}
