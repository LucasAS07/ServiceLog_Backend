package io.lrsystem.ServiceLog.repositorie;

import io.lrsystem.ServiceLog.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepositorie extends JpaRepository<Usuario,Long> {
}
