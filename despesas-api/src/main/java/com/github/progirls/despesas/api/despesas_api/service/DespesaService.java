package com.github.progirls.despesas.api.despesas_api.service;

import com.github.progirls.despesas.api.despesas_api.dto.AtualizarDespesaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.DespesaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.DespesaFiltradaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.NovaDespesaDTO;
import com.github.progirls.despesas.api.despesas_api.entities.Despesa;
import com.github.progirls.despesas.api.despesas_api.entities.DespesaSpecification;
import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import com.github.progirls.despesas.api.despesas_api.mapper.DespesaMapper;
import com.github.progirls.despesas.api.despesas_api.repository.DespesaRepository;
import com.github.progirls.despesas.api.despesas_api.repository.UsuarioRepository;
import com.github.progirls.despesas.api.despesas_api.security.AuthenticatedUserService;

import com.github.progirls.despesas.api.despesas_api.security.UserAuthenticated;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DespesaService {

    private final DespesaRepository despesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DespesaMapper despesaMapper;
    private final AuthenticatedUserService authenticatedUserService;

    public DespesaService(DespesaRepository despesaRepository, UsuarioRepository usuarioRepository, DespesaMapper despesaMapper, AuthenticatedUserService authenticatedUserService) {
        this.despesaRepository = despesaRepository;
        this.usuarioRepository = usuarioRepository;
        this.despesaMapper = despesaMapper;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public DespesaDTO criarDespesa(NovaDespesaDTO dto, Authentication authentication) {
        Despesa despesa = despesaMapper.toEntity(dto);

        if (dto.parcelamento() == 1) {
            despesa.setDataFim(dto.dataInicio());
        } else {
            despesa.setDataFim(calcularDataFim(dto.dataInicio(), dto.parcelamento()));
        }

        despesa.setQuitado(quitadoOuFalso(dto));

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        despesa.setUsuario(usuario);

        Despesa salva = despesaRepository.save(despesa);
        return despesaMapper.toDTO(salva);
    }

    public List<DespesaFiltradaDTO> buscarDespesasComFiltros(
            String categoria,
            LocalDate dataInicio,
            LocalDate dataFim,
            Authentication authentication
    ) {
        Usuario usuario = authenticatedUserService.getUsuarioAutenticado(authentication);

        return despesaRepository
                .findAll(DespesaSpecification.comFiltros(usuario, categoria, dataInicio, dataFim))
                .stream()
                .map(despesaMapper::toFiltradaDTO)
                .toList();
    }

    @Transactional
    public Page<DespesaDTO> listarDespesaDoUsuario(Authentication authentication, Pageable pageable) {

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        return despesaRepository.findByUsuario(usuario, pageable)
                .map(despesaMapper::toDTO);
    }

    @Transactional
    public DespesaDTO editarDespesa(Authentication authentication, Long despesaId, AtualizarDespesaDTO atualizarDespesa) throws AccessDeniedException {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario não encontrado."));

        Despesa despesa = despesaRepository.findById(despesaId)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada."));

        if (!despesa.getUsuario().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Você não tem permissão para editar essa despesa.");
        }

        despesa.setValor(atualizarDespesa.valor());
        despesa.setDescricao(atualizarDespesa.descricao());
        despesa.setParcelamento(atualizarDespesa.parcelamento());
        despesa.setDataInicio(atualizarDespesa.dataInicio());

        if (atualizarDespesa.parcelamento() == 1) {
            despesa.setDataFim(atualizarDespesa.dataInicio());
        } else {
            despesa.setDataFim(atualizarDespesa.dataInicio().plusMonths(atualizarDespesa.parcelamento()));
        }

        return despesaMapper.toDTO(despesaRepository.save(despesa));
    }

    private LocalDate calcularDataFim(LocalDate dataInicio, int parcelamento) {
        return dataInicio.plusMonths(parcelamento);
    }

    private Boolean quitadoOuFalso(NovaDespesaDTO dto) {
        return dto.quitado() != null ? dto.quitado() : false;
    }

    public void safeDeleteDespesa(Long idDespesa, Authentication authentication) {
        var idUsuario = authenticatedUserService.getIdUsuarioAutenticado(authentication);

        var despesa = despesaRepository.buscarDespesaDoUsuario(idDespesa, idUsuario).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID da despesa inválido ou não pertence ao usuário."));

        despesa.setAtivo(false);
        despesaRepository.save(despesa);

    }
}
