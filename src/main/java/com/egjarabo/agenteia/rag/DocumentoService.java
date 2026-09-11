package com.egjarabo.agenteia.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

  private final VectorStore vectorStore;

  public void indexar(String contenido) {
    Document documento = new Document("search_document: " + contenido);
    vectorStore.add(List.of(documento));
    log.info("Documento indexado ({} caracteres)", contenido.length());
  }

  public List<String> buscar(String consulta) {
    List<Document> resultados = vectorStore.similaritySearch("search_query: " + consulta);
    resultados.forEach(
        doc ->
            log.info(
                "Score: {} | Texto: {}",
                doc.getScore(),
                doc.getText().substring(0, Math.min(50, doc.getText().length()))));
    return resultados.stream()
        .map(doc -> doc.getText().replaceFirst("^search_document: ", ""))
        .toList();
  }

  public String buscarComoContexto(String consulta) {
    List<String> resultados = buscar(consulta);
    return String.join("\n", resultados);
  }
}
