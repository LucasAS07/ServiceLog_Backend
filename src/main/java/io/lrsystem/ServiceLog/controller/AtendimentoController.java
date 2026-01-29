package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.service.AtendimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atendimentos")
public class AtendimentoController {

    @Autowired
    private AtendimentoService atendimentoService;

    @PostMapping
    public ResponseEntity<AtendimentoResponseDTO> salvar(@RequestBody AtendimentoRequestDTO atendimento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atendimentoService.adicionar(atendimento));
    }

}
