package com.empresa.onboarding.controller;

import com.empresa.onboarding.domain.proposta.PropostaOnboarding;
import com.empresa.onboarding.domain.proposta.PropostaService;
import br.com.odevpedro.foundation.web.idempotency.IdempotencyKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PropostaController.class)
@AutoConfigureMockMvc(addFilters = false)
class PropostaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PropostaService propostaService;

    @MockBean
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    void criarDeveRejeitarBodyVazio() throws Exception {
        mockMvc.perform(post("/api/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarDeveRejeitarCpfInvalido() throws Exception {
        mockMvc.perform(post("/api/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cpfCnpj", "00000000000"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarDeveRejeitarCpfComTodosDigitosIguais() throws Exception {
        mockMvc.perform(post("/api/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cpfCnpj", "11111111111"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarDeveRejeitarCnpjInvalido() throws Exception {
        mockMvc.perform(post("/api/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cpfCnpj", "00000000000000"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarDeveAceitarCpfValido() throws Exception {
        PropostaOnboarding proposta = new PropostaOnboarding();
        proposta.setId("test-id");
        when(propostaService.criarProposta(eq("52998224725"), any()))
                .thenReturn(proposta);

        mockMvc.perform(post("/api/propostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cpfCnpj", "52998224725"))))
                .andExpect(status().isCreated());
    }
}
