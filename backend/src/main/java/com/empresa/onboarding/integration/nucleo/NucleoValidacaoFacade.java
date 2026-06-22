package com.empresa.onboarding.integration.nucleo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NucleoValidacaoFacade {
    private static final Logger log = LoggerFactory.getLogger(NucleoValidacaoFacade.class);
    private final NucleoValidacaoClient client;

    public NucleoValidacaoFacade(NucleoValidacaoClient client) {
        this.client = client;
    }

    public NucleoValidacaoClient.ValidacaoResponse executarGrupoValidacao(
            Integer idGrupo, String correlationId, List<NucleoValidacaoClient.ParametroEntrada> parametros) {
        log.info("Executando grupo de validacao {} via NucleoValidacao", idGrupo);
        var request = new NucleoValidacaoClient.ValidacaoRequest(idGrupo, parametros);
        try {
            var response = client.executar(request, correlationId);
            log.info("Grupo {} executado: resultado={}", idGrupo, response.resultadoNegocioGrupo());
            return response;
        } catch (Exception e) {
            log.error("Falha ao executar grupo {} no NucleoValidacao", idGrupo, e);
            throw new RuntimeException("Erro na comunicacao com NucleoValidacao: " + e.getMessage(), e);
        }
    }
}
