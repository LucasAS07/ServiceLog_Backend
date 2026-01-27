package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.UsuarioCreateUpdateDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.model.Usuario;
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
    public ResponseEntity<UsuarioCreateUpdateDTO> salvar(@RequestBody Usuario usuario) {
        UsuarioCreateUpdateDTO novoUsuario = usuarioService.salvar(usuario);
        return ResponseEntity.status(201).body(novoUsuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioCreateUpdateDTO> atualizar(@PathVariable Long id,
                                                            @RequestBody UsuarioCreateUpdateDTO usuarioDto) {
        UsuarioCreateUpdateDTO usuario = usuarioService.atualizar(id,usuarioDto);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
