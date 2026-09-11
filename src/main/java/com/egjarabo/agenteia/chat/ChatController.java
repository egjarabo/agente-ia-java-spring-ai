package com.egjarabo.agenteia.chat;

import com.egjarabo.agenteia.inventario.ConsultaStockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @GetMapping("/preguntar")
  public String preguntar(@RequestParam String mensaje) {
    return chatService.responder(mensaje);
  }

  @GetMapping("/preguntar/estructurado")
  public ConsultaStockResponse preguntarEstructurado(@RequestParam String mensaje) {
    return chatService.responderEstructurado(mensaje);
  }
}
