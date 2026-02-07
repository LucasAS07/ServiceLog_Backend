package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.dto.LoginRequest;
import io.lrsystem.ServiceLog.dto.LoginResponse;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.exceptions.UsuarioOuSenhaInvalidos;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    private final BCryptPasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;

    private final JwtEncoder jwtEncoder;

    public TokenService(BCryptPasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository,
                        JwtEncoder jwtEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.jwtEncoder = jwtEncoder;
    }

    public LoginResponse login(LoginRequest request) {

        var usuario = usuarioRepository.findByEmail(request.email());

        if (usuario.isEmpty() || !usuario.get().isLoginCorrect(request,passwordEncoder)){
            throw new UsuarioOuSenhaInvalidos("Usuario ou Senha invalidos");
        }

        var now = Instant.now();
        var expiresIn = 300L;

        var scopes = usuario.get().getRole().getNome();

        var claims = JwtClaimsSet.builder()
                .issuer("backend")
                .subject(usuario.get().getEmail())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("username", usuario.get().getEmail())
                .claim("usuarioid", usuario.get().getId())
                .claim("scope", scopes)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponse(jwtValue, expiresIn);
    }

}
