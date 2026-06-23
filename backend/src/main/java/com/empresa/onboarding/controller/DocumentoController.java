package com.empresa.onboarding.controller;

import com.empresa.onboarding.controller.dto.RejeitarDocumentoRequest;
import com.empresa.onboarding.domain.documentos.Documento;
import com.empresa.onboarding.domain.documentos.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Documentos", description = "Upload e aprovacao de documentos da proposta")
@RestController
@RequestMapping("/api/propostas/{propostaId}/documentos")
public class DocumentoController {
    private static final Logger log = LoggerFactory.getLogger(DocumentoController.class);
    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @Operation(summary = "Listar documentos", description = "Retorna todos os documentos de uma proposta")
    @GetMapping
    public ResponseEntity<List<Documento>> listar(@PathVariable String propostaId) {
        return ResponseEntity.ok(documentoService.listarPorProposta(propostaId));
    }

    @Operation(summary = "Upload de documento", description = "Faz upload de um documento para a proposta")
    @ApiResponse(responseCode = "200", description = "Documento enviado")
    @ApiResponse(responseCode = "400", description = "Arquivo invalido ou vazio")
    @PostMapping
    public ResponseEntity<Documento> upload(
            @PathVariable String propostaId,
            @RequestParam("tipo") String tipo,
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Documento doc = documentoService.enviarDocumento(
                propostaId, tipo, arquivo.getOriginalFilename(),
                arquivo.getContentType(), arquivo.getBytes());
        log.info("Documento enviado: propostaId={}, tipo={}", propostaId, tipo);
        return ResponseEntity.ok(doc);
    }

    @Operation(summary = "Aprovar documento", description = "Aprova um documento pendente")
    @PostMapping("/{documentoId}/aprovar")
    public ResponseEntity<Documento> aprovar(@PathVariable String documentoId) {
        return ResponseEntity.ok(documentoService.aprovarDocumento(documentoId));
    }

    @Operation(summary = "Rejeitar documento", description = "Rejeita um documento com motivo")
    @PostMapping("/{documentoId}/rejeitar")
    public ResponseEntity<Documento> rejeitar(
            @PathVariable String documentoId,
            @Valid @RequestBody RejeitarDocumentoRequest request) {
        return ResponseEntity.ok(documentoService.rejeitarDocumento(
                documentoId, request.getMotivo()));
    }
}
