package com.empresa.onboarding.controller;

import com.empresa.onboarding.controller.dto.AtualizarDadosPessoaisRequest;
import com.empresa.onboarding.controller.dto.AvancarPropostaRequest;
import com.empresa.onboarding.controller.dto.CancelarPropostaRequest;
import com.empresa.onboarding.controller.dto.CriarPropostaRequest;
import com.empresa.onboarding.domain.proposta.HistoricoEstado;
import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaService;
import com.empresa.onboarding.state.StatusProposta;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/propostas")
public class PropostaController {
    private static final Logger log = LoggerFactory.getLogger(PropostaController.class);
    private final PropostaService propostaService;

    public PropostaController(PropostaService propostaService) {
        this.propostaService = propostaService;
    }

    @PostMapping
    public ResponseEntity<PropostaOnboarding> criar(
            @Valid @RequestBody CriarPropostaRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        PropostaOnboarding proposta = propostaService.criarProposta(request.getCpfCnpj(), correlationId);
        log.info("Proposta criada: id={}, cpfCnpj={}", proposta.getId(), request.getCpfCnpj());
        return ResponseEntity.status(HttpStatus.CREATED).body(proposta);
    }

    @GetMapping
    public ResponseEntity<List<PropostaOnboarding>> listar() {
        return ResponseEntity.ok(propostaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropostaOnboarding> buscar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(propostaService.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/dados-pessoais")
    public ResponseEntity<PropostaOnboarding> atualizarDadosPessoais(
            @PathVariable String id,
            @Valid @RequestBody AtualizarDadosPessoaisRequest dados,
            @RequestHeader(value = "X-User", defaultValue = "SISTEMA") String usuario) {
        try {
            return ResponseEntity.ok(propostaService.atualizarDadosPessoais(id, dados, usuario));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PropostaOnboarding> cancelar(
            @PathVariable String id,
            @RequestBody CancelarPropostaRequest request,
            @RequestHeader(value = "X-User", defaultValue = "SISTEMA") String usuario) {
        try {
            String motivo = request.getMotivo() != null ? request.getMotivo() : "Cancelado pelo usuario";
            return ResponseEntity.ok(propostaService.cancelar(id, motivo, usuario));
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoEstado>> historico(@PathVariable String id) {
        return ResponseEntity.ok(propostaService.historico(id));
    }

    @GetMapping("/{id}/transicoes-permitidas")
    public ResponseEntity<List<StatusProposta>> transicoesPermitidas(@PathVariable String id) {
        PropostaOnboarding proposta = propostaService.buscarPorId(id);
        StatusProposta atual = StatusProposta.valueOf(proposta.getStatus());
        return ResponseEntity.ok(propostaService.buscarTransicoesPermitidas(atual));
    }

    @PostMapping("/{id}/avancar")
    public ResponseEntity<PropostaOnboarding> avancar(
            @PathVariable String id,
            @Valid @RequestBody AvancarPropostaRequest request,
            @RequestHeader(value = "X-User", defaultValue = "SISTEMA") String usuario) {
        try {
            StatusProposta novoStatus = StatusProposta.valueOf(request.getNovoStatus());
            String observacao = request.getObservacao() != null ? request.getObservacao() : "Avanco manual";
            return ResponseEntity.ok(propostaService.avancarStatus(id, novoStatus, usuario, observacao));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().build();
        }
    }
}
