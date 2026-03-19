package com.basilios.emailapi.application.service;

import com.basilios.emailapi.domain.model.FailedNotification;
import com.basilios.emailapi.infra.repository.FailedNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço para registrar notificações que falharam após todas as tentativas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FailedNotificationService {

    private final FailedNotificationRepository failedNotificationRepository;
    private final ObjectMapper objectMapper;

    public void save(String eventId, String eventType, String recipientEmail,
                     String subject, Object payload, String errorMessage, int attemptCount) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            FailedNotification failed = FailedNotification.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .payload(payloadJson)
                    .errorMessage(errorMessage)
                    .attemptCount(attemptCount)
                    .build();

            failedNotificationRepository.save(failed);
            log.info("Notificação falha salva: eventId={}, email={}", eventId, recipientEmail);
        } catch (Exception e) {
            log.error("Erro ao salvar notificação falha no banco: eventId={}, erro={}", eventId, e.getMessage());
        }
    }

}
