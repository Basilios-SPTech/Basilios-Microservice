package com.basilios.emailapi.infra.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ: exchanges, queues, bindings e DLQ.
 *
 * Topologia:
 *   Exchange: basilios.notifications (topic)
 *   Queues:
 *     - email.order-status     (routing key: order.status.*)
 *     - email.password-reset   (routing key: auth.password-reset)
 *   DLQ:
 *     - email.order-status.dlq
 *     - email.password-reset.dlq
 */
@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NOTIFICATIONS = "basilios.notifications";

    // Queues
    public static final String QUEUE_ORDER_STATUS = "email.order-status";
    public static final String QUEUE_PASSWORD_RESET = "email.password-reset";

    // DLQ
    public static final String QUEUE_ORDER_STATUS_DLQ = "email.order-status.dlq";
    public static final String QUEUE_PASSWORD_RESET_DLQ = "email.password-reset.dlq";

    // Routing keys
    public static final String ROUTING_KEY_ORDER_STATUS = "order.status.#";
    public static final String ROUTING_KEY_PASSWORD_RESET = "auth.password-reset";

    // ========== Exchange ==========

    @Bean
    public TopicExchange notificationsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NOTIFICATIONS)
                .durable(true)
                .build();
    }

    // ========== Queues ==========

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_STATUS)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_ORDER_STATUS_DLQ)
                .build();
    }

    @Bean
    public Queue passwordResetQueue() {
        return QueueBuilder.durable(QUEUE_PASSWORD_RESET)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_PASSWORD_RESET_DLQ)
                .build();
    }

    @Bean
    public Queue orderStatusDlq() {
        return QueueBuilder.durable(QUEUE_ORDER_STATUS_DLQ).build();
    }

    @Bean
    public Queue passwordResetDlq() {
        return QueueBuilder.durable(QUEUE_PASSWORD_RESET_DLQ).build();
    }

    // ========== Bindings ==========

    @Bean
    public Binding orderStatusBinding(Queue orderStatusQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(orderStatusQueue)
                .to(notificationsExchange)
                .with(ROUTING_KEY_ORDER_STATUS);
    }

    @Bean
    public Binding passwordResetBinding(Queue passwordResetQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(passwordResetQueue)
                .to(notificationsExchange)
                .with(ROUTING_KEY_PASSWORD_RESET);
    }

    // ========== Serialização JSON ==========

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(10);
        return factory;
    }

}
