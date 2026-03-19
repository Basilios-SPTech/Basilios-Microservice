package com.basilios.emailapi.infra.messaging;

import com.basilios.emailapi.application.dto.PasswordResetRequestedEventDTO;
import com.basilios.emailapi.application.service.FailedNotificationService;
import com.basilios.emailapi.application.service.IdempotencyService;
import com.basilios.emailapi.infra.email.EmailSenderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetConsumerTest {

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private FailedNotificationService failedNotificationService;

    @InjectMocks
    private PasswordResetConsumer passwordResetConsumer;

    private PasswordResetRequestedEventDTO buildEvent() {
        return PasswordResetRequestedEventDTO.builder()
                .eventId("evt-reset-001")
                .email("user@email.com")
                .resetUrl("http://localhost:5173/reset-password?token=abc123")
                .userName("Maria")
                .expiresIn("1 hora")
                .build();
    }

    @Test
    @DisplayName("Deve enviar email de reset com sucesso")
    void handlePasswordResetRequested_success() {
        when(idempotencyService.isAlreadyProcessed("evt-reset-001")).thenReturn(false);
        PasswordResetRequestedEventDTO event = buildEvent();

        passwordResetConsumer.handlePasswordResetRequested(event);

        verify(emailSenderService).sendPasswordResetEmail(
                "user@email.com",
                "http://localhost:5173/reset-password?token=abc123",
                "Maria",
                "1 hora"
        );
        verify(idempotencyService).markAsProcessed("evt-reset-001", "PASSWORD_RESET_REQUESTED");
    }

    @Test
    @DisplayName("Deve ignorar evento duplicado de reset")
    void handlePasswordResetRequested_duplicate() {
        when(idempotencyService.isAlreadyProcessed("evt-reset-001")).thenReturn(true);
        PasswordResetRequestedEventDTO event = buildEvent();

        passwordResetConsumer.handlePasswordResetRequested(event);

        verifyNoInteractions(emailSenderService);
    }

    @Test
    @DisplayName("Deve salvar falha e re-lançar exceção quando envio de reset falha")
    void handlePasswordResetRequested_failure() {
        when(idempotencyService.isAlreadyProcessed("evt-reset-001")).thenReturn(false);
        PasswordResetRequestedEventDTO event = buildEvent();
        doThrow(new RuntimeException("SMTP timeout")).when(emailSenderService)
                .sendPasswordResetEmail(any(), any(), any(), any());

        assertThrows(RuntimeException.class, () ->
                passwordResetConsumer.handlePasswordResetRequested(event));

        verify(failedNotificationService).save(
                eq("evt-reset-001"), eq("PASSWORD_RESET_REQUESTED"), eq("user@email.com"),
                any(), eq(event), eq("SMTP timeout"), eq(1));
    }

}
