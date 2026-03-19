package com.basilios.emailapi.infra.messaging;

import com.basilios.emailapi.application.dto.OrderStatusChangedEventDTO;
import com.basilios.emailapi.application.service.FailedNotificationService;
import com.basilios.emailapi.application.service.IdempotencyService;
import com.basilios.emailapi.infra.config.RabbitMQConfig;
import com.basilios.emailapi.infra.email.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer RabbitMQ para eventos de mudança de status de pedido.
 * Recebe o evento, verifica idempotência e dispara o email correspondente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusConsumer {

    private final EmailSenderService emailSenderService;
    private final IdempotencyService idempotencyService;
    private final FailedNotificationService failedNotificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_STATUS)
    public void handleOrderStatusChanged(OrderStatusChangedEventDTO event) {
        log.info("Evento recebido: orderId={}, orderCode={}, {} → {}",
                event.getOrderId(), event.getOrderCode(),
                event.getOldStatus(), event.getNewStatus());

        // Idempotência: verifica se já foi processado
        if (idempotencyService.isAlreadyProcessed(event.getEventId())) {
            log.warn("Evento duplicado ignorado: eventId={}", event.getEventId());
            return;
        }

        try {
            String email = event.getClientEmail();
            String clientName = event.getClientName();
            String orderCode = event.getOrderCode();

            switch (event.getNewStatus()) {
                case CONFIRMADO -> emailSenderService.sendOrderConfirmedEmail(email, clientName, orderCode);
                case PREPARANDO -> emailSenderService.sendOrderPreparingEmail(email, clientName, orderCode);
                case DESPACHADO -> emailSenderService.sendOrderDispatchedEmail(email, clientName, orderCode);
                case ENTREGUE -> emailSenderService.sendOrderDeliveredEmail(email, clientName, orderCode);
                case CANCELADO -> emailSenderService.sendOrderCancelledEmail(email, clientName, orderCode, event.getMotivo());
                default -> log.debug("Status {} não requer notificação por email", event.getNewStatus());
            }

            idempotencyService.markAsProcessed(event.getEventId(), "ORDER_STATUS_CHANGED");
            log.info("Notificação enviada com sucesso para {} (pedido {})", email, orderCode);

        } catch (Exception e) {
            log.error("Erro ao processar notificação de pedido: eventId={}, erro={}",
                    event.getEventId(), e.getMessage());

            failedNotificationService.save(
                    event.getEventId(),
                    "ORDER_STATUS_CHANGED",
                    event.getClientEmail(),
                    "Notificação de pedido " + event.getOrderCode(),
                    event,
                    e.getMessage(),
                    1
            );

            // Re-throw para que a mensagem vá para DLQ
            throw e;
        }
    }

}
