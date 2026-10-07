package backmac.pedidos.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemCarritoDTO {
    private String productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
