package com.egjarabo.agenteia.chat;

import com.egjarabo.agenteia.inventario.ConsultaStockResponse;
import com.egjarabo.agenteia.inventario.InventarioTools;
import com.egjarabo.agenteia.rag.DocumentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final ChatClient chatClient;
  private final DocumentoService documentoService;

  private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

  public ChatService(
      ChatClient.Builder builder,
      HoraTools horaTools,
      InventarioTools inventarioTools,
      DocumentoService documentoService) {
    this.chatClient = builder.defaultTools(horaTools, inventarioTools).build();
    this.documentoService = documentoService;
  }

  public String responder(String mensaje) {
    try {
      String contexto = documentoService.buscarComoContexto(mensaje);
      logger.info("Contexto recuperado para [{}]: {}", mensaje, contexto);

      return chatClient
          .prompt()
          .system(
              """
        Eres el asistente interno de la empresa para consultas de RRHH e inventario.

        REGLA DE PRIORIDAD: si el "Contexto de documentación interna" de abajo contiene
        información relevante para la pregunta, respóndela usando ese contexto — sin
        importar de qué tema general trate (RRHH, legislación, políticas, etc.).
        Solo si el contexto NO cubre la pregunta, entonces di explícitamente:
        "No tengo información interna sobre esto, te recomiendo consultarlo con RRHH."

        No respondas con conocimiento general propio sobre temas que no estén en el
        contexto ni cubiertos por tus herramientas — no inventes ni completes.

        Usa siempre las herramientas disponibles para datos de stock, precios o cálculos;
        nunca calcules manualmente si existe una herramienta para ello.

        Responde de forma breve y profesional.

        Contexto de documentación interna:
        """
                  + contexto)
          .user(mensaje)
          .call()
          .content();
    } catch (Exception e) {
      logger.error("Error al procesar la pregunta [{}]: {}", mensaje, e.getMessage());
      return "Lo siento, ha ocurrido un problema al procesar tu pregunta. Inténtalo de nuevo en unos momentos.";
    }
  }

  public ConsultaStockResponse responderEstructurado(String mensaje) {
    return chatClient.prompt().user(mensaje).call().entity(ConsultaStockResponse.class);
  }
}
