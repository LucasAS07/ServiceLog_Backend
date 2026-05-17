package io.lrsystem.ServiceLog.scheduler;

import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnvioFolhasAtendimentos {

    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;
    private static final String NOME_EMAIL = "Folha Mensal de Atendimentos";
    private static final String BODY = "Segue em anexo sua folha de atendimentos dos ultimos 30 dias.";
    private static final String TEMPO = "0 0 0 26 * *";

    @Scheduled(cron = TEMPO)
    public void enviaEmailAtendimentosUsuarios() throws JRException, MessagingException {
        List<Usuario> usuarios = usuarioRepository.findAll();

        LocalDate hoje = LocalDate.now();

        LocalDate inicio = hoje.minusMonths(1).withDayOfMonth(26);

        LocalDate fim = hoje.withDayOfMonth(25);

        for(Usuario usuario : usuarios) {
            try {
                emailService.sendEmailWhiteAttachment(NOME_EMAIL, BODY, inicio, fim, usuario);
            } catch (Exception e) {
                log.error("Erro ao enviar email para usuario {}", usuario.getId());
            }
        }
    }

}
