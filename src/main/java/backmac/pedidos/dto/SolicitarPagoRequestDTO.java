package backmac.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitarPagoRequestDTO {
    private String metodoPago;
    private String numeroTarjeta;
}
