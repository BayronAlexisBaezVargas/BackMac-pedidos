package backmac.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPagoEventDTO {
    private Long pedidoId;
    private String usuarioId;
    private BigDecimal monto;
    private String metodoPago;
    private String numeroTarjeta;
}
