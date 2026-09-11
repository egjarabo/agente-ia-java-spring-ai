package com.egjarabo.agenteia.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RagConfig {

  @Bean
  public VectorStore vectorStore(EmbeddingModel embeddingModel) {
    return SimpleVectorStore.builder(embeddingModel).build();
  }

  @Bean
  public CommandLineRunner cargarDocumentosDePrueba(DocumentoService documentoService) {
    return args -> {
      documentoService.indexar(
          "Los empleados pueden solicitar sus vacaciones a través del portal interno de recursos humanos, con un mínimo de una semana de antelación.");
      documentoService.indexar(
          "Las facturas de proveedores deben aprobarse por el departamento financiero antes de proceder a su pago, en un plazo máximo de 30 días.");
      documentoService.indexar(
          "El teletrabajo está permitido hasta 3 días por semana, previa autorización del responsable de equipo.");
      documentoService.indexar(
          "Para dar de alta un nuevo servidor en producción es necesario abrir un ticket en el sistema de infraestructura.");
    };
  }
}
