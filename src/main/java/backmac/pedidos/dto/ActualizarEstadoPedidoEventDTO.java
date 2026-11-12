package backmac.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEstadoPedidoEventDTO {
    private Long pedidoId;
    private String usuarioId;
    private String nuevoEstado;
    private String codigoTransaccion;
}
