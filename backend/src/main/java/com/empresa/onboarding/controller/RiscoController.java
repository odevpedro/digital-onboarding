package com.empresa.onboarding.controller;

import com.empresa.onboarding.controller.dto.AnalisarRiscoRequest;
import com.empresa.onboarding.domain.risco.AnaliseRisco;
import com.empresa.onboarding.domain.risco.RiscoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Risco", description = "Analise de risco de credito da proposta")
@RestController
@RequestMapping("/api/propostas/{propostaId}/risco")
public class RiscoController {
    private static final Logger log = LoggerFactory.getLogger(RiscoController.class);
    private final RiscoService riscoService;

    public RiscoController(RiscoService riscoService) {
        this.riscoService = riscoService;
    }

    @Operation(summary = "Buscar analise de risco", description = "Retorna a analise de risco de uma proposta")
    @ApiResponse(responseCode = "200", description = "Analise encontrada")
    @ApiResponse(responseCode = "404", description = "Analise nao encontrada")
    @GetMapping
    public ResponseEntity<AnaliseRisco> buscar(@PathVariable String propostaId) {
        try {
            return ResponseEntity.ok(riscoService.buscarPorProposta(propostaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Analisar risco", description = "Executa a analise de risco com base na renda informada")
    @PostMapping("/analisar")
    public ResponseEntity<AnaliseRisco> analisar(
            @PathVariable String propostaId,
            @Valid @RequestBody AnalisarRiscoRequest request) {
        AnaliseRisco analise = riscoService.executarAnaliseRisco(propostaId, request.getRendaInformada());
        log.info("Analise de risco concluida: propostaId={}, nivel={}", propostaId, analise.getNivelRisco());
        return ResponseEntity.ok(analise);
    }
}
