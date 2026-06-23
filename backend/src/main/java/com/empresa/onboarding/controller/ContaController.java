package com.empresa.onboarding.controller;

import com.empresa.onboarding.controller.dto.CriarContaRequest;
import com.empresa.onboarding.domain.conta.ContaCriada;
import com.empresa.onboarding.domain.conta.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Conta", description = "Criacao e ativacao de conta bancaria")
@RestController
@RequestMapping("/api/propostas/{propostaId}/conta")
public class ContaController {
    private static final Logger log = LoggerFactory.getLogger(ContaController.class);
    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @Operation(summary = "Criar conta", description = "Cria uma conta bancaria para a proposta aprovada")
    @PostMapping("/criar")
    public ResponseEntity<ContaCriada> criar(
            @PathVariable String propostaId,
            @Valid @RequestBody CriarContaRequest request) {
        ContaCriada conta = contaService.criarConta(propostaId, request.getTipoConta());
        log.info("Conta criada: propostaId={}, agencia={}, conta={}",
                propostaId, conta.getAgencia(), conta.getNumeroConta());
        return ResponseEntity.ok(conta);
    }

    @Operation(summary = "Ativar conta", description = "Ativa uma conta bancaria recem-criada")
    @PostMapping("/{contaId}/ativar")
    public ResponseEntity<ContaCriada> ativar(@PathVariable String contaId) {
        ContaCriada conta = contaService.ativarConta(contaId);
        log.info("Conta ativada: contaId={}, propostaId={}", contaId, conta.getPropostaId());
        return ResponseEntity.ok(conta);
    }
}
