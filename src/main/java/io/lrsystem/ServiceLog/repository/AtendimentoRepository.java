package io.lrsystem.ServiceLog.repository;

import io.lrsystem.ServiceLog.model.Atendimento;
import io.lrsystem.ServiceLog.model.RelatorioAtendimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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

    @Query("""
    select new io.lrsystem.ServiceLog.model.RelatorioAtendimento(
          a.colaborador,
          a.dia,
          a.diaDaSemana,
          a.inicio,
          a.fim,
          a.tempoTotal,
          a.justificativa
    )
    from Atendimento a
    where a.usuario.id = :usuarioId
      and a.dia >= coalesce(:inicio, a.dia)
      and a.dia <= coalesce(:fim, a.dia)
    order by a.dia, a.inicio
          """)
    List<RelatorioAtendimento> buscarPorDataEUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

}
