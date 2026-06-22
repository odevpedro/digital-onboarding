package com.empresa.onboarding.domain.proposta;

import com.empresa.onboarding.controller.dto.AtualizarDadosPessoaisRequest;
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
public class PropostaService {
    private static final Logger log = LoggerFactory.getLogger(PropostaService.class);
    private final PropostaOnboardingRepository repository;
    private final HistoricoEstadoRepository historicoRepository;
    private final OnboardingStateMachine stateMachine;

    public PropostaService(PropostaOnboardingRepository repository,
                           HistoricoEstadoRepository historicoRepository,
                           OnboardingStateMachine stateMachine) {
        this.repository = repository;
        this.historicoRepository = historicoRepository;
        this.stateMachine = stateMachine;
    }

    @Transactional
    public PropostaOnboarding criarProposta(String cpfCnpj, String correlationId) {
        if (repository.existsByCpfCnpj(cpfCnpj)) {
            throw new IllegalArgumentException("Ja existe uma proposta para o documento: " + cpfCnpj);
        }
        PropostaOnboarding proposta = new PropostaOnboarding();
        proposta.setId(UUID.randomUUID().toString());
        proposta.setCpfCnpj(cpfCnpj);
        proposta.setStatus(StatusProposta.RASCUNHO.name());
        proposta.setEtapaAtual("DADOS_PESSOAIS");
        proposta.setCorrelationId(correlationId);
        proposta.setCriadoEm(LocalDateTime.now());
        proposta.setAtualizadoEm(LocalDateTime.now());
        repository.save(proposta);
        log.info("Proposta criada: id={}, cpfCnpj={}", proposta.getId(), cpfCnpj);
        return proposta;
    }

    @Transactional
    public PropostaOnboarding atualizarDadosPessoais(String propostaId, AtualizarDadosPessoaisRequest dados, String usuario) {
        PropostaOnboarding proposta = repository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));
        stateMachine.transitir(proposta, StatusProposta.DADOS_PESSOAIS_ENVIADOS, usuario, "Dados pessoais enviados");
        proposta.setNomeCompleto(dados.getNomeCompleto());
        proposta.setNomeSocial(dados.getNomeSocial());
        proposta.setGenero(dados.getGenero());
        proposta.setNacionalidade(dados.getNacionalidade());
        proposta.setNomeMae(dados.getNomeMae());
        proposta.setEstadoCivil(dados.getEstadoCivil());
        proposta.setEmail(dados.getEmail());
        proposta.setTelefone(dados.getTelefone());
        proposta.setCep(dados.getCep());
        proposta.setLogradouro(dados.getLogradouro());
        proposta.setNumero(dados.getNumero());
        proposta.setComplemento(dados.getComplemento());
        proposta.setBairro(dados.getBairro());
        proposta.setCidade(dados.getCidade());
        proposta.setEstado(dados.getEstado());
        proposta.setTipoPessoa(dados.getTipoPessoa());
        proposta.setRazaoSocial(dados.getRazaoSocial());
        proposta.setNomeFantasia(dados.getNomeFantasia());
        proposta.setAtualizadoEm(LocalDateTime.now());
        return repository.save(proposta);
    }

    public PropostaOnboarding buscarPorId(String propostaId) {
        return repository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));
    }

    public List<PropostaOnboarding> listarTodas() {
        return repository.findAll();
    }

    public List<HistoricoEstado> historico(String propostaId) {
        return historicoRepository.findByPropostaIdOrderByCriadoEmAsc(propostaId);
    }

    @Transactional
    public PropostaOnboarding cancelar(String propostaId, String motivo, String usuario) {
        PropostaOnboarding proposta = repository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));
        stateMachine.transitir(proposta, StatusProposta.CANCELADO, usuario, motivo);
        proposta.setAtualizadoEm(LocalDateTime.now());
        return repository.save(proposta);
    }

    public List<StatusProposta> buscarTransicoesPermitidas(StatusProposta atual) {
        return stateMachine.transicoesPermitidas(atual);
    }

    @Transactional
    public PropostaOnboarding avancarStatus(String propostaId, StatusProposta novoStatus, String usuario, String observacao) {
        PropostaOnboarding proposta = repository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));
        stateMachine.transitir(proposta, novoStatus, usuario, observacao);
        proposta.setAtualizadoEm(LocalDateTime.now());
        return repository.save(proposta);
    }
}
