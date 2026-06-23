package com.empresa.onboarding.controller;

import com.empresa.onboarding.domain.compliance.ComplianceService;
import com.empresa.onboarding.domain.compliance.ValidacaoCompliance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Compliance", description = "Validacoes de compliance e KYC da proposta")
@RestController
@RequestMapping("/api/propostas/{propostaId}/compliance")
public class ComplianceController {
    private static final Logger log = LoggerFactory.getLogger(ComplianceController.class);
    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @Operation(summary = "Listar validacoes", description = "Retorna todas as validacoes de compliance de uma proposta")
    @GetMapping
    public ResponseEntity<List<ValidacaoCompliance>> listar(@PathVariable String propostaId) {
        return ResponseEntity.ok(complianceService.listarValidacoes(propostaId));
    }

    @Operation(summary = "Executar validacoes", description = "Dispara a execucao de todas as validacoes de compliance")
    @PostMapping("/executar")
    public ResponseEntity<Map<String, String>> executar(@PathVariable String propostaId) {
        complianceService.executarValidacoesCompliance(propostaId);
        log.info("Validacoes de compliance executadas: propostaId={}", propostaId);
        return ResponseEntity.ok(Map.of("mensagem", "Validacoes de compliance executadas", "propostaId", propostaId));
    }
}
