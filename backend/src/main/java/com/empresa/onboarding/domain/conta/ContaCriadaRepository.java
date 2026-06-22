package com.empresa.onboarding.domain.conta;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContaCriadaRepository extends JpaRepository<ContaCriada, String> {
    Optional<ContaCriada> findByPropostaId(String propostaId);
}
