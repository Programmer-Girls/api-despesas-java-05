package com.github.progirls.despesas.api.despesas_api.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.time.LocalDateTime;

import com.github.progirls.despesas.api.despesas_api.dto.UsuarioRedefinirSenhaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.UsuarioRedefinirSenhaPorOtpDTO;
import com.github.progirls.despesas.api.despesas_api.entities.OtpToken;
import com.github.progirls.despesas.api.despesas_api.repository.OtpTokenRepository;
import com.github.progirls.despesas.api.despesas_api.service.OtpService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.progirls.despesas.api.despesas_api.dto.UsuarioRegisterDto;
import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import com.github.progirls.despesas.api.despesas_api.repository.UsuarioRepository;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final String userEmail = "existente@teste.com";
    private final String otpValido = "123456";
    private String token;

    private static final String BASE_URL = "/api/v1/usuarios";

    @BeforeEach
    void setUp() throws Exception {
        otpTokenRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setId(null);
        usuario.setNome("Usuário Existente");
        usuario.setEmail(userEmail);
        String userSenha = "Senha@123.";
        usuario.setSenha(passwordEncoder.encode(userSenha));
        usuario.setDataCriacao(LocalDateTime.now());

        usuarioRepository.save(usuario);

        OtpToken otpToken = new OtpToken();
        otpToken.setCodigo(otpValido);
        otpToken.setExpiracao(LocalDateTime.now().plusMinutes(5));
        otpToken.setUsuario(usuario);
        otpTokenRepository.save(otpToken);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/login")
                        .with(httpBasic(userEmail, userSenha)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        token = objectMapper.readTree(responseJson).get("token").asText();
    }

    @AfterEach
    void tearDown() {
        otpTokenRepository.deleteAll();
        usuarioRepository.deleteAll();
    }
    @Test
    void deveCadastrarUsuarioComSucesso() throws Exception {
        var novoUsuario = new UsuarioRegisterDto(
                "Usuário Novo", "novo@teste.com", "Senha@1234"
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoUsuario)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Usuário cadastrado com sucesso!"));
    }

    @Test
    void deveRetornar400ParaDadosInvalidos() throws Exception {
        var usuarioInvalido = new UsuarioRegisterDto(null, null, null);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar409ParaEmailJaCadastrado() throws Exception {
        var usuarioExistenteDto = new UsuarioRegisterDto(
                "Usuário Existente", "existente@teste.com", "Senha@1234"
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioExistenteDto)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email já cadastrado, por favor tente outro."));
    }

    @DisplayName("Redefinição de senha: deve atualizar a senha do usuário com sucesso")
    @Test
    void deveAtualizarSenhaComSucessoNaRedefinicao() throws Exception {
        UsuarioRedefinirSenhaDTO novaSenhaDto = new UsuarioRedefinirSenhaDTO("NovaSenha@123");

        mockMvc.perform(patch(BASE_URL + "/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaSenhaDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Senha alterada com sucesso!"));

        Usuario atualizado = usuarioRepository.findByEmail(userEmail).orElseThrow();
        assertTrue(passwordEncoder.matches("NovaSenha@123", atualizado.getSenha()));
    }

    @DisplayName("Redefinição de senha: deve retornar 400 Bad Request ao fornecer senha inválida")
    @Test
    void deveRetornar400NaRedefinicaoComSenhaInvalida() throws Exception {
        UsuarioRedefinirSenhaDTO dtoSenhaInvalida = new UsuarioRedefinirSenhaDTO("12");

        mockMvc.perform(patch(BASE_URL + "/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoSenhaInvalida)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Redefinição de senha: deve retornar 401 Unauthorized ao tentar redefinir sem autenticação")
    @Test
    void deveRetornar401NaRedefinicaoSemAutenticacao() throws Exception{
        UsuarioRedefinirSenhaDTO dto = new UsuarioRedefinirSenhaDTO("NovaSenha@123!");

        mockMvc.perform(patch(BASE_URL + "/senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Redefinição de senha: deve retornar 401 Unauthorized ao usar token inválido")
    @Test
    void deveRetornar401NaRedefinicaoDeSenhaComTokenInvalido() throws Exception{
        UsuarioRedefinirSenhaDTO dto = new UsuarioRedefinirSenhaDTO("NovaSenha@123!");

        String tokenInvalido = "tokenInvalido123";
        mockMvc.perform(patch(BASE_URL + "/senha")
                        .header("Authorization", "Bearer " + tokenInvalido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 200 e redefinir a senha com sucesso usando OTP válido")
    void deveRetornar200ParaRedefinirSenhaComOtpValido() throws Exception {
        UsuarioRedefinirSenhaPorOtpDTO dto = new UsuarioRedefinirSenhaPorOtpDTO(userEmail, otpValido, "NovaSenha@123!");

        mockMvc.perform(patch(BASE_URL + "/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Senha redefinida com sucesso."));

        Usuario atualizado = usuarioRepository.findByEmail(userEmail).orElseThrow();
        assertTrue(passwordEncoder.matches("NovaSenha@123!", atualizado.getSenha()));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar redefinir a senha com OTP inválido")
    void deveRetornar400ParaRedefinirSenhaComOtpInvalido() throws Exception {
        UsuarioRedefinirSenhaPorOtpDTO dto = new UsuarioRedefinirSenhaPorOtpDTO(userEmail, "00-00-00", "NovaSenha@123!");

        mockMvc.perform(patch(BASE_URL + "/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar redefinir a senha com email inválido")
    void deveRetornar400ParaRedefinirSenhaComEmailInvalido() throws Exception {
        UsuarioRedefinirSenhaPorOtpDTO dto = new UsuarioRedefinirSenhaPorOtpDTO("email-invalido", otpValido, "NovaSenha@123!");

        mockMvc.perform(patch(BASE_URL + "/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar redefinir a senha com senha inválida")
    void deveRetornar400ParaRedefinirSenhaComSenhaInvalida() throws Exception {
        UsuarioRedefinirSenhaPorOtpDTO dto = new UsuarioRedefinirSenhaPorOtpDTO(userEmail, otpValido, "Senha123");

        mockMvc.perform(patch(BASE_URL + "/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar redefinir a senha com email de usuário inexistente")
    void deveRetornar400ParaRedefinirSenhaComUsuarioInexistente() throws Exception {
        String emailInexistente = "naoexiste@email.com";
        UsuarioRedefinirSenhaPorOtpDTO dto = new UsuarioRedefinirSenhaPorOtpDTO(emailInexistente, otpValido, "NovaSenha@123!");

        mockMvc.perform(patch(BASE_URL + "/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

}
