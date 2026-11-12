package backmac.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEventDTO {
    private String usuarioId;
    private Long pedidoId;
    private String tipo;
    private String mensaje;
}
