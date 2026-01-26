package io.lrsystem.ServiceLog.dto;

import lombok.Data;

@Data
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private boolean status;

}
