package com.basilios.emailapi.application.service;

import com.basilios.emailapi.domain.model.ProcessedEvent;
import com.basilios.emailapi.infra.repository.ProcessedEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    @DisplayName("Deve retornar true quando evento já foi processado")
    void isAlreadyProcessed_existingEvent_returnsTrue() {
        when(processedEventRepository.existsById("evt-123")).thenReturn(true);
        assertTrue(idempotencyService.isAlreadyProcessed("evt-123"));
    }

    @Test
    @DisplayName("Deve retornar false quando evento é novo")
    void isAlreadyProcessed_newEvent_returnsFalse() {
        when(processedEventRepository.existsById("evt-456")).thenReturn(false);
        assertFalse(idempotencyService.isAlreadyProcessed("evt-456"));
    }

    @Test
    @DisplayName("Deve retornar false para eventId nulo ou vazio")
    void isAlreadyProcessed_nullOrBlank_returnsFalse() {
        assertFalse(idempotencyService.isAlreadyProcessed(null));
        assertFalse(idempotencyService.isAlreadyProcessed(""));
        assertFalse(idempotencyService.isAlreadyProcessed("   "));
    }

    @Test
    @DisplayName("Deve salvar evento como processado")
    void markAsProcessed_savesEvent() {
        idempotencyService.markAsProcessed("evt-789", "ORDER_STATUS_CHANGED");

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(captor.capture());

        ProcessedEvent saved = captor.getValue();
        assertEquals("evt-789", saved.getEventId());
        assertEquals("ORDER_STATUS_CHANGED", saved.getEventType());
        assertNotNull(saved.getProcessedAt());
    }

}
