package backmac.pedidos.client;

import backmac.pedidos.dto.CarritoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-carrito", url = "${servicios.carrito.url}")
public interface CarritoClient {

    @GetMapping
    CarritoDTO obtenerCarrito(@RequestHeader("Authorization") String token);

    @DeleteMapping
    void vaciarCarrito(@RequestHeader("Authorization") String token);
}
