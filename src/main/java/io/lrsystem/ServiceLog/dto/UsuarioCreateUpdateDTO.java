package io.lrsystem.ServiceLog.dto;

import lombok.Data;

@Data
public class UsuarioCreateUpdateDTO {

    private String nome;
    private String email;
    private String senha;
    private boolean status;

}
