package backmac.pedidos.dto;

import lombok.Data;
import java.util.List;

@Data
public class CarritoDTO {
    private String usuarioId;
    private String codigoCupon;
    private Integer porcentajeDescuento;
    private List<ItemCarritoDTO> items;
}
