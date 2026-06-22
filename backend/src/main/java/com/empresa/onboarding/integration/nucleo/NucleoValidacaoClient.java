package com.empresa.onboarding.integration.nucleo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "nucleo-validacao", url = "${onboarding.nucleo-validacao.url:http://localhost:8080}")
public interface NucleoValidacaoClient {

    @PostMapping("/api/nucleo-validacao/executar")
    ValidacaoResponse executar(@RequestBody ValidacaoRequest request,
                               @RequestHeader("X-Correlation-Id") String correlationId);

    record ValidacaoRequest(Integer idGrupoValidacao, java.util.List<ParametroEntrada> parametros) {}
    record ParametroEntrada(String nome, Object valor) {}
    record ValidacaoResponse(Long idGrupoSolicitacao, Integer idGrupoValidacao, String nomeGrupoValidacao,
                             String estadoGrupo, String resultadoNegocioGrupo, String mensagemGrupoValidacao,
                             String correlationId, java.util.List<ValidacaoResultado> validacoes) {}
    record ValidacaoResultado(Integer idValidacao, String nomeValidacao, String procedureRef, String tipo,
                              String estadoTecnico, String resultadoNegocio, Long tempoMs,
                              java.util.List<String> mensagens, Object payload) {}
}
