# Agente IA — Java + Spring AI

Proyecto de aprendizaje de **IA agéntica en Java**, construido con Spring Boot y
Spring AI. Explora conceptos como llamadas a un LLM, *tool calling* (el modelo
ejecuta funciones Java reales), *bucle agéntico* (encadenar varias tools) y
**RAG** (respuestas basadas en documentación propia), todo ejecutado sobre
modelos locales con Ollama.

## Qué demuestra este proyecto

- **Chat básico con un LLM** — un endpoint que envía texto al modelo y devuelve su respuesta
- **Tool calling** — el LLM decide cuándo llamar a funciones Java reales (hora actual, consulta de stock/precio) en vez de inventar la respuesta
- **Bucle agéntico** — el modelo encadena varias herramientas en una sola pregunta (ej. consultar stock + precio + calcular total)
- **RAG (Retrieval-Augmented Generation)** — el agente responde citando documentación interna indexada, sin necesidad de reentrenar el modelo
- **Ejecución 100% local** — modelos de chat y embeddings corren en la propia máquina vía Ollama, sin coste ni dependencia de una API externa
- **Normalización de parámetros** — manejo defensivo de los argumentos que el modelo extrae del lenguaje natural (plurales, tildes, etc.)
- **Arquitectura por dominio** — Controller → Service → Tools, siguiendo el patrón "package by feature"

## Tech stack

| Tecnología | Versión | Propósito |
|---|---|---|
| Java | 21 | Lenguaje |
| Spring Boot | 4.1.1 | Framework |
| Spring AI | 2.0.1 | Integración con LLMs, tool calling y RAG |
| Lombok | Latest | Reducción de boilerplate (constructores) |
| Ollama | — | Motor de ejecución de modelos en local (chat y embeddings) |
| Maven | — | Gestión de dependencias y build |

## Modelos utilizados

| Modelo | Tipo | Notas |
|---|---|---|
| `llama3.1:8b` | Chat | Más fiable que `llama3.2` (3B) citando datos reales de las tools |
| `nomic-embed-text-v2-moe` | Embeddings | Versión **multilingüe** — la v1 (solo inglés) da resultados pobres en español |

## Arquitectura

Organización **package by feature**: un paquete por dominio de negocio.

```
com.egjarabo.agenteia
├── AgenteIaApplication         # Clase de arranque de Spring Boot
├── chat
│   ├── ChatController          # Endpoints REST: respuesta libre y respuesta estructurada
│   ├── ChatService             # Orquesta ChatClient + RAG; construye el cliente una sola vez
│   └── HoraTools                # Herramienta de propósito general (fecha/hora)
├── inventario
│   ├── ConsultaStockResponse   # Record de salida estructurada (producto, stock, precio, coste total)
│   └── InventarioTools         # Consulta de stock, precio y cálculo de coste total
└── rag
    ├── DocumentoController         # Endpoints REST de indexado y búsqueda semántica directa
    ├── DocumentoService             # Indexado y búsqueda semántica (similaritySearch)
    ├── EmbeddingDemoController      # Endpoint de utilidad: similitud coseno entre dos textos
    └── RagConfig                    # Bean del VectorStore + carga automática de documentos de prueba
```

- El **Controller** solo recibe la petición HTTP y delega en el Service (en `rag`, la responsabilidad REST se reparte entre `DocumentoController` para indexado/búsqueda y `EmbeddingDemoController` para la comparación directa de embeddings).
- **`ChatService`** combina las tres piezas del agente: tools (`defaultTools`), RAG (contexto inyectado como mensaje `system`) y la llamada al modelo — construyendo el `ChatClient` una única vez en el constructor. También expone `responderEstructurado(...)`, que usa `.entity(ConsultaStockResponse.class)` de Spring AI para forzar que la respuesta del modelo se parsee directamente a un record Java, en vez de devolver texto libre.
- **`ConsultaStockResponse`** es el record (`producto`, `unidadesDisponibles`, `precioUnitario`, `costeTotal`) que da forma a esa salida estructurada.
- **`DocumentoService`** indexa texto en el `VectorStore` y expone `buscar(...)` / `buscarComoContexto(...)` para recuperar los fragmentos más relevantes por significado.

## Herramientas disponibles (tool calling)

| Herramienta | Descripción | Parámetros |
|---|---|---|
| `obtenerHoraActual` | Devuelve la fecha y hora actual del sistema | — |
| `consultarStock` | Consulta las unidades disponibles de un producto (nombre normalizado a singular) | `nombreProducto` (String) |
| `consultarPrecio` | Consulta el precio unitario de un producto | `nombreProducto` (String) |

## Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/preguntar?mensaje={texto}` | Pregunta al agente; puede usar tools y/o contexto RAG según lo que decida necesario |
| GET | `/preguntar/estructurado?mensaje={texto}` | Igual que `/preguntar`, pero fuerza la respuesta del modelo a un JSON tipado (`ConsultaStockResponse`) en vez de texto libre |
| POST | `/documentos` | Indexa un texto en el VectorStore (body en texto plano) |
| GET | `/documentos/buscar?consulta={texto}` | Búsqueda semántica directa, sin pasar por el chat — devuelve los fragmentos más relevantes |
| GET | `/embeddings/comparar?a={texto}&b={texto}` | Utilidad de aprendizaje: devuelve la similitud numérica (coseno) entre dos textos |

Al arrancar la aplicación, un `CommandLineRunner` indexa automáticamente 4 documentos de ejemplo (vacaciones, facturas, teletrabajo, alta de servidores) — no hace falta indexarlos a mano.

## Cómo ejecutarlo

### Requisitos previos
- Java 21+
- Maven 3.9+ (o el wrapper `./mvnw` incluido en el proyecto)
- [Ollama](https://ollama.com) instalado y en ejecución, con los modelos descargados:
  ```bash
  ollama pull llama3.1:8b
  ollama pull nomic-embed-text-v2-moe
  ```

### Configuración

En `src/main/resources/application.properties`:
```properties
spring.ai.ollama.chat.options.model=llama3.1:8b
spring.ai.ollama.embedding.options.model=nomic-embed-text-v2-moe
spring.ai.ollama.base-url=http://localhost:11434
```

> **Nota — desarrollo en WSL2:** si el proyecto corre en WSL y Ollama en Windows,
> `localhost` no resuelve automáticamente hacia el host. Hay que:
> 1. Configurar la variable de entorno `OLLAMA_HOST=0.0.0.0` en Windows y reiniciar Ollama, para que escuche en todas las interfaces.
> 2. Sustituir `base-url` por la IP del host Windows vista desde WSL, obtenida con `ip route show | grep default`.
>
> Esa IP puede cambiar entre reinicios de Windows/WSL; conviene revisarla si la conexión empieza a fallar.

### Arrancar la aplicación

```bash
./mvnw spring-boot:run
```

Espera a ver 4 líneas `Documento indexado (...)` en el log antes de hacer peticiones — la indexación inicial tarda unos segundos.

### Probar el agente

```bash
# Tool calling simple
curl -G "http://localhost:8080/preguntar" --data-urlencode "mensaje=¿qué hora es?"

# Tool calling encadenado
curl -G "http://localhost:8080/preguntar" --data-urlencode "mensaje=si compro todo el stock de monitores, ¿cuánto me costaría en total?"

# RAG
curl -G "http://localhost:8080/preguntar" --data-urlencode "mensaje=¿cómo pido días libres?"

# Búsqueda semántica directa (sin pasar por el chat)
curl -G "http://localhost:8080/documentos/buscar" --data-urlencode "consulta=quiero trabajar desde casa"
```