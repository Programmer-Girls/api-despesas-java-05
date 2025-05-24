package com.github.progirls.despesas.api.despesas_api.service;

import com.github.progirls.despesas.api.despesas_api.entities.OtpToken;
import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import com.github.progirls.despesas.api.despesas_api.repository.OtpTokenRepository;
import com.github.progirls.despesas.api.despesas_api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    private final UsuarioRepository usuarioRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    public OtpService(UsuarioRepository usuarioRepository, OtpTokenRepository otpTokenRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
    }

    public void gerarEEnviarOtp(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String codigo = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiracao = LocalDateTime.now().plusMinutes(10);

        otpTokenRepository.findByUsuario(usuario).ifPresent(otpTokenRepository::delete);

        OtpToken otp = new OtpToken();
        otp.setCodigo(codigo);
        otp.setExpiracao(expiracao);
        otp.setUsuario(usuario);

        otpTokenRepository.save(otp);

        emailService.enviarOtp(emailUsuario, codigo);
    }
}
