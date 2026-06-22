package com.empresa.onboarding.domain.risco;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnaliseRiscoRepository extends JpaRepository<AnaliseRisco, String> {
    Optional<AnaliseRisco> findByPropostaId(String propostaId);
}
