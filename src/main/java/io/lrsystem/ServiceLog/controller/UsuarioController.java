package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.UsuarioDTO;
import io.lrsystem.ServiceLog.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<Page<UsuarioDTO>> listar(Pageable pageable) {
        Page<UsuarioDTO> usuarios = usuarioService.list(pageable);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscaPorId(@PathVariable Long id){
        UsuarioDTO usuario = usuarioService.findById(id);
        return ResponseEntity.ok(usuario);
    }

}
