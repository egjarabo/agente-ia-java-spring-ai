package com.egjarabo.agenteia.chat;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class HoraTools {

  @Tool(description = "Devuelve la fecha y hora actual")
  public String obtenerHoraActual() {
    return LocalDateTime.now().toString();
  }
}
