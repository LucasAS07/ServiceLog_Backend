package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.service.AtendimentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/atendimentos")
public class AtendimentoController {

    @Autowired
    private AtendimentoService atendimentoService;

    @PostMapping
    public ResponseEntity<AtendimentoResponseDTO> salvar(@RequestBody @Valid AtendimentoRequestDTO atendimento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atendimentoService.adicionar(atendimento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtendimentoResponseDTO> atualizar(@PathVariable Long id,
                                                            @RequestBody @Valid AtendimentoRequestDTO atendimentoDTO) {
        AtendimentoResponseDTO atendimento = atendimentoService.atualizar(id,atendimentoDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(atendimento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
        atendimentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
