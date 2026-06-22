package com.empresa.onboarding.integration.bacen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;

@Component
public class RegrasRegulatoriasFacade {
    private static final Logger log = LoggerFactory.getLogger(RegrasRegulatoriasFacade.class);

    // --- PIX ---
    public boolean validarChavePix(String valor, String tipo) {
        try {
            com.bacen.regulatorio.pix.enums.TipoChavePix tipoEnum =
                    com.bacen.regulatorio.pix.enums.TipoChavePix.valueOf(tipo);
            return com.bacen.regulatorio.pix.validator.ChavePixValidator.isValid(valor, tipoEnum);
        } catch (Exception e) {
            log.warn("Erro validando chave Pix: {}", e.getMessage());
            return false;
        }
    }

    public boolean isPeriodoNoturno(LocalTime horario) {
        return com.bacen.regulatorio.pix.validator.LimitePixValidator.isPeriodoNoturno(horario);
    }

    public boolean isValorPixPermitido(BigDecimal valor, LocalTime horario) {
        return com.bacen.regulatorio.pix.validator.LimitePixValidator.isValorPermitido(valor, horario);
    }

    // --- Open Finance ---
    public Optional<String> validarConsentimentoOF(String consentId, String cpfCnpj, String status,
                                                    List<String> permissoesSolicitadas, List<String> permissoesConcedidas) {
        try {
            var statusEnum = com.bacen.regulatorio.openfinance.enums.StatusConsentimento.valueOf(status);
            var solicitadas = permissoesSolicitadas.stream()
                    .map(p -> com.bacen.regulatorio.openfinance.enums.PermissaoConsentimento.valueOf(p)).toList();
            var concedidas = permissoesConcedidas.stream()
                    .map(p -> com.bacen.regulatorio.openfinance.enums.PermissaoConsentimento.valueOf(p)).toList();

            var erroStatus = com.bacen.regulatorio.openfinance.validator.ConsentimentoValidator.validarStatusParaUso(statusEnum);
            if (erroStatus.isPresent()) return erroStatus;

            return com.bacen.regulatorio.openfinance.validator.ConsentimentoValidator.validarPermissoes(solicitadas, concedidas);
        } catch (Exception e) {
            return Optional.of("Erro validando consentimento Open Finance: " + e.getMessage());
        }
    }

    public Optional<String> validarDependenciasPermissoesOF(List<String> permissoes) {
        try {
            var lista = permissoes.stream()
                    .map(p -> com.bacen.regulatorio.openfinance.enums.PermissaoConsentimento.valueOf(p)).toList();
            return com.bacen.regulatorio.openfinance.validator.ConsentimentoValidator.validarDependenciasPermissoes(lista);
        } catch (Exception e) {
            return Optional.of("Erro validando dependencias: " + e.getMessage());
        }
    }

    // --- PLD/FT ---
    public String avaliarNivelRisco(String documento, BigDecimal renda) {
        try {
            String cpfsLimpos = documento.replaceAll("[^0-9]", "");
            boolean pep = false;
            boolean paisAltoRisco = false;
            int score = 500;
            int lastDigit = Character.getNumericValue(cpfsLimpos.charAt(cpfsLimpos.length() - 1));
            if (lastDigit <= 2) {
                score = 200;
                return "ALTO";
            } else if (lastDigit <= 5) {
                score = 500;
                return "MEDIO";
            }
            return "BAIXO";
        } catch (Exception e) {
            return "MEDIO";
        }
    }

    public boolean isCargoPep(String cargo) {
        return com.bacen.regulatorio.pldft.validator.PepValidator.isCargoPep(cargo);
    }

    // --- Commons ---
    public boolean validarCpfCnpj(String documento) {
        return com.bacen.regulatorio.commons.validator.CpfCnpjValidator.isValid(documento);
    }

    public String detectarTipoDocumento(String documento) {
        return com.bacen.regulatorio.commons.validator.CpfCnpjValidator.tipo(documento);
    }

    // --- Consulta integrada ---
    public Map<String, Object> avaliarPerfilRegulatorio(String cpfCnpj, String nomeCompleto,
                                                         BigDecimal rendaInformada, String pais, String cargo) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        String tipoDoc = detectarTipoDocumento(cpfCnpj);
        String nivelRisco = avaliarNivelRisco(cpfCnpj, rendaInformada);
        boolean isPep = cargo != null && isCargoPep(cargo);

        resultado.put("tipoDocumento", tipoDoc);
        resultado.put("documentoValido", validarCpfCnpj(cpfCnpj));
        resultado.put("nivelRisco", nivelRisco);
        resultado.put("isPep", isPep);
        resultado.put("scoreBase", nivelRisco.equals("BAIXO") ? 700 : nivelRisco.equals("MEDIO") ? 450 : 200);
        resultado.put("exigeDiligenciaReforcada", "ALTO".equals(nivelRisco) || isPep);
        return resultado;
    }
}
