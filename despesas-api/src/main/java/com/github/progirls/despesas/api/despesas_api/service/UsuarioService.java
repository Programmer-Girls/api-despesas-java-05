package com.github.progirls.despesas.api.despesas_api.service;

import java.time.LocalDateTime;
import java.util.List;

import com.github.progirls.despesas.api.despesas_api.entities.OtpToken;
import com.github.progirls.despesas.api.despesas_api.repository.OtpTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.progirls.despesas.api.despesas_api.dto.UsuarioRedefinirSenhaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.UsuarioRegisterDto;
import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import com.github.progirls.despesas.api.despesas_api.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final OtpTokenRepository otpTokenRepository;

    public UsuarioService(BCryptPasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, OtpTokenRepository otpTokenRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.otpTokenRepository = otpTokenRepository;
    }
    
    public Usuario criarUsuario(UsuarioRegisterDto usuarioRegisterDto) {
        
        var usuarioEmail = usuarioRepository.findByEmail(usuarioRegisterDto.email());
        if (usuarioEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado, por favor tente outro.");
        }

        String encryptedPassword = passwordEncoder.encode(usuarioRegisterDto.senha());

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioRegisterDto.nome());
        usuario.setSenha(encryptedPassword);
        usuario.setEmail(usuarioRegisterDto.email());
        usuario.setDataCriacao(LocalDateTime.now());

        usuario = usuarioRepository.save(usuario);

        return usuario;

    }

    @Transactional
    public void atualizarSenha(UsuarioRedefinirSenhaDTO novaSenha, Authentication authentication) {

        String emailUsuario = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String novaSenhaCriptografada = passwordEncoder.encode(novaSenha.senha());

        usuario.setSenha(novaSenhaCriptografada);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void redefinirSenhaComOtp(String email, String codigo, String novaSenha) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        OtpToken otp = otpTokenRepository.findByUsuario(usuario).filter(t -> t.getCodigo().equals(codigo) && LocalDateTime.now().isBefore(t.getExpiracao())).orElseThrow(() -> new IllegalArgumentException("Código OTP inválido."));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        otpTokenRepository.delete(otp);
    }
}
