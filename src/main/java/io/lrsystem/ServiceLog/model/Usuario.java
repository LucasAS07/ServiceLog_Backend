package io.lrsystem.ServiceLog.model;

import io.lrsystem.ServiceLog.dto.LoginRequest;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public boolean isLoginCorrect(LoginRequest request, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(request.password(), this.senha);
    }

}
