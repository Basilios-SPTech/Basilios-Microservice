package com.basilios.emailapi.application.service;

import com.basilios.emailapi.domain.model.ProcessedEvent;
import com.basilios.emailapi.infra.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Serviço de idempotência baseado em eventId.
 * Garante que cada evento seja processado apenas uma vez,
 * evitando envio duplicado de emails em caso de reentrega do broker.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    /**
     * Verifica se o evento já foi processado.
     */
    public boolean isAlreadyProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        return processedEventRepository.existsById(eventId);
    }

    /**
     * Marca o evento como processado.
     */
    public void markAsProcessed(String eventId, String eventType) {
        ProcessedEvent event = ProcessedEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .processedAt(LocalDateTime.now())
                .build();

        processedEventRepository.save(event);
        log.debug("Evento marcado como processado: eventId={}, type={}", eventId, eventType);
    }

}
