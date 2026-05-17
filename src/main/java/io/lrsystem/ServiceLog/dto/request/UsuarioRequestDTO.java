package io.lrsystem.ServiceLog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequestDTO {

    @Schema(example = "Joao da Silva")
    @NotBlank
    private String nome;

    @Schema(example = "joao@email.com")
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @Schema(example = "1234")
    @NotBlank
    private String senha;

    @Schema(example = "true")
    private boolean status;

    @Schema(examples = "1")
    private Long roleId;

}
