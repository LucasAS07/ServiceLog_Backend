package io.lrsystem.ServiceLog.config;

import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.RoleRepository;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class AdminUserConfig implements CommandLineRunner {

    @Value("${user.email}")
    private String adminEmail;

    @Value("${user.senha}")
    private String adminSenha;

    private final static String ROLE_USUARIO = "ADMIN";
    private final static String USUARIO = "admin";

    private final RoleRepository roleRepository;

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        var roleAdmin = roleRepository.findByNome(ROLE_USUARIO);

        var userAdmin = usuarioRepository.findByNome(USUARIO);

        userAdmin.ifPresentOrElse(
                user -> {System.out.println("Admin ja existe");},
                () -> {
                    var user = new Usuario();
                    user.setNome("admin");
                    user.setEmail(adminEmail);
                    user.setStatus(true);
                    user.setSenha(passwordEncoder.encode(adminSenha));
                    user.setRole(roleAdmin);
                    usuarioRepository.save(user);
                }
        );

    }
}
