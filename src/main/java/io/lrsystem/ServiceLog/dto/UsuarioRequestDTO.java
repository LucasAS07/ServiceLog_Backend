package io.lrsystem.ServiceLog.dto;

import lombok.Data;

@Data
public class UsuarioRequestDTO {

    private Long id;
    private String nome;
    private String email;
    private boolean status;

}
