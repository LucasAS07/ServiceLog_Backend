package io.lrsystem.ServiceLog.repository;

import io.lrsystem.ServiceLog.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByNome(String nome);
}
