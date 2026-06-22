package com.empresa.onboarding.domain.proposta;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoEstadoRepository extends JpaRepository<HistoricoEstado, String> {
    List<HistoricoEstado> findByPropostaIdOrderByCriadoEmAsc(String propostaId);
}
