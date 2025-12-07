package io.lrsystem.ServiceLog.dto;

import io.lrsystem.ServiceLog.entities.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter
@Setter
public class UsuarioDTO {

    private Long id;
    private String nome;
    private String email;
    private boolean status;
    private OffsetDateTime dataCadastro;

    public UsuarioDTO(Usuario entity) {
        id = entity.getId();
        nome = entity.getNome();
        email = entity.getEmail();
        status = entity.getStatus();
        dataCadastro = entity.getDataCadastro();
    }

}
