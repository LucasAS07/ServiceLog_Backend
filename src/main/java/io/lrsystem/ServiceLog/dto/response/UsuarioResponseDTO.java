package io.lrsystem.ServiceLog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UsuarioResponseDTO {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "João da Silva")
    private String nome;

    @Schema(example = "joao@email.com")
    private String email;

    @Schema(example = "true")
    private boolean status;

}
