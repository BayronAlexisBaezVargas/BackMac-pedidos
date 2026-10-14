# Documentación Técnica: Microservicio de Pedidos (ms-pedidos)

## Descripción General
El microservicio `ms-pedidos` gestiona el ciclo de vida de las órdenes de compra.

## Arquitectura y Tecnologías
- **Lenguaje:** Java 21
- **Framework:** Spring Boot
- **Persistencia:** PostgreSQL
- **Seguridad:** Autenticación y Autorización basada en JWT.

## Cambios Recientes
### Versión 1.3.1
- Integración completa con `ms-carrito` mediante Feign Client para vaciado automático y consulta de ítems al crear pedidos.
- Adaptación para despliegue Cloud Native en AWS Academy con Dockerfile multi-stage y PostgreSQL embebido.
- Compatibilidad con enrutamiento de métodos REST y CORS en AWS API Gateway.

### Versión 1.2.1
- Se añadió un `Dockerfile` optimizado para el despliegue del servicio.
