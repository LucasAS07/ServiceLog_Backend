package io.lrsystem.ServiceLog.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String senha;
    private boolean status;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY,
                cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Atendimento> atendimentos;

}
