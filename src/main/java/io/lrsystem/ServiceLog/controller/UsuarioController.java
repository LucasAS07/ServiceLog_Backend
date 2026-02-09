package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.dto.UsuarioRequestDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.mapper.UsuarioMapper;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.service.AtendimentoService;
import io.lrsystem.ServiceLog.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioMapper mapper;

    public UsuarioController(UsuarioMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AtendimentoService atendimentoService;

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listar();
        return ResponseEntity.ok(usuarios);
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscar(@PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscar(id);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> salvar(@RequestBody @Valid UsuarioRequestDTO usuarioDTO) {
        Usuario usuario = mapper.usuarioToEntity(usuarioDTO);
        return ResponseEntity.status(201).body(usuarioService.salvar(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                       @RequestBody @Valid UsuarioRequestDTO usuarioDto) {
        UsuarioResponseDTO usuario = usuarioService.atualizar(id,usuarioDto);
        return ResponseEntity.ok(usuario);
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        usuarioService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @DeleteMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        usuarioService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_USER')")
    @GetMapping("/atendimentos")
    public ResponseEntity<Page<AtendimentoResponseDTO>> listarAtendimentos(@RequestParam(required = false)
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

}
