package com.github.progirls.despesas.api.despesas_api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record NovaDespesaDTO(
        @NotBlank(message = "A categoria da despesa é obrigatória")
        @Size(max = 55, message = "A descrição deve ter no máximo 55 caracteres")
        String categoria,
        
        @NotNull(message = "O valor da despesa é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        Double valor,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "O número de parcelas é obrigatório")
        @Min(value = 1, message = "O parcelamento deve ser no mínimo 1")
        Integer parcelamento,

        @NotNull(message = "A data de início é obrigatória")
        @PastOrPresent(message = "A data de início não pode estar no futuro")
        LocalDate dataInicio,
        LocalDate dataFim,

        Boolean quitado,
        Boolean ativo

) {
}
