package com.empresa.onboarding.domain.documentos;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, String> {
    List<Documento> findByPropostaId(String propostaId);
    List<Documento> findByPropostaIdAndStatus(String propostaId, String status);
}
