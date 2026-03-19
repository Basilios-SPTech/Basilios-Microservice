package com.basilios.emailapi.domain.enums;

/**
 * Espelho do enum de status de pedido do monolito.
 * Mantido sincronizado para deserialização correta dos eventos.
 */
public enum StatusPedidoEnum {
    PENDENTE,
    CONFIRMADO,
    PREPARANDO,
    DESPACHADO,
    ENTREGUE,
    CANCELADO
}
