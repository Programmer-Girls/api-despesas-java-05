package com.github.progirls.despesas.api.despesas_api.repository;

import com.github.progirls.despesas.api.despesas_api.entities.OtpToken;
import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByUsuario(Usuario usuario);

    void delete(OtpToken otpToken);
}
