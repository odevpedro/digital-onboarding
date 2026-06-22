package com.empresa.onboarding.domain.compliance;

import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaOnboardingRepository;
import com.empresa.onboarding.domain.risco.AnaliseRisco;
import com.empresa.onboarding.domain.risco.AnaliseRiscoRepository;
import com.empresa.onboarding.integration.simulador.*;
import com.empresa.onboarding.integration.bacen.RegrasRegulatoriasFacade;
import com.empresa.onboarding.state.OnboardingStateMachine;
import com.empresa.onboarding.state.StatusProposta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ComplianceService {
    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);
    private final ValidacaoComplianceRepository validacaoRepository;
    private final PropostaOnboardingRepository propostaRepository;
    private final AnaliseRiscoRepository analiseRiscoRepository;
    private final OnboardingStateMachine stateMachine;
    private final ReceitaFederalSimulador receitaSimulador;
    private final ListaRestritivaSimulador listaSimulador;
    private final PepGafiSimulador pepSimulador;
    private final BiometriaFacialSimulador biometriaSimulador;
    private final RegrasRegulatoriasFacade regrasFacade;

    public ComplianceService(ValidacaoComplianceRepository validacaoRepository,
                             PropostaOnboardingRepository propostaRepository,
                             AnaliseRiscoRepository analiseRiscoRepository,
                             OnboardingStateMachine stateMachine,
                             ReceitaFederalSimulador receitaSimulador,
                             ListaRestritivaSimulador listaSimulador,
                             PepGafiSimulador pepSimulador,
                             BiometriaFacialSimulador biometriaSimulador,
                             RegrasRegulatoriasFacade regrasFacade) {
        this.validacaoRepository = validacaoRepository;
        this.propostaRepository = propostaRepository;
        this.analiseRiscoRepository = analiseRiscoRepository;
        this.stateMachine = stateMachine;
        this.receitaSimulador = receitaSimulador;
        this.listaSimulador = listaSimulador;
        this.pepSimulador = pepSimulador;
        this.biometriaSimulador = biometriaSimulador;
        this.regrasFacade = regrasFacade;
    }

    @Transactional
    public void executarValidacoesCompliance(String propostaId) {
        PropostaOnboarding proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new IllegalArgumentException("Proposta nao encontrada: " + propostaId));

        stateMachine.transitir(proposta, StatusProposta.EM_ANALISE_COMPLIANCE, "SISTEMA",
                "Iniciando validacoes de compliance");

        // 1. Consulta Receita Federal
        var situacaoFiscal = receitaSimulador.consultar(proposta.getCpfCnpj());
        salvarValidacao(propostaId, "KYC_RECEITA_FEDERAL",
                "REGULAR".equals(situacaoFiscal.situacao()),
                "Situacao: " + situacaoFiscal.situacao() + ". Tipo: " + situacaoFiscal.tipoPessoa());

        // 2. Validar documento (CPF/CNPJ)
        boolean docValido = regrasFacade.validarCpfCnpj(proposta.getCpfCnpj());
        salvarValidacao(propostaId, "KYC_DOCUMENTO_VALIDO", docValido,
                docValido ? "Documento valido" : "Documento invalido");

        // 3. Consulta listas restritivas
        var listaResultado = listaSimulador.consultar(proposta.getCpfCnpj());
        salvarValidacao(propostaId, "LISTA_RESTRITIVA", !listaResultado.possuiRestricao(),
                listaResultado.possuiRestricao() ? "Restricoes: " + String.join(", ", listaResultado.restricoes()) : "Sem restricoes");

        // 4. Consulta PEP/GAFI
        var pepResultado = pepSimulador.consultar(proposta.getCpfCnpj(), proposta.getNomeCompleto());
        salvarValidacao(propostaId, "PEP_GAFI", !pepResultado.isPep() && !pepResultado.isPaisAltoRisco(),
                pepResultado.isPep() ? "PEP identificado" : "Sem indicacao PEP");

        boolean todasAprovadas = true;
        List<ValidacaoCompliance> validacoes = validacaoRepository.findByPropostaId(propostaId);
        for (var v : validacoes) {
            if (!"APROVADO".equals(v.getStatus())) {
                todasAprovadas = false;
                break;
            }
        }

        proposta.setAtualizadoEm(LocalDateTime.now());

        if (todasAprovadas) {
            stateMachine.transitir(proposta, StatusProposta.ANALISE_COMPLIANCE_APROVADA, "SISTEMA",
                    "Todas as validacoes de compliance aprovadas");
        } else {
            stateMachine.transitir(proposta, StatusProposta.ANALISE_COMPLIANCE_REPROVADA, "SISTEMA",
                    "Uma ou mais validacoes de compliance reprovadas");
        }
        propostaRepository.save(proposta);
    }

    private void salvarValidacao(String propostaId, String tipo, boolean aprovado, String detalhes) {
        ValidacaoCompliance v = new ValidacaoCompliance();
        v.setId(UUID.randomUUID().toString());
        v.setPropostaId(propostaId);
        v.setTipoValidacao(tipo);
        v.setStatus(aprovado ? "APROVADO" : "REPROVADO");
        v.setResultado(aprovado ? "APROVADO" : "REPROVADO");
        v.setDetalhes(detalhes);
        v.setRealizadoEm(LocalDateTime.now());
        v.setCorrelationId(propostaRepository.findById(propostaId).map(p -> p.getCorrelationId()).orElse(null));
        validacaoRepository.save(v);
    }

    public List<ValidacaoCompliance> listarValidacoes(String propostaId) {
        return validacaoRepository.findByPropostaId(propostaId);
    }
}
