package io.lrsystem.ServiceLog.service;

import io.lrsystem.ServiceLog.model.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService{

    private final JavaMailSender javaMailSender;
    private final AtendimentoService atendimentoService;
    private final AuthService authService;

    public void sendEmailWhiteAttachment(String subject, String body, LocalDate inicio,
                                         LocalDate fim, Usuario usuario) throws MessagingException, JRException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper mimeMessageHelper =  new MimeMessageHelper(mimeMessage,true);
        
        mimeMessageHelper.setFrom("notificacao@servicelog.com.br");
        mimeMessageHelper.setTo(usuario.getEmail());
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(body);

        byte[] pdfBytes = atendimentoService.gerarFolhaAtendimentos(usuario.getId(),inicio,fim);

        String nomeArquivo = "Folha atendimentos periodo de "
                + inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " até " + fim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        mimeMessageHelper.addAttachment(
                nomeArquivo,
                resource,
                "application/pdf"
        );

        javaMailSender.send(mimeMessage);
    }

}
