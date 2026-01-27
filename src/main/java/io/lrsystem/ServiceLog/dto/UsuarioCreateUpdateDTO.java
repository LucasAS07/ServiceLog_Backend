package io.lrsystem.ServiceLog.dto;

import lombok.Data;

@Data
public class UsuarioCreateUpdateDTO {

    private Long id;
    private String nome;
    private String email;
    private boolean status;

}
