package io.lrsystem.ServiceLog.ExceptionHandler;

import io.lrsystem.ServiceLog.dto.CustomError;
import io.lrsystem.ServiceLog.service.exceptions.AtendimentoNaoEncontrado;
import io.lrsystem.ServiceLog.service.exceptions.HorarioInvalidoException;
import io.lrsystem.ServiceLog.service.exceptions.UsuarioNaoEncontradoException;
import io.lrsystem.ServiceLog.service.exceptions.UsuarioOuSenhaInvalidos;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ServiceLogExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomError> illegalArgument(
            IllegalArgumentException e,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        CustomError err = new CustomError(
                Instant.now(),
                status.value(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<CustomError> usuarioNotFound(UsuarioNaoEncontradoException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomError err = new CustomError(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(AtendimentoNaoEncontrado.class)
    public ResponseEntity<CustomError> atendimentoNotFound(AtendimentoNaoEncontrado e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomError err = new CustomError(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HorarioInvalidoException.class)
    public ResponseEntity<CustomError> horarioInvalido(HorarioInvalidoException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        CustomError err = new CustomError(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CustomError> entidadeNotFound(EntityNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        CustomError err = new CustomError(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UsuarioOuSenhaInvalidos.class)
    public ResponseEntity<CustomError> loginInvalido(UsuarioOuSenhaInvalidos e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        CustomError err = new CustomError(Instant.now(), status.value(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomError> jsonInvalido(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        String mensagem = "Formato de horário inválido. Use HH:mm";

        CustomError err = new CustomError(
                Instant.now(),
                status.value(),
                mensagem,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

}
