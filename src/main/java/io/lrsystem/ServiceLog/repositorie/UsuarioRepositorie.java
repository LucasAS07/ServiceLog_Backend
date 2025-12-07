package io.lrsystem.ServiceLog.repositorie;

import io.lrsystem.ServiceLog.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepositorie extends JpaRepository<Usuario,Long> {
}
