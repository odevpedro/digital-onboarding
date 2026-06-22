package com.empresa.onboarding.controller;

import com.empresa.onboarding.controller.dto.AnalisarRiscoRequest;
import com.empresa.onboarding.domain.risco.AnaliseRisco;
import com.empresa.onboarding.domain.risco.RiscoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/propostas/{propostaId}/risco")
public class RiscoController {
    private static final Logger log = LoggerFactory.getLogger(RiscoController.class);
    private final RiscoService riscoService;

    public RiscoController(RiscoService riscoService) {
        this.riscoService = riscoService;
    }

    @GetMapping
    public ResponseEntity<AnaliseRisco> buscar(@PathVariable String propostaId) {
        try {
            return ResponseEntity.ok(riscoService.buscarPorProposta(propostaId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/analisar")
    public ResponseEntity<AnaliseRisco> analisar(
            @PathVariable String propostaId,
            @Valid @RequestBody AnalisarRiscoRequest request) {
        AnaliseRisco analise = riscoService.executarAnaliseRisco(propostaId, request.getRendaInformada());
        log.info("Analise de risco concluida: propostaId={}, nivel={}", propostaId, analise.getNivelRisco());
        return ResponseEntity.ok(analise);
    }
}
