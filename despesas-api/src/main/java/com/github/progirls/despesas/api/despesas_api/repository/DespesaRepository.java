package com.github.progirls.despesas.api.despesas_api.repository;

import com.github.progirls.despesas.api.despesas_api.entities.Despesa;
import com.github.progirls.despesas.api.despesas_api.entities.Usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DespesaRepository extends JpaRepository<Despesa, Long>, JpaSpecificationExecutor<Despesa> {
    Page<Despesa> findByUsuario(Usuario usuario, Pageable pageable);

    @Query("SELECT d FROM Despesa d WHERE d.id = :idDespesa AND d.usuario.id = :idUsuario")
    Optional<Despesa> buscarDespesaDoUsuario(@Param("idDespesa") Long idDespesa, @Param("idUsuario") UUID idUsuario);

}
