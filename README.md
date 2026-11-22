# Documentación Técnica: Microservicio de Pedidos (ms-pedidos)

## Descripción General
El microservicio `ms-pedidos` gestiona el ciclo de vida de las órdenes de compra.

## Arquitectura y Tecnologías
- **Lenguaje:** Java 21
- **Framework:** Spring Boot
- **Persistencia:** PostgreSQL
- **Seguridad:** Autenticación y Autorización basada en JWT.

## Cambios Recientes
### Versión 1.4.1
- Alineación de dependencias en `pom.xml` a Spring Boot `3.3.4` y Spring Cloud `2023.0.3` (reemplazo de starters por `spring-boot-starter-web` y `spring-boot-starter-test`) para compilación estable en Docker y AWS EC2.

### Versión 1.4.0
- Implementación de arquitectura asíncrona multi-cola en **RabbitMQ** (`RabbitMQConfig.java`):
  - Productor de solicitudes de cobro hacia `pagos.queue` (`POST /api/pedidos/{id}/pagar`) y alertas `PEDIDO_CREADO` hacia `notificaciones.queue`.
  - Consumidor resiliente (`PedidoEstadoRabbitConsumer.java`) sobre `pedidos.estado.queue` con confirmación manual (`ACK`/`NACK`) y desvío a Dead Letter Queue (`pedidos.estado.dlq`) para actualizar el estado del pedido (`EN_PREPARACION` o `CANCELADO`).
- Soporte dual para ejecución local vía `docker-compose.yml` y despliegue aislado por `Dockerfile` en AWS EC2 (puerto HTTP `8083`, PostgreSQL embebido `5435`).

### Versión 1.3.1
- Integración completa con `ms-carrito` mediante Feign Client para vaciado automático y consulta de ítems al crear pedidos.
- Adaptación para despliegue Cloud Native en AWS Academy con Dockerfile multi-stage y PostgreSQL embebido.
- Compatibilidad con enrutamiento de métodos REST y CORS en AWS API Gateway.

### Versión 1.2.1
- Se añadió un `Dockerfile` optimizado para el despliegue del servicio.
