package io.lrsystem.ServiceLog.repository;

import io.lrsystem.ServiceLog.model.Atendimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento,Long> {

    @Query("""
            select a
            from Atendimento a
            where a.usuario.id = :usuarioId
                and a.dia >= coalesce(:inicio, a.dia)
                and a.dia <= coalesce(:fim, a.dia)
            """)
    Page<Atendimento> buscarPorUsuarioEPeriodo(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            Pageable pageable
    );

}
