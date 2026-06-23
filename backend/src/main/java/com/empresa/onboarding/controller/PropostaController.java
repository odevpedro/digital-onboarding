package com.empresa.onboarding.controller;

import com.empresa.onboarding.controller.dto.AtualizarDadosPessoaisRequest;
import com.empresa.onboarding.controller.dto.AvancarPropostaRequest;
import com.empresa.onboarding.controller.dto.CancelarPropostaRequest;
import com.empresa.onboarding.controller.dto.CriarPropostaRequest;
import com.empresa.onboarding.domain.proposta.HistoricoEstado;
import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaService;
import com.empresa.onboarding.state.StatusProposta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Propostas", description = "Ciclo de vida da proposta de onboarding")
@RestController
@RequestMapping("/api/propostas")
public class PropostaController {
    private static final Logger log = LoggerFactory.getLogger(PropostaController.class);
    private final PropostaService propostaService;

    public PropostaController(PropostaService propostaService) {
        this.propostaService = propostaService;
    }

    @Operation(summary = "Criar proposta", description = "Inicia uma nova proposta de onboarding PF/PJ")
    @ApiResponse(responseCode = "201", description = "Proposta criada")
    @ApiResponse(responseCode = "400", description = "Dados invalidos")
    @PostMapping
    public ResponseEntity<PropostaOnboarding> criar(
            @Valid @RequestBody CriarPropostaRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        PropostaOnboarding proposta = propostaService.criarProposta(request.getCpfCnpj(), correlationId);
        log.info("Proposta criada: id={}, cpfCnpj={}", proposta.getId(), request.getCpfCnpj());
        return ResponseEntity.status(HttpStatus.CREATED).body(proposta);
    }

    @Operation(summary = "Listar propostas", description = "Retorna todas as propostas cadastradas")
    @GetMapping
    public ResponseEntity<List<PropostaOnboarding>> listar() {
        return ResponseEntity.ok(propostaService.listarTodas());
    }

    @Operation(summary = "Buscar proposta", description = "Retorna uma proposta pelo ID")
    @ApiResponse(responseCode = "200", description = "Proposta encontrada")
    @ApiResponse(responseCode = "404", description = "Proposta nao encontrada")
    @GetMapping("/{id}")
    public ResponseEntity<PropostaOnboarding> buscar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(propostaService.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Atualizar dados pessoais", description = "Atualiza os dados pessoais de uma proposta existente")
    @ApiResponse(responseCode = "200", description = "Dados atualizados")
    @ApiResponse(responseCode = "422", description = "Proposta em estado invalido para atualizacao")
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

    @Operation(summary = "Cancelar proposta", description = "Cancela uma proposta existente")
    @ApiResponse(responseCode = "200", description = "Proposta cancelada")
    @ApiResponse(responseCode = "422", description = "Proposta em estado invalido para cancelamento")
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

    @Operation(summary = "Historico de estados", description = "Retorna o historico completo de transicoes de estado da proposta")
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoEstado>> historico(@PathVariable String id) {
        return ResponseEntity.ok(propostaService.historico(id));
    }

    @Operation(summary = "Transicoes permitidas", description = "Lista as transicoes de estado validas para a proposta atual")
    @GetMapping("/{id}/transicoes-permitidas")
    public ResponseEntity<List<StatusProposta>> transicoesPermitidas(@PathVariable String id) {
        PropostaOnboarding proposta = propostaService.buscarPorId(id);
        StatusProposta atual = StatusProposta.valueOf(proposta.getStatus());
        return ResponseEntity.ok(propostaService.buscarTransicoesPermitidas(atual));
    }

    @Operation(summary = "Avancar proposta", description = "Transita a proposta para um novo estado")
    @ApiResponse(responseCode = "200", description = "Proposta avancada")
    @ApiResponse(responseCode = "422", description = "Transicao invalida")
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
