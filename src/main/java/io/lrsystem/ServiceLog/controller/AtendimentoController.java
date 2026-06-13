package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.doc.AtendimentoDocController;
import io.lrsystem.ServiceLog.dto.request.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.response.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.service.AtendimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController implements AtendimentoDocController {

    private final AtendimentoService atendimentoService;

    @PostMapping
    public ResponseEntity<AtendimentoResponseDTO> salvar(@RequestBody @Valid AtendimentoRequestDTO atendimento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atendimentoService.adicionar(atendimento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtendimentoResponseDTO> atualizar(@PathVariable Long id, @RequestBody
                                                                 @Valid AtendimentoRequestDTO atendimentoDTO) {
        AtendimentoResponseDTO atendimento = atendimentoService.atualizar(id,atendimentoDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(atendimento);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
        atendimentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_USER')")
    @GetMapping("/relatorios")
    public ResponseEntity<byte[]> folhaAtendimentos(
                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                    LocalDate inicio,
                                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                    LocalDate fim) throws JRException {
        byte[] relatorio = atendimentoService.gerarFolhaAtendimentos(inicio,fim);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(relatorio);
    }

    @GetMapping("/total-dia")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_USER')")
    public Long totalDia() {
        return atendimentoService.totalAtendimentosDia();
    }

}
