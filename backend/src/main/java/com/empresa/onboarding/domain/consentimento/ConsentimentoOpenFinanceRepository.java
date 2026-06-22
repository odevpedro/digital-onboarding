package com.empresa.onboarding.domain.consentimento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConsentimentoOpenFinanceRepository extends JpaRepository<ConsentimentoOpenFinance, String> {
    Optional<ConsentimentoOpenFinance> findByPropostaId(String propostaId);
    Optional<ConsentimentoOpenFinance> findByConsentId(String consentId);
}
