package com.github.progirls.despesas.api.despesas_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRedefinirSenhaPorOtpDTO(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String codigo,

        @NotBlank
        @Size(min = 8, max = 16, message = "A senha deve conter entre 8 e 16 caracteres!")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
                message = "A senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um número e um caractere especial")
        String novaSenha
) {
}
