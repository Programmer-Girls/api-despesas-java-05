package com.github.progirls.despesas.api.despesas_api.security;

import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import com.github.progirls.despesas.api.despesas_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthenticatedUserService {

    private final UsuarioRepository usuarioRepository;

    public AuthenticatedUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuarioAutenticado(Authentication authentication) {
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, ("Usuário não encontrado")));
    }

    public UUID getIdUsuarioAutenticado(Authentication authentication) {
        return getUsuarioAutenticado(authentication).getId();
    }

}
