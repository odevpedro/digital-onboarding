package com.empresa.onboarding.domain.compliance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ValidacaoComplianceRepository extends JpaRepository<ValidacaoCompliance, String> {
    List<ValidacaoCompliance> findByPropostaId(String propostaId);
    Optional<ValidacaoCompliance> findByPropostaIdAndTipoValidacao(String propostaId, String tipoValidacao);
}
