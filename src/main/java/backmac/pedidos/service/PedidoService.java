package backmac.pedidos.service;

import backmac.pedidos.client.CarritoClient;
import backmac.pedidos.dto.CarritoDTO;
import backmac.pedidos.dto.ItemCarritoDTO;
import backmac.pedidos.dto.NotificacionEventDTO;
import backmac.pedidos.dto.SolicitudPagoEventDTO;
import backmac.pedidos.entity.DetallePedido;
import backmac.pedidos.entity.EstadoPedido;
import backmac.pedidos.entity.Pedido;
import backmac.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoClient carritoClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:pedidos360.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-keys.pagos:pagos.routing.key}")
    private String pagosRoutingKey;

    @Value("${app.rabbitmq.routing-keys.notificaciones:notificaciones.routing.key}")
    private String notificacionesRoutingKey;

    @Transactional
    public Pedido procesarCompra(String usuarioId, String tokenHeader) {
        // 1. Obtener carrito del usuario
        CarritoDTO carrito = carritoClient.obtenerCarrito(tokenHeader);
        
        if (carrito == null || carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío, no se puede crear el pedido.");
        }

        // 2. Crear nueva entidad Pedido
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        
        BigDecimal total = BigDecimal.ZERO;

        // 3. Transformar Items del carrito a Detalles del pedido
        for (ItemCarritoDTO item : carrito.getItems()) {
            DetallePedido detalle = new DetallePedido();
            detalle.setProductoId(item.getProductoId());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            
            pedido.addDetalle(detalle);
            
            total = total.add(item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad())));
        }

        if (carrito.getPorcentajeDescuento() != null && carrito.getPorcentajeDescuento() > 0) {
            BigDecimal factor = BigDecimal.valueOf(100 - carrito.getPorcentajeDescuento())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            total = total.multiply(factor).setScale(0, java.math.RoundingMode.HALF_UP);
        }
        
        pedido.setTotal(total);

        // 4. Guardar pedido en BD
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 5. Vaciar carrito
        carritoClient.vaciarCarrito(tokenHeader);

        // 6. Enviar notificación asíncrona PEDIDO_CREADO por RabbitMQ
        try {
            NotificacionEventDTO notif = new NotificacionEventDTO(
                    usuarioId,
                    pedidoGuardado.getId(),
                    "PEDIDO_CREADO",
                    "Tu pedido #" + pedidoGuardado.getId() + " por $" + pedidoGuardado.getTotal() + " fue creado y está pendiente de pago."
            );
            rabbitTemplate.convertAndSend(exchangeName, notificacionesRoutingKey, notif);
            log.info("[RABBITMQ-PRODUCER] Evento PEDIDO_CREADO enviado a cola de notificaciones para pedido #{}", pedidoGuardado.getId());
        } catch (Exception e) {
            log.warn("[RABBITMQ-PRODUCER] No se pudo encolar notificación PEDIDO_CREADO: {}", e.getMessage());
        }
        
        return pedidoGuardado;
    }

    @Transactional(readOnly = true)
    public SolicitudPagoEventDTO solicitarPagoEnCola(Long pedidoId, String metodoPago, String numeroTarjeta, String usuarioId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        if (!pedido.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado para pagar este pedido");
        }

        SolicitudPagoEventDTO eventoPago = new SolicitudPagoEventDTO(
                pedido.getId(),
                usuarioId,
                pedido.getTotal(),
                metodoPago != null ? metodoPago : "TARJETA_CREDITO",
                numeroTarjeta
        );

        rabbitTemplate.convertAndSend(exchangeName, pagosRoutingKey, eventoPago);
        log.info("[RABBITMQ-PRODUCER] Solicitud de pago encolada en '{}' (routingKey='{}') para pedido #{} por ${}",
                exchangeName, pagosRoutingKey, pedido.getId(), pedido.getTotal());

        return eventoPago;
    }

    public List<Pedido> obtenerHistorial(String usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    @Transactional
    public Pedido actualizarEstado(Long id, EstadoPedido nuevoEstado, String usuarioId) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        
        if (!pedido.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado para modificar este pedido");
        }
        
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizarEstadoDesdeEvento(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado para actualizar desde evento: " + id));
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }
}
