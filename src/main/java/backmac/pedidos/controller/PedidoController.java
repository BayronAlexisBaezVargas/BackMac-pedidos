package backmac.pedidos.controller;

import backmac.pedidos.entity.EstadoPedido;
import backmac.pedidos.entity.Pedido;
import backmac.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<Pedido>> obtenerMisPedidos(@AuthenticationPrincipal Jwt jwt) {
        String usuarioId = jwt.getSubject();
        List<Pedido> pedidos = pedidoService.obtenerHistorial(usuarioId);
        return ResponseEntity.ok(pedidos);
    }

    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@AuthenticationPrincipal Jwt jwt, 
                                              @RequestHeader("Authorization") String tokenHeader) {
        String usuarioId = jwt.getSubject();
        Pedido nuevoPedido = pedidoService.procesarCompra(usuarioId, tokenHeader);
        return ResponseEntity.ok(nuevoPedido);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstado(@PathVariable Long id, 
                                                   @RequestParam String estado,
                                                   @AuthenticationPrincipal Jwt jwt) {
        String usuarioId = jwt.getSubject();
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(estado);
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado, usuarioId));
    }
}
