package com.egjarabo.agenteia.inventario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class InventarioTools {

  private static final Logger logger = LoggerFactory.getLogger(InventarioTools.class);

  @Tool(
      description =
          "Consulta las unidades disponibles en stock de un producto, dado su nombre en singular")
  public int consultarStock(String nombreProducto) {
    String normalizado = normalizar(nombreProducto);
    logger.debug("Parámetro de producto normalizado: [{}]", normalizado);
    int resultado =
        switch (normalizado) {
          case "teclado" -> 42;
          case "raton" -> 15;
          case "monitor" -> 3;
          default -> 0;
        };
    logger.info("Stock consultado para [{}]: {} unidades", normalizado, resultado);
    return resultado;
  }

  @Tool(description = "Consulta el precio unitario de un producto, dado su nombre en singular")
  public double consultarPrecio(String nombreProducto) {
    String normalizado = normalizar(nombreProducto);
    double precio =
        switch (normalizado) {
          case "teclado" -> 25.0;
          case "raton" -> 12.0;
          case "monitor" -> 150.0;
          default -> 0.0;
        };
    logger.info("Precio consultado para [{}]: {}€", normalizado, precio);
    return precio;
  }

  private String normalizar(String texto) {
    String limpio = texto.toLowerCase().trim();
    limpio = limpio.replace("ó", "o").replace("á", "a"); // quita tildes básicas
    if (limpio.endsWith("es")) {
      limpio = limpio.substring(0, limpio.length() - 2); // monitores -> monitor
    } else if (limpio.endsWith("s")) {
      limpio =
          limpio.substring(0, limpio.length() - 1); // ratones -> raton (ojo, no siempre perfecto)
    }
    return limpio;
  }
}
