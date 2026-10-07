package backmac.pedidos.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CarritoDTO {
    private String usuarioId;
    private List<ItemCarritoDTO> items;
}
