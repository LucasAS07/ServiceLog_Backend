package io.lrsystem.ServiceLog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequestDTO {

    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    private boolean status;

}
