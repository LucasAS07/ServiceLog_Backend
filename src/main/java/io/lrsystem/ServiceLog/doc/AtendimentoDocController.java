package io.lrsystem.ServiceLog.doc;

import io.lrsystem.ServiceLog.dto.request.AtendimentoRequestDTO;
import io.lrsystem.ServiceLog.dto.response.AtendimentoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Atendimentos", description = "Gerencia os atendimentos")
public interface AtendimentoDocController {

    @Operation(summary = "Cadastra um novo atendimento")
    ResponseEntity<AtendimentoResponseDTO> salvar(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                                 (description = "Representação de um atendimento",
                                                                         required = true)
                                                         @RequestBody @Valid AtendimentoRequestDTO atendimento);

    @Operation(summary = "Atualiza um usuario", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do atendimento invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<AtendimentoResponseDTO> atualizar(@io.swagger.v3.oas.annotations.parameters.RequestBody
                                                                    (description = "Representação de um atendimento",
                                                                            required = true)
                                                            @Parameter(description = "ID de um atendimento",
                                                                    example = "1", required = true)
                                                            Long id,
                                                            AtendimentoRequestDTO atendimentoDTO);

    @Operation(summary = "Exclui um usuario específico", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "ID do atendimento invalido",
                    content = @Content(schema = @Schema(ref = "Problema")))
    })
    ResponseEntity<Void> deletar(@Parameter(description = "ID de um atendimento",
            example = "1", required = true) Long id);

    @Operation(summary = "Gera o relatorio em PDF")
    ResponseEntity<byte[]> folhaAtendimentos(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate fim) throws JRException;

    @Operation(summary = "Mostra o total de atendimentos feito no dia")
    Long totalDia();

}
