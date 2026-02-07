package io.lrsystem.ServiceLog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "roles")
public class Role {

    @Id
    private Long id;
    private String nome;

}
