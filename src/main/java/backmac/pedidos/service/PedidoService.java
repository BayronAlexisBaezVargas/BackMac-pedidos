package backmac.pedidos.service;

import backmac.pedidos.client.CarritoClient;
import backmac.pedidos.dto.CarritoDTO;
import backmac.pedidos.dto.ItemCarritoDTO;
import backmac.pedidos.entity.DetallePedido;
import backmac.pedidos.entity.EstadoPedido;
import backmac.pedidos.entity.Pedido;
import backmac.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoClient carritoClient;

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
        
        pedido.setTotal(total);

        // 4. Guardar pedido en BD
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 5. Vaciar carrito
        carritoClient.vaciarCarrito(tokenHeader);

        // Opcional: Llamar a ms-productos para descontar stock (usando otro cliente Feign)
        
        return pedidoGuardado;
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
}
