package com.egjarabo.agenteia.chat;

import com.egjarabo.agenteia.inventario.ConsultaStockResponse;
import com.egjarabo.agenteia.inventario.InventarioTools;
import com.egjarabo.agenteia.rag.DocumentoService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final ChatClient chatClient;
  private final DocumentoService documentoService;

  public ChatService(
      ChatClient.Builder builder,
      HoraTools horaTools,
      InventarioTools inventarioTools,
      DocumentoService documentoService) {
    this.chatClient = builder.defaultTools(horaTools, inventarioTools).build();
    this.documentoService = documentoService;
  }

  public String responder(String mensaje) {
    String contexto = documentoService.buscarComoContexto(mensaje);

    return chatClient
        .prompt()
        .system(
            "Usa el siguiente contexto de la documentación interna si es relevante para responder. "
                + "Si el contexto no tiene relación con la pregunta, ignóralo y responde con tus herramientas o tu conocimiento general.\n\n"
                + "Contexto:\n"
                + contexto)
        .user(mensaje)
        .call()
        .content();
  }

  public ConsultaStockResponse responderEstructurado(String mensaje) {
    return chatClient.prompt().user(mensaje).call().entity(ConsultaStockResponse.class);
  }
}
