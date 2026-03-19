package com.basilios.emailapi.application.dto;

import com.basilios.emailapi.domain.enums.StatusPedidoEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Contrato V1 do evento de mudança de status de pedido.
 * Publicado pelo monolito no RabbitMQ, consumido por este microserviço.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusChangedEventDTO {

    @NotBlank
    private String eventId;

    private String eventType;

    private Integer eventVersion;

    @NotNull
    private Long orderId;

    @NotBlank
    private String orderCode;

    private StatusPedidoEnum oldStatus;

    @NotNull
    private StatusPedidoEnum newStatus;

    @NotBlank
    @Email
    private String clientEmail;

    @NotBlank
    private String clientName;

    private String motivo;

    private LocalDateTime occurredAt;

    private String source;

}
