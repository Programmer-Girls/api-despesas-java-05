package com.github.progirls.despesas.api.despesas_api.controller;

import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import com.github.progirls.despesas.api.despesas_api.repository.OtpTokenRepository;
import com.github.progirls.despesas.api.despesas_api.repository.UsuarioRepository;
import com.github.progirls.despesas.api.despesas_api.service.OtpService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // OBSERVAÇÃO IMPORTANTE:
    // Este teste está configurado para usar o serviço real de envio de email (Mailtrap).
    // Se você NÃO quiser configurar o Mailtrap, descomente a anotação @MockitoBean abaixo
    // e também os métodos `verify(...)` e `verifyNoInteractions(...)` nos testes.
    // Isso permitirá testar apenas a chamada do serviço, sem enviar emails de verdade.

    // @MockitoBean
    // private OtpService otpService;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String email;

    @BeforeEach
    void setUp() {
        otpTokenRepository.deleteAll();
        usuarioRepository.deleteAll();

        email = "teste" + System.currentTimeMillis() + "@teste.com";

        String senha = "Teste@123";
        Usuario usuario = new Usuario(
                null,
                "Teste OTP",
                email,
                passwordEncoder.encode(senha),
                LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @AfterEach
    void tearDown() {
        otpTokenRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve retornar status 200 OK quando o email é válido")
    void deveRetornarOkQuandoEmailValido() throws Exception {
        mockMvc.perform(post("/api/v1/otp/enviar")
                        .param("email", email))
                .andExpect(status().isOk());

//        verify(otpService, times(1)).gerarEEnviarOtp(email);
    }

    @Test
    @DisplayName("Deve retornar status 400 Bad Request quando o email é inválido")
    void deveRetornarBadRequestQuandoEmailInvalido() throws Exception {
        String emailInvalido = "email-invalido";

        mockMvc.perform(post("/api/v1/otp/enviar")
                        .param("email", emailInvalido))
                .andExpect(status().isBadRequest());

//        verifyNoInteractions(otpService);
    }

    @Test
    @DisplayName("Deve retornar status 400 Bad Request quando o parâmetro email está ausente")
    void deveRetornarBadRequestQuandoEmailAusente() throws Exception {
        mockMvc.perform(post("/api/v1/otp/enviar"))
                .andExpect(status().isBadRequest());

//        verifyNoInteractions(otpService);
    }

}
