package com.egjarabo.agenteia.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmbeddingDemoController {

  private final EmbeddingModel embeddingModel;

  @GetMapping("/embeddings/comparar")
  public String comparar(@RequestParam String a, @RequestParam String b) {
    float[] vectorA = embeddingModel.embed(a);
    float[] vectorB = embeddingModel.embed(b);

    double similitud = similitudCoseno(vectorA, vectorB);

    return String.format(
        "Similitud entre \"%s\" y \"%s\": %.4f (dimensiones del vector: %d)",
        a, b, similitud, vectorA.length);
  }

  private double similitudCoseno(float[] a, float[] b) {
    double productoEscalar = 0, normaA = 0, normaB = 0;
    for (int i = 0; i < a.length; i++) {
      productoEscalar += a[i] * b[i];
      normaA += a[i] * a[i];
      normaB += b[i] * b[i];
    }
    return productoEscalar / (Math.sqrt(normaA) * Math.sqrt(normaB));
  }
}
