package com.github.progirls.despesas.api.despesas_api.controller;

import com.github.progirls.despesas.api.despesas_api.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @Operation(
            summary = "Gerar e enviar OTP por email",
            description = "Gera um código OTP para o email informado e envia o código via email. " +
                    "Usado para autenticação ou redefinição de senha.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP gerado e enviado com sucesso."),
                    @ApiResponse(responseCode = "400", description = "Email inválido ou ausência do parâmetro.")
            }
    )
    @PostMapping("/enviar")
    public ResponseEntity<Void> enviar(@RequestParam @Email String email) {
        otpService.gerarEEnviarOtp(email);
        return ResponseEntity.ok().build();
    }

}
