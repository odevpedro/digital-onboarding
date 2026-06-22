package com.empresa.onboarding.domain.proposta;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PropostaOnboardingRepository extends JpaRepository<PropostaOnboarding, String> {
    Optional<PropostaOnboarding> findByCpfCnpj(String cpfCnpj);
    boolean existsByCpfCnpj(String cpfCnpj);
}
