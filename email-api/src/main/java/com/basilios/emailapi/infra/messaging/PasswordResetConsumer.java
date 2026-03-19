package com.basilios.emailapi.infra.messaging;

import com.basilios.emailapi.application.dto.PasswordResetRequestedEventDTO;
import com.basilios.emailapi.application.service.FailedNotificationService;
import com.basilios.emailapi.application.service.IdempotencyService;
import com.basilios.emailapi.infra.config.RabbitMQConfig;
import com.basilios.emailapi.infra.email.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer RabbitMQ para eventos de solicitação de reset de senha.
 * Recebe o evento, verifica idempotência e dispara o email de reset.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordResetConsumer {

    private final EmailSenderService emailSenderService;
    private final IdempotencyService idempotencyService;
    private final FailedNotificationService failedNotificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PASSWORD_RESET)
    public void handlePasswordResetRequested(PasswordResetRequestedEventDTO event) {
        log.info("Evento de reset de senha recebido: email={}", event.getEmail());

        // Idempotência
        if (idempotencyService.isAlreadyProcessed(event.getEventId())) {
            log.warn("Evento duplicado de reset ignorado: eventId={}", event.getEventId());
            return;
        }

        try {
            emailSenderService.sendPasswordResetEmail(
                    event.getEmail(),
                    event.getResetUrl(),
                    event.getUserName(),
                    event.getExpiresIn()
            );

            idempotencyService.markAsProcessed(event.getEventId(), "PASSWORD_RESET_REQUESTED");
            log.info("Email de reset enviado com sucesso para: {}", event.getEmail());

        } catch (Exception e) {
            log.error("Erro ao enviar email de reset: eventId={}, erro={}",
                    event.getEventId(), e.getMessage());

            failedNotificationService.save(
                    event.getEventId(),
                    "PASSWORD_RESET_REQUESTED",
                    event.getEmail(),
                    "Redefinição de Senha - Basilios",
                    event,
                    e.getMessage(),
                    1
            );

            throw e;
        }
    }

}
