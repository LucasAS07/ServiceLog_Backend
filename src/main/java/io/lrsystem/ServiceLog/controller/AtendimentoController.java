package io.lrsystem.ServiceLog.controller;

import io.lrsystem.ServiceLog.dto.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.AtendimentoResponseDTO;
import io.lrsystem.ServiceLog.service.AtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Atendimentos", description = "Gerencia os atendimentos")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;

    @PostMapping
    @Operation(summary = "Cadastra um novo atendimento")
    public ResponseEntity<AtendimentoResponseDTO> salvar(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                                     (description = "Representação de um atendimento",
                                                                             required = true)
                                                        @RequestBody @Valid AtendimentoRequestDTO atendimento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atendimentoService.adicionar(atendimento));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuario",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do atendimento invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<AtendimentoResponseDTO> atualizar(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                                       (description = "Representação de um atendimento",
                                                                                required = true)
                                                                @Parameter(description = "ID de um atendimento",
                                                                        example = "1", required = true)
                                                                @PathVariable Long id,
                                                                 @RequestBody
                                                                 @Valid AtendimentoRequestDTO atendimentoDTO) {
        AtendimentoResponseDTO atendimento = atendimentoService.atualizar(id,atendimentoDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(atendimento);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_USER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um usuario específico",responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do atendimento invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    public ResponseEntity<Void> deletar(@Parameter(description = "ID de um atendimento",
                                        example = "1", required = true)
                                            @PathVariable Long id){
        atendimentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_USER')")
    @GetMapping("/relatorios")
    @Operation(summary = "Gera o relatorio em PDF")
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
    @Operation(summary = "Mostra o total de atendimentos feito no dia")
    public Long totalDia() {
        return atendimentoService.totalAtendimentosDia();
    }

}
