package com.egjarabo.agenteia.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DocumentoController {

  private final DocumentoService documentoService;

  @PostMapping("/documentos")
  public String indexar(@RequestBody String contenido) {
    documentoService.indexar(contenido);
    return "Documento indexado";
  }

  @GetMapping("/documentos/buscar")
  public String buscar(@RequestParam String consulta) {
    return String.join("\n---\n", documentoService.buscar(consulta));
  }
}
