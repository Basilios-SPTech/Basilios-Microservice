package com.basilios.emailapi.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Contrato V1 do evento de solicitação de reset de senha.
 * Publicado pelo monolito no RabbitMQ, consumido por este microserviço.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetRequestedEventDTO {

    @NotBlank
    private String eventId;

    private String eventType;

    private Integer eventVersion;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String resetUrl;

    private String userName;

    private String expiresIn;

    private LocalDateTime occurredAt;

    private String source;

}
