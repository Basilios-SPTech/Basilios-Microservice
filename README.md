# Basilios Email API

Microsserviço de notificações por e-mail do **Basilios** (status de pedido e reset de senha), consumindo eventos via RabbitMQ com idempotência e registro de falhas.

Repositórios relacionados: [basilios-api](https://github.com/Basilios-SPTech/basilios-api) · [basilios-infra](https://github.com/Basilios-SPTech/basilios-infra)

## Arquitetura

```
email-api/
├── src/main/java/com/basilios/emailapi/
│   ├── EmailApiApplication.java          # Bootstrap Spring Boot
│   ├── api/                              # Controllers REST
│   │   └── FailedNotificationController  # Consulta de falhas
│   ├── application/                      # Camada de aplicação
│   │   ├── dto/                          # Contratos de evento
│   │   │   ├── OrderStatusChangedEventDTO
│   │   │   └── PasswordResetRequestedEventDTO
│   │   └── service/                      # Serviços de aplicação
│   │       ├── IdempotencyService        # Controle de duplicidade
│   │       └── FailedNotificationService # Registro de falhas
│   ├── domain/                           # Domínio
│   │   ├── enums/
│   │   │   └── StatusPedidoEnum
│   │   └── model/
│   │       ├── ProcessedEvent            # Idempotência
│   │       └── FailedNotification        # Falhas persistidas
│   └── infra/                            # Infraestrutura
│       ├── config/
│       │   └── RabbitMQConfig            # Exchanges, queues, DLQ
│       ├── email/
│       │   └── EmailSenderService        # Envio SMTP
│       ├── messaging/
│       │   ├── OrderStatusConsumer       # Consumer de pedidos
│       │   └── PasswordResetConsumer     # Consumer de reset
│       └── repository/
│           ├── ProcessedEventRepository
│           └── FailedNotificationRepository
└── src/test/java/                        # Testes unitários
```

## Tecnologias

- Java 21
- Spring Boot 3.5.4
- RabbitMQ (AMQP)
- MySQL
- Spring Mail (SMTP)
- Spring Actuator (health, metrics)
- Spring Retry

## Eventos Consumidos

| Evento | Queue | Routing Key |
|--------|-------|-------------|
| Mudança de status de pedido | `email.order-status` | `order.status.#` |
| Reset de senha | `email.password-reset` | `auth.password-reset` |

## Executando

```bash
# Desenvolvimento local (requer RabbitMQ e MySQL rodando)
cd email-api
./mvnw spring-boot:run

# Via Docker Compose (no projeto basilios-infra)
docker compose up -d rabbitmq email-api
```

## Health Check

```
GET http://localhost:8081/actuator/health
```

## Licença

MIT — veja [LICENSE](LICENSE).

---

Mantido sob a organização [Basilios-SPTech](https://github.com/Basilios-SPTech).
