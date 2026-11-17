package io.lrsystem.ServiceLog.scheduler;

import io.lrsystem.ServiceLog.model.Usuario;
import io.lrsystem.ServiceLog.repository.UsuarioRepository;
import io.lrsystem.ServiceLog.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioFolhasAtendimentosTest {

    @Mock private EmailService emailService;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private EnvioFolhasAtendimentos scheduler;

    private static Usuario usuario(Long id, String email) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    @Test
    @DisplayName("envia uma folha para cada usuário, no período 26 do mês anterior até 25 do mês atual")
    void enviaParaTodosOsUsuarios() throws Exception {
        Usuario u1 = usuario(1L, "a@teste.com");
        Usuario u2 = usuario(2L, "b@teste.com");
        when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));

        scheduler.enviaEmailAtendimentosUsuarios();

        ArgumentCaptor<LocalDate> inicio = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> fim = ArgumentCaptor.forClass(LocalDate.class);
        verify(emailService).sendEmailWhiteAttachment(anyString(), anyString(), inicio.capture(), fim.capture(), eq(u1));
        verify(emailService).sendEmailWhiteAttachment(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class), eq(u2));

        LocalDate hoje = LocalDate.now();
        assertThat(inicio.getValue()).isEqualTo(hoje.minusMonths(1).withDayOfMonth(26));
        assertThat(fim.getValue()).isEqualTo(hoje.withDayOfMonth(25));
    }

    @Test
    @DisplayName("falha no envio para um usuário não interrompe os demais")
    void falhaIndividual_naoInterrompeOsOutros() throws Exception {
        Usuario u1 = usuario(1L, "a@teste.com");
        Usuario u2 = usuario(2L, "b@teste.com");
        when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));
        doThrow(new RuntimeException("SMTP fora do ar"))
                .when(emailService)
                .sendEmailWhiteAttachment(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class), eq(u1));

        assertThatCode(() -> scheduler.enviaEmailAtendimentosUsuarios()).doesNotThrowAnyException();

        verify(emailService).sendEmailWhiteAttachment(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class), eq(u2));
    }

    @Test
    @DisplayName("sem usuários, nenhum e-mail é enviado")
    void semUsuarios_naoEnvia() throws Exception {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        scheduler.enviaEmailAtendimentosUsuarios();

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("erro ao buscar usuários é tratado e não derruba o scheduler")
    void erroNoRepositorio_eEngolido() {
        when(usuarioRepository.findAll()).thenThrow(new RuntimeException("banco fora do ar"));

        assertThatCode(() -> scheduler.enviaEmailAtendimentosUsuarios()).doesNotThrowAnyException();

        verifyNoInteractions(emailService);
    }
}
