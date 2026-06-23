package com.empresa.onboarding.controller;

import com.empresa.onboarding.controller.dto.SolicitarConsentimentoRequest;
import com.empresa.onboarding.domain.consentimento.ConsentimentoOpenFinance;
import com.empresa.onboarding.domain.consentimento.ConsentimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Consentimento", description = "Fluxo de consentimento Open Finance")
@RestController
@RequestMapping("/api/propostas/{propostaId}/consentimento")
public class ConsentimentoController {
    private static final Logger log = LoggerFactory.getLogger(ConsentimentoController.class);
    private final ConsentimentoService consentimentoService;

    public ConsentimentoController(ConsentimentoService consentimentoService) {
        this.consentimentoService = consentimentoService;
    }

    @Operation(summary = "Solicitar consentimento", description = "Solicita consentimento Open Finance com as permissoes informadas")
    @PostMapping("/solicitar")
    public ResponseEntity<ConsentimentoOpenFinance> solicitar(
            @PathVariable String propostaId,
            @Valid @RequestBody SolicitarConsentimentoRequest request) {
        ConsentimentoOpenFinance consentimento = consentimentoService.solicitarConsentimento(propostaId, request.getPermissoes());
        log.info("Consentimento solicitado: propostaId={}, consentId={}", propostaId, consentimento.getConsentId());
        return ResponseEntity.ok(consentimento);
    }

    @Operation(summary = "Autorizar consentimento", description = "Autoriza um consentimento pendente")
    @PostMapping("/{consentId}/autorizar")
    public ResponseEntity<ConsentimentoOpenFinance> autorizar(@PathVariable String consentId) {
        return ResponseEntity.ok(consentimentoService.autorizarConsentimento(consentId));
    }

    @Operation(summary = "Rejeitar consentimento", description = "Rejeita um consentimento pendente")
    @PostMapping("/{consentId}/rejeitar")
    public ResponseEntity<ConsentimentoOpenFinance> rejeitar(@PathVariable String consentId) {
        return ResponseEntity.ok(consentimentoService.rejeitarConsentimento(consentId));
    }
}
