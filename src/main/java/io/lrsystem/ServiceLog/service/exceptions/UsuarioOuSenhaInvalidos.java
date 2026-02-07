package io.lrsystem.ServiceLog.service.exceptions;

public class UsuarioOuSenhaInvalidos extends RuntimeException {
    public UsuarioOuSenhaInvalidos(String message) {
        super(message);
    }
}
