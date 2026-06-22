package com.empresa.onboarding.domain.conta;

import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaOnboardingRepository;
import com.empresa.onboarding.integration.nucleo.NucleoValidacaoClient;
import com.empresa.onboarding.integration.nucleo.NucleoValidacaoFacade;
import com.empresa.onboarding.integration.simulador.CoreBancarioSimulador;
import com.empresa.onboarding.integration.simulador.DictPixSimulador;
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
public class ContaService {
    private static final Logger log = LoggerFactory.getLogger(ContaService.class);
    private final ContaCriadaRepository repository;
    private final PropostaOnboardingRepository propostaRepository;
    private final OnboardingStateMachine stateMachine;
    private final CoreBancarioSimulador coreSimulador;
    private final DictPixSimulador dictSimulador;
    private final NucleoValidacaoFacade nucleoFacade;

    public ContaService(ContaCriadaRepository repository,
                        PropostaOnboardingRepository propostaRepository,
                        OnboardingStateMachine stateMachine,
                        CoreBancarioSimulador coreSimulador,
                        DictPixSimulador dictSimulador,
                        NucleoValidacaoFacade nucleoFacade) {
        this.repository = repository;
        this.propostaRepository = propostaRepository;
        this.stateMachine = stateMachine;
        this.coreSimulador = coreSimulador;
        this.dictSimulador = dictSimulador;
        this.nucleoFacade = nucleoFacade;
    }

    @Transactional
    public ContaCriada criarConta(String propostaId, String tipoConta) {
        PropostaOnboarding proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));

        // Avanca para analise de integracao se necessario
        StatusProposta statusAtual = StatusProposta.valueOf(proposta.getStatus());
        if (statusAtual == StatusProposta.CONSENTIMENTO_OF_AUTORIZADO) {
            stateMachine.transitir(proposta, StatusProposta.EM_ANALISE_INTEGRACAO_NUCLEO, "SISTEMA",
                    "Iniciando analise de integracao");
        }

        // Executa validacao final no NucleoValidacao (grupo 100 = ABERTURA_CONTA_CORRENTE)
        var params = List.of(
                new NucleoValidacaoClient.ParametroEntrada("cpf_cnpj", proposta.getCpfCnpj()),
                new NucleoValidacaoClient.ParametroEntrada("canal_origem", "DIGITAL_ONBOARDING")
        );
        var validacaoResultado = nucleoFacade.executarGrupoValidacao(100, proposta.getCorrelationId(), params);

        if (!"APROVADO".equals(validacaoResultado.resultadoNegocioGrupo())) {
            stateMachine.transitir(proposta, StatusProposta.INTEGRACAO_NUCLEO_REPROVADA, "SISTEMA",
                    "Validacao no Nucleo reprovada: " + validacaoResultado.mensagemGrupoValidacao());
            proposta.setAtualizadoEm(LocalDateTime.now());
            propostaRepository.save(proposta);
            throw new IllegalStateException("Validacao no Nucleo reprovada: " + validacaoResultado.mensagemGrupoValidacao());
        }

        stateMachine.transitir(proposta, StatusProposta.INTEGRACAO_NUCLEO_APROVADA, "SISTEMA",
                "Validacao no Nucleo aprovada");
        stateMachine.transitir(proposta, StatusProposta.AGUARDANDO_CRIACAO_CONTA, "SISTEMA",
                "Aguardando criacao de conta");

        // Cria conta no Core Bancario
        var contaCore = coreSimulador.criarConta(proposta.getCpfCnpj(), proposta.getNomeCompleto(), tipoConta);

        // Registra chave Pix (CPF/CNPJ)
        var chavePix = dictSimulador.registrarChave(
                proposta.getCpfCnpj(),
                proposta.getCpfCnpj().length() <= 11 ? "CPF" : "CNPJ",
                "00000000",
                contaCore.agencia(),
                contaCore.numeroConta()
        );

        stateMachine.transitir(proposta, StatusProposta.CONTA_CRIADA, "SISTEMA",
                "Conta criada: " + contaCore.agencia() + "/" + contaCore.numeroConta());

        ContaCriada conta = new ContaCriada();
        conta.setId(UUID.randomUUID().toString());
        conta.setPropostaId(propostaId);
        conta.setAgencia(contaCore.agencia());
        conta.setNumeroConta(contaCore.numeroConta());
        conta.setDigito(contaCore.digito());
        conta.setTipoConta(tipoConta);
        conta.setIspb("00000000");
        conta.setStatus(contaCore.status());
        conta.setChavePix(chavePix.chave());
        conta.setTipoChavePix(chavePix.tipo());
        conta.setCorrelationId(proposta.getCorrelationId());

        proposta.setAtualizadoEm(LocalDateTime.now());
        propostaRepository.save(proposta);
        ContaCriada saved = repository.save(conta);
        log.info("Conta criada: propostaId={}, agencia={}, conta={}", propostaId, contaCore.agencia(), contaCore.numeroConta());
        return saved;
    }

    @Transactional
    public ContaCriada ativarConta(String contaId) {
        ContaCriada conta = repository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta nao encontrada: " + contaId));

        coreSimulador.ativarConta(conta.getAgencia(), conta.getNumeroConta());

        conta.setStatus("ATIVA");
        conta.setAtivadaEm(LocalDateTime.now());

        PropostaOnboarding proposta = propostaRepository.findById(conta.getPropostaId()).orElseThrow();
        stateMachine.transitir(proposta, StatusProposta.CONTA_ATIVADA, "SISTEMA",
                "Conta ativada com sucesso");
        stateMachine.transitir(proposta, StatusProposta.FINALIZADO, "SISTEMA",
                "Onboarding concluido com sucesso");
        proposta.setAtualizadoEm(LocalDateTime.now());
        propostaRepository.save(proposta);

        repository.save(conta);
        log.info("Conta ativada: contaId={}, propostaId={}", contaId, conta.getPropostaId());
        return conta;
    }
}
