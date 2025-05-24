package com.github.progirls.despesas.api.despesas_api.entities;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DespesaSpecification {

    public static Specification<Despesa> comFiltros(Usuario usuario, String categoria, LocalDate dataInicio, LocalDate dataFim){

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("usuario"), usuario));

            if(categoria != null && !categoria.isEmpty()){
                predicates.add(cb.equal(root.get("categoria"), categoria));
            }

            if(dataInicio != null){
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataInicio"), dataInicio));
            }

            if(dataFim != null){
                predicates.add(cb.lessThanOrEqualTo(root.get("dataFim"), dataFim));
            }

            return  cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));

        };
    }
}
