package io.lrsystem.ServiceLog.doc;

import io.lrsystem.ServiceLog.dto.request.UsuarioRequestDTO;
import io.lrsystem.ServiceLog.dto.response.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.dto.response.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Usuarios", description = "Gerencia os usuarios")
public interface UsuarioDocController {

    @Operation(summary = "Lista todos os usuarios")
    ResponseEntity<List<UsuarioResponseDTO>> listar();

    @Operation(summary = "Busca um usuario específico", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<UsuarioResponseDTO> buscar(@Parameter(description = "ID de um usuario",
            example = "1", required = true) Long id);

    @Operation(summary = "Cadastra um novo usuario")
    ResponseEntity<UsuarioResponseDTO> salvar(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                             (description = "Representação de um usuario",
                                                                     required = true) UsuarioRequestDTO usuarioDTO);

    @Operation(summary = "Atualiza um usuario",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<UsuarioResponseDTO> atualizar(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                                (description = "Representação de um usuario",
                                                                        required = true)
                                                        @Parameter(description = "ID de um usuario",
                                                                example = "1", required = true)
                                                        Long id, UsuarioRequestDTO usuarioDto);

    @Operation(summary = "Exclui um usuario específico",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<Void> delete(@Parameter(description = "ID de um usuario",
            example = "1", required = true) Long id);

    @Operation(summary = "Ativa um usuario",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<Void> ativar(@Parameter(description = "ID de um usuario",
            example = "1", required = true) Long id);

    @Operation(summary = "Inativa um usuario",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<Void> inativar(@Parameter(description = "ID de um usuario",
            example = "1", required = true) Long id);

    @Operation(summary = "Lista todos atendimentos de um usuario específico",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
     ResponseEntity<Page<AtendimentoResponseDTO>> listarAtendimentos(@Parameter(description = "Data de inicio",
                                                                                   example = "2026-02-23",
                                                                                   required = false)
                                                                           @RequestParam(required = false)
                                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                           LocalDate inicio,

                                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                           LocalDate fim,

                                                                           @PageableDefault(
                                                                                   size = 20,
                                                                                   sort = "dia",
                                                                                   direction = Sort.Direction.ASC
                                                                           )
                                                                           Pageable pageable);

    @Operation(summary = "Busca um usuario específico", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<UsuarioResponseDTO> buscarMeuUsuario();

}
