package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String ROLE_ADMIN = "ADMIN";

    private final UsuarioRepository usuarioRepository;

    public void validateSelfOrAdmin(Long id) {
        Usuario me = authenticated();

        if (!me.getId().equals(id) && !me.getRole().getNome().equals(ROLE_ADMIN)) {
            throw new ForbiddenException("Acesso negado");
        }
    }

    protected Usuario authenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            String userName = jwtPrincipal.getClaim("username");

            return usuarioRepository.findByEmail(userName).get();
        } catch (Exception e) {
            throw new UsernameNotFoundException("Email não encontrado");
        }
    }

}
