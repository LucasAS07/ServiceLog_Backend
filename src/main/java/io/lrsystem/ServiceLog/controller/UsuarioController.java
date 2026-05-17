package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.response.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.dto.request.UsuarioRequestDTO;
import io.lrsystem.ServiceLog.dto.response.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.mapper.UsuarioMapper;
import io.lrsystem.ServiceLog.service.AtendimentoService;
import io.lrsystem.ServiceLog.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gerencia os usuarios")
public class UsuarioController {

    private final UsuarioMapper mapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AtendimentoService atendimentoService;

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping
    @Operation(summary = "Lista todos os usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listar();
        return ResponseEntity.ok(usuarios);
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuario específico", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<UsuarioResponseDTO> buscar(@Parameter(description = "ID de um usuario",
                                                                 example = "1", required = true)
                                                     @PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscar(id);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    @Operation(summary = "Cadastra um novo usuario")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UsuarioResponseDTO> salvar(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                                 (description = "Representação de um usuario",
                                                                 required = true)
                                                        @RequestBody @Valid UsuarioRequestDTO usuarioDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.salvar(usuarioDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuario",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<UsuarioResponseDTO> atualizar(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                                    (description = "Representação de um usuario",
                                                                            required = true)
                                                        @Parameter(description = "ID de um usuario",
                                                                example = "1", required = true)
                                                        @PathVariable Long id,
                                                       @RequestBody @Valid UsuarioRequestDTO usuarioDto) {
        UsuarioResponseDTO usuario = usuarioService.atualizar(id,usuarioDto);
        return ResponseEntity.ok(usuario);
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um usuario específico",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<Void> delete(@Parameter(description = "ID de um usuario",
                                                    example = "1", required = true)
                                       @PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}/ativar")
    @Operation(summary = "Ativa um usuario",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<Void> ativar(@Parameter(description = "ID de um usuario",
                                                    example = "1", required = true)
                                           @PathVariable Long id) {
        usuarioService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}/inativar")
    @Operation(summary = "Inativa um usuario",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<Void> inativar(@Parameter(description = "ID de um usuario",
                                                        example = "1", required = true)
                                            @PathVariable Long id) {
        usuarioService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_USER')")
    @GetMapping("/atendimentos")
    @Operation(summary = "Lista todos atendimentos de um usuario específico",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<Page<AtendimentoResponseDTO>> listarAtendimentos(@Parameter(description = "Data de inicio",
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
                                                                           Pageable pageable) {
        Page<AtendimentoResponseDTO> atendimentos = atendimentoService.listarPorUsuario(inicio, fim, pageable);
        return ResponseEntity.ok(atendimentos);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_USER')")
    @GetMapping("/myuser")
    @Operation(summary = "Busca um usuario específico", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do usuario invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<UsuarioResponseDTO> buscarMeuUsuario() {
        UsuarioResponseDTO usuario = usuarioService.myUser();
        return ResponseEntity.ok(usuario);
    }

}
