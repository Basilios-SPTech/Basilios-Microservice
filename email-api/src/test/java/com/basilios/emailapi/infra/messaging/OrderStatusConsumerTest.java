package com.basilios.emailapi.infra.messaging;

import com.basilios.emailapi.application.dto.OrderStatusChangedEventDTO;
import com.basilios.emailapi.application.service.FailedNotificationService;
import com.basilios.emailapi.application.service.IdempotencyService;
import com.basilios.emailapi.domain.enums.StatusPedidoEnum;
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
class OrderStatusConsumerTest {

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private FailedNotificationService failedNotificationService;

    @InjectMocks
    private OrderStatusConsumer orderStatusConsumer;

    private OrderStatusChangedEventDTO buildEvent(StatusPedidoEnum newStatus) {
        return OrderStatusChangedEventDTO.builder()
                .eventId("evt-001")
                .orderId(1L)
                .orderCode("PED-001")
                .oldStatus(StatusPedidoEnum.PENDENTE)
                .newStatus(newStatus)
                .clientEmail("cliente@email.com")
                .clientName("João")
                .build();
    }

    @Test
    @DisplayName("Deve enviar email de pedido confirmado")
    void handleOrderStatusChanged_confirmed() {
        when(idempotencyService.isAlreadyProcessed("evt-001")).thenReturn(false);
        OrderStatusChangedEventDTO event = buildEvent(StatusPedidoEnum.CONFIRMADO);

        orderStatusConsumer.handleOrderStatusChanged(event);

        verify(emailSenderService).sendOrderConfirmedEmail("cliente@email.com", "João", "PED-001");
        verify(idempotencyService).markAsProcessed("evt-001", "ORDER_STATUS_CHANGED");
    }

    @Test
    @DisplayName("Deve enviar email de pedido em preparo")
    void handleOrderStatusChanged_preparing() {
        when(idempotencyService.isAlreadyProcessed("evt-001")).thenReturn(false);
        OrderStatusChangedEventDTO event = buildEvent(StatusPedidoEnum.PREPARANDO);

        orderStatusConsumer.handleOrderStatusChanged(event);

        verify(emailSenderService).sendOrderPreparingEmail("cliente@email.com", "João", "PED-001");
    }

    @Test
    @DisplayName("Deve enviar email de pedido despachado")
    void handleOrderStatusChanged_dispatched() {
        when(idempotencyService.isAlreadyProcessed("evt-001")).thenReturn(false);
        OrderStatusChangedEventDTO event = buildEvent(StatusPedidoEnum.DESPACHADO);

        orderStatusConsumer.handleOrderStatusChanged(event);

        verify(emailSenderService).sendOrderDispatchedEmail("cliente@email.com", "João", "PED-001");
    }

    @Test
    @DisplayName("Deve enviar email de pedido entregue")
    void handleOrderStatusChanged_delivered() {
        when(idempotencyService.isAlreadyProcessed("evt-001")).thenReturn(false);
        OrderStatusChangedEventDTO event = buildEvent(StatusPedidoEnum.ENTREGUE);

        orderStatusConsumer.handleOrderStatusChanged(event);

        verify(emailSenderService).sendOrderDeliveredEmail("cliente@email.com", "João", "PED-001");
    }

    @Test
    @DisplayName("Deve enviar email de pedido cancelado")
    void handleOrderStatusChanged_cancelled() {
        when(idempotencyService.isAlreadyProcessed("evt-001")).thenReturn(false);
        OrderStatusChangedEventDTO event = buildEvent(StatusPedidoEnum.CANCELADO);
        event.setMotivo("Cliente solicitou");

        orderStatusConsumer.handleOrderStatusChanged(event);

        verify(emailSenderService).sendOrderCancelledEmail("cliente@email.com", "João", "PED-001", "Cliente solicitou");
    }

    @Test
    @DisplayName("Deve ignorar evento duplicado")
    void handleOrderStatusChanged_duplicate() {
        when(idempotencyService.isAlreadyProcessed("evt-001")).thenReturn(true);
        OrderStatusChangedEventDTO event = buildEvent(StatusPedidoEnum.CONFIRMADO);

        orderStatusConsumer.handleOrderStatusChanged(event);

        verifyNoInteractions(emailSenderService);
        verify(idempotencyService, never()).markAsProcessed(any(), any());
    }

    @Test
    @DisplayName("Deve salvar falha e re-lançar exceção quando envio falha")
    void handleOrderStatusChanged_failure() {
        when(idempotencyService.isAlreadyProcessed("evt-001")).thenReturn(false);
        OrderStatusChangedEventDTO event = buildEvent(StatusPedidoEnum.CONFIRMADO);
        doThrow(new RuntimeException("SMTP error")).when(emailSenderService)
                .sendOrderConfirmedEmail(any(), any(), any());

        assertThrows(RuntimeException.class, () ->
                orderStatusConsumer.handleOrderStatusChanged(event));

        verify(failedNotificationService).save(
                eq("evt-001"), eq("ORDER_STATUS_CHANGED"), eq("cliente@email.com"),
                any(), eq(event), eq("SMTP error"), eq(1));
    }

}
