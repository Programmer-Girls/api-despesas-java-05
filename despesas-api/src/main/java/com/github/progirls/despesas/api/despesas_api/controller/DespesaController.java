package com.github.progirls.despesas.api.despesas_api.controller;

import com.github.progirls.despesas.api.despesas_api.dto.DespesaFiltradaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.ListaDespesasResponseDTO;
import com.github.progirls.despesas.api.despesas_api.dto.AtualizarDespesaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.DespesaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.NovaDespesaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.PageResponseDTO;
import com.github.progirls.despesas.api.despesas_api.service.DespesaService;

import io.micrometer.core.ipc.http.HttpSender.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;

import java.net.URI;
import java.nio.file.AccessDeniedException;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/despesas")
public class DespesaController {

    private final DespesaService despesaService;

    public DespesaController(DespesaService despesaService) {
        this.despesaService = despesaService;
    }

    @Operation(summary = "Criar uma nova despesa ao usuário autenticado",

            description = "Este endpoint permite que o usuário crie uma despesa nova. " +
                    "O usuário precisa estar autenticado para realizar a criação. ", responses = {
            @ApiResponse(responseCode = "201", description = "Despesa criada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Despesa com dados incompletos ou inválidos."),
            @ApiResponse(responseCode = "401", description = "Falha na autenticação. Token inválido ou ausente.")})
    @PostMapping
    public ResponseEntity<?> criarDespesa(@Valid @RequestBody NovaDespesaDTO dto, Authentication authentication) {
        try {
            var novaDespesa = despesaService.criarDespesa(dto, authentication);
            URI location = URI.create("/api/v1/despesas/" + novaDespesa.id());
            return ResponseEntity
                    .created(location)
                    .body(novaDespesa);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro inesperado: " + e.getMessage());
        }
    }


    @Operation(
            summary = "Lista as despesas com ou sem filtro com o usuário autenticado",
            description = "Se nenhum filtro for passado, retorna a lista de despesas paginadas. Se filtros forem usados, retorna a lista filtrada",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Despesas encontradas com sucesso",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ListaDespesasResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Falha na autenticação. Token inválido ou ausente.")
            }
    )
    @GetMapping
    public ResponseEntity<?> buscarDespesasComFiltros(
            @Parameter(description = "Categoria da despesa (opcional)")
            @RequestParam(required = false) String categoria,

            @Parameter(description = "Data de fim no formato ISO (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,

            @Parameter(description = "Data de início no formato ISO (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Authentication authentication
    ) {
        boolean comFiltro = categoria != null || dataInicio != null || dataFim != null;

        if (comFiltro) {
            List<DespesaFiltradaDTO> despesas = despesaService.buscarDespesasComFiltros(categoria, dataInicio, dataFim, authentication);
    
            String mensagem = despesas.isEmpty()
                    ? "Nenhuma despesa encontrada com os filtros fornecidos."
                    : "Despesas encontradas com sucesso.";
    
            ListaDespesasResponseDTO resposta = new ListaDespesasResponseDTO(mensagem, despesas);
            return ResponseEntity.ok(resposta);
            
        } else {
            
            try {
                Pageable pageable = PageRequest.of(0, 6);
                Page<DespesaDTO> despesaPage = despesaService.listarDespesaDoUsuario(authentication, pageable);
        
                return ResponseEntity.ok(new PageResponseDTO<>(despesaPage));
        
            } catch (Exception e) {
        
                return ResponseEntity.badRequest().body("Erro inesperado: " + e.getMessage());
            }
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarDespesa(@PathVariable Long id, 
            @RequestBody @Valid AtualizarDespesaDTO dto, Authentication authentication) {

        try {
            DespesaDTO atualizada = despesaService.editarDespesa(authentication, id, dto);
            return ResponseEntity.ok(atualizada);

        } catch (EntityNotFoundException e) {
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Despesa não encontrada");
        } catch (AccessDeniedException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado, token inválido");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar despesa: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Desativar uma despesa (safe delete)",
            description = "Marca a despesa como inativa em vez de removê-la do banco de dados. " +
                    "Retorna mensagem confirmando a exclusão lógica.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Despesa deletada com sucesso.", content = @Content(mediaType = "text/plain")),
                @ApiResponse(responseCode = "400", description = "Despesa não encontrada", content = @Content),
                @ApiResponse(responseCode = "401", description = "Falha na autenticação. Token ausente ou inválido.", content = @Content),
            }
    )
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<?> desativarDespesa(
            @Parameter(description = "ID da despesa a ser inativada", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        despesaService.safeDeleteDespesa(id, authentication);
        return ResponseEntity.ok("Despesa deletada com sucesso.");
    }
}
