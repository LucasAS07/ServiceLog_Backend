package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.dto.UsuarioRequestDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.service.AtendimentoService;
import io.lrsystem.ServiceLog.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AtendimentoService atendimentoService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listar();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscar(@PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscar(id);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<UsuarioRequestDTO> salvar(@RequestBody Usuario usuario) {
        UsuarioRequestDTO novoUsuario = usuarioService.salvar(usuario);
        return ResponseEntity.status(201).body(novoUsuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioRequestDTO> atualizar(@PathVariable Long id,
                                                       @RequestBody UsuarioRequestDTO usuarioDto) {
        UsuarioRequestDTO usuario = usuarioService.atualizar(id,usuarioDto);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(Long id) {
        // TODO IMPLEMENTAR ATIVAÇÃO DE USUARIO
        return null;
    }

    @PutMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(Long id) {
        // TODO IMPLEMENTAR INATIVAÇÃO DE USUARIO
        return null;
    }

    @GetMapping("/{id}/atendimentos")
    public ResponseEntity<List<AtendimentoResponseDTO>> listarAtendimentos(@PathVariable Long id) {
        return ResponseEntity.ok(atendimentoService.listarPorUsuario(id));
    }

}
