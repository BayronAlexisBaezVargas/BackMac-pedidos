package backmac.pedidos.consumer;

import backmac.pedidos.dto.ActualizarEstadoPedidoEventDTO;
import backmac.pedidos.entity.EstadoPedido;
import backmac.pedidos.service.PedidoService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoEstadoConsumer {

    private final PedidoService pedidoService;

    @RabbitListener(queues = "${app.rabbitmq.queues.pedidos-estado:pedidos.estado.queue}")
    public void recibirActualizacionEstado(ActualizarEstadoPedidoEventDTO dto,
                                           Channel channel,
                                           @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[RABBITMQ-CONSUMER] Actualización de estado recibida desde ms-pagos: Pedido={}, NuevoEstado={}, Transaccion={}",
                    dto.getPedidoId(), dto.getNuevoEstado(), dto.getCodigoTransaccion());

            if (dto.getPedidoId() == null || dto.getNuevoEstado() == null) {
                throw new IllegalArgumentException("Payload inválido en evento de actualización de estado de pedido");
            }

            EstadoPedido nuevoEstado = EstadoPedido.valueOf(dto.getNuevoEstado());
            pedidoService.actualizarEstadoDesdeEvento(dto.getPedidoId(), nuevoEstado);

            channel.basicAck(deliveryTag, false);
            log.info("[RABBITMQ-CONSUMER] ACK enviado con éxito tras actualizar estado del pedido ID: {} a {}",
                    dto.getPedidoId(), nuevoEstado);

        } catch (Exception e) {
            log.error("[RABBITMQ-CONSUMER] Error actualizando estado del pedido: {}. Enviando a DLQ...", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
