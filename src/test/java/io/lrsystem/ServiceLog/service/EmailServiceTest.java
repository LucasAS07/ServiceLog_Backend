package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.model.Usuario;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender javaMailSender;
    @Mock private AtendimentoService atendimentoService;
    @Mock private AuthService authService;

    private EmailService service;

    private final LocalDate inicio = LocalDate.of(2025, 12, 26);
    private final LocalDate fim = LocalDate.of(2026, 1, 25);

    @BeforeEach
    void setUp() {
        // ordem do construtor: javaMailSender, atendimentoService, authService
        service = new EmailService(javaMailSender, atendimentoService, authService);
    }

    private static Usuario usuario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Maria");
        u.setEmail("maria@teste.com");
        return u;
    }

    @Test
    @DisplayName("sendEmailWhiteAttachment: monta e envia e-mail com o PDF em anexo")
    void envia_emailComAnexo() throws Exception {
        MimeMessage mensagem = new MimeMessage(Session.getInstance(new Properties()));
        byte[] pdf = "%PDF-fake".getBytes();

        when(javaMailSender.createMimeMessage()).thenReturn(mensagem);
        when(atendimentoService.gerarFolhaAtendimentos(1L, inicio, fim)).thenReturn(pdf);

        service.sendEmailWhiteAttachment("Assunto", "Corpo", inicio, fim, usuario());

        verify(javaMailSender).send(mensagem);

        assertThat(mensagem.getSubject()).isEqualTo("Assunto");
        assertThat(mensagem.getFrom()[0]).hasToString("notificacao@servicelog.com.br");
        assertThat(mensagem.getRecipients(Message.RecipientType.TO)[0]).hasToString("maria@teste.com");

        MimeMultipart raiz = (MimeMultipart) mensagem.getContent();
        List<String> nomesAnexos = new ArrayList<>();
        for (int i = 0; i < raiz.getCount(); i++) {
            BodyPart parte = raiz.getBodyPart(i);
            if (parte.getFileName() != null) {
                nomesAnexos.add(parte.getFileName());
            }
        }

        assertThat(nomesAnexos).hasSize(1);
        assertThat(nomesAnexos.get(0))
                .contains("Folha atendimentos periodo de")
                .contains("26/12/2025")
                .contains("25/01/2026");
    }

    @Test
    @DisplayName("sendEmailWhiteAttachment: falha ao gerar o relatório propaga erro e não envia e-mail")
    void falhaNoRelatorio_naoEnvia() throws Exception {
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(atendimentoService.gerarFolhaAtendimentos(1L, inicio, fim)).thenThrow(new JRException("falha jasper"));

        assertThatThrownBy(() -> service.sendEmailWhiteAttachment("Assunto", "Corpo", inicio, fim, usuario()))
                .isInstanceOf(JRException.class);

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
}
