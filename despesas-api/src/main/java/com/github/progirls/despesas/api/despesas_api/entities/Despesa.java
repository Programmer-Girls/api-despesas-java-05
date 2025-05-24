package com.github.progirls.despesas.api.despesas_api.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "despesas")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Despesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_despesa")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private Double valor;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private Integer parcelamento;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    private Boolean quitado;

    @Column(nullable = false)
    private Boolean ativo = true;
}
