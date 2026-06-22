package com.empresa.onboarding.domain.documentos;

import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaOnboardingRepository;
import com.empresa.onboarding.integration.simulador.SerproDocumentSimulador;
import com.empresa.onboarding.integration.storage.MinioStorageService;
import com.empresa.onboarding.state.OnboardingStateMachine;
import com.empresa.onboarding.state.StatusProposta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {
    private static final Logger log = LoggerFactory.getLogger(DocumentoService.class);
    private final DocumentoRepository repository;
    private final PropostaOnboardingRepository propostaRepository;
    private final MinioStorageService storageService;
    private final SerproDocumentSimulador serproSimulador;
    private final OnboardingStateMachine stateMachine;

    public DocumentoService(DocumentoRepository repository,
                            PropostaOnboardingRepository propostaRepository,
                            MinioStorageService storageService,
                            SerproDocumentSimulador serproSimulador,
                            OnboardingStateMachine stateMachine) {
        this.repository = repository;
        this.propostaRepository = propostaRepository;
        this.storageService = storageService;
        this.serproSimulador = serproSimulador;
        this.stateMachine = stateMachine;
    }

    @Transactional
    public Documento enviarDocumento(String propostaId, String tipo, String nomeArquivo, String mimeType, byte[] conteudo) {
        PropostaOnboarding proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));

        String objectKey = storageService.upload(nomeArquivo, mimeType, conteudo);

        Documento doc = new Documento();
        doc.setId(UUID.randomUUID().toString());
        doc.setPropostaId(propostaId);
        doc.setTipo(tipo);
        doc.setNomeArquivo(nomeArquivo);
        doc.setMimeType(mimeType);
        doc.setTamanhoBytes((long) conteudo.length);
        doc.setMinioObjectKey(objectKey);
        doc.setStatus("PENDENTE");
        doc.setEnviadoEm(LocalDateTime.now());
        doc.setCorrelationId(proposta.getCorrelationId());

        String statusAtual = proposta.getStatus();
        if (StatusProposta.RASCUNHO.name().equals(statusAtual)
                || StatusProposta.DADOS_PESSOAIS_ENVIADOS.name().equals(statusAtual)) {
            stateMachine.transitir(proposta, StatusProposta.DOCUMENTOS_PENDENTES, "SISTEMA",
                    "Primeiro documento enviado");
        }

        if (StatusProposta.DOCUMENTOS_REJEITADOS.name().equals(statusAtual)) {
            stateMachine.transitir(proposta, StatusProposta.DOCUMENTOS_ENVIADOS, "SISTEMA",
                    "Reenvio de documento: " + tipo);
        }

        proposta.setAtualizadoEm(LocalDateTime.now());
        propostaRepository.save(proposta);
        Documento saved = repository.save(doc);
        log.info("Documento enviado: propostaId={}, tipo={}, arquivo={}", propostaId, tipo, nomeArquivo);
        return saved;
    }

    @Transactional
    public Documento aprovarDocumento(String documentoId) {
        Documento doc = repository.findById(documentoId)
                .orElseThrow(() -> new IllegalArgumentException("Documento nao encontrado: " + documentoId));
        doc.setStatus("APROVADO");
        doc.setAprovadoEm(LocalDateTime.now());

        List<Documento> docs = repository.findByPropostaId(doc.getPropostaId());
        boolean todosAprovados = docs.stream().allMatch(d -> "APROVADO".equals(d.getStatus()));

        if (todosAprovados) {
            PropostaOnboarding proposta = propostaRepository.findById(doc.getPropostaId()).orElseThrow();
            StatusProposta statusAtual = StatusProposta.valueOf(proposta.getStatus());
            if (statusAtual == StatusProposta.DOCUMENTOS_PENDENTES) {
                stateMachine.transitir(proposta, StatusProposta.DOCUMENTOS_ENVIADOS, "SISTEMA",
                        "Todos os documentos enviados");
            }
            stateMachine.transitir(proposta, StatusProposta.DOCUMENTOS_APROVADOS, "SISTEMA",
                    "Todos os documentos aprovados");
            proposta.setAtualizadoEm(LocalDateTime.now());
            propostaRepository.save(proposta);
        }

        return repository.save(doc);
    }

    @Transactional
    public Documento rejeitarDocumento(String documentoId, String motivo) {
        Documento doc = repository.findById(documentoId)
                .orElseThrow(() -> new IllegalArgumentException("Documento nao encontrado: " + documentoId));
        doc.setStatus("REJEITADO");
        doc.setMotivoRejeicao(motivo);

        PropostaOnboarding proposta = propostaRepository.findById(doc.getPropostaId()).orElseThrow();
        StatusProposta statusAtual = StatusProposta.valueOf(proposta.getStatus());
        if (statusAtual == StatusProposta.DOCUMENTOS_PENDENTES) {
            stateMachine.transitir(proposta, StatusProposta.DOCUMENTOS_ENVIADOS, "SISTEMA",
                    "Todos os documentos enviados");
        }
        stateMachine.transitir(proposta, StatusProposta.DOCUMENTOS_REJEITADOS, "SISTEMA",
                "Documento rejeitado: " + motivo);
        proposta.setAtualizadoEm(LocalDateTime.now());
        propostaRepository.save(proposta);

        return repository.save(doc);
    }

    public List<Documento> listarPorProposta(String propostaId) {
        return repository.findByPropostaId(propostaId);
    }
}
