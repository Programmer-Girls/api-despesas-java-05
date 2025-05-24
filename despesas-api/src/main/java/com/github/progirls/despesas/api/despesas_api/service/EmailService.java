package com.github.progirls.despesas.api.despesas_api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarOtp(String emailUsuario, String otp) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(emailUsuario);
            helper.setSubject("Código de recuperação de conta");

            String htmlContent = carregarTemplateEmail("email-otp.html", otp);
            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail: " + e.getMessage(), e);
        }
    }

    public String carregarTemplateEmail(String nomeArquivo, String otp) {
        try {
            var resource = new ClassPathResource("templates/" + nomeArquivo);
            String html = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            return html.replace("{{OTP}}", otp);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar template de e-mail", e);
        }
    }
}
