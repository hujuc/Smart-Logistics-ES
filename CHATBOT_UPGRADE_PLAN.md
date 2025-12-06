# Chatbot Next-Level Upgrade Plan

**Timeline**: 3-5 days
**Sprint**: Sprint 6
**Status**: Planning
**Date**: 2025-12-04

---

## 🎯 Objectives

Based on current implementation analysis and priority assessment, we will upgrade the SLMS chatbot with the following capabilities:

1. **Database Access Tool** - Enable read-only queries for orders, shipments, warehouse data
2. **Streaming Responses** - Real-time typing effect for better UX
3. **Enhanced RAG Documents** - Better chunking + technical docs + auto-sync from codebase
4. **Proactive Suggestions** - Context-aware help based on current route (stretch goal)

---

## 📊 Current State Analysis

### Strengths
- Well-architected RAG system with Gemini + Pinecone
- Multi-layer filtering (role, frontend, route, locale)
- 40+ seeded documents across 9 categories
- 6 role-specific BFF endpoints
- Conversation history in localStorage
- Clean separation of concerns (integration → service → controller)

### Limitations
- **No database access**: Chatbot can't answer "What's the status of order #12345?"
- **Blocking responses**: User waits for full response (poor UX for long answers)
- **Large document chunks**: 40 documents but chunking not optimized for retrieval
- **No technical docs**: Missing API documentation, data schemas, integration guides
- **Static help only**: Can't provide context-aware proactive suggestions
- **Manual doc management**: No auto-sync from codebase

### Technical Debt
- `DocumentManagementService.java:124` - TODO: Delete from Pinecone (Pinecone client lacks delete method)
- Frontend component duplication (53 TSX files across 13 directories)
- Hardcoded system prompts, temperature, maxTokens
- No rate limiting on chat endpoints
- No caching layer for embeddings/queries

---

## 📋 Phase 1: Database Access via Tool Calling

**Duration**: 1.5-2 days
**Priority**: HIGH

### 1.1 Tool System Infrastructure

**Files to Create**:
- `src/backend/.../chatbot_module/tools/Tool.java`
- `src/backend/.../chatbot_module/tools/ToolRegistry.java`
- `src/backend/.../chatbot_module/tools/ToolExecutor.java`
- `src/backend/.../chatbot_module/dto/ToolCall.java`
- `src/backend/.../chatbot_module/dto/ToolDefinition.java`
- `src/backend/.../chatbot_module/dto/ToolResult.java`

**Base Tool Interface**:
```java
public interface Tool {
    String getName();
    String getDescription();
    JsonNode getParametersSchema();  // JSON Schema for parameters
    ToolResult execute(Map<String, Object> params, UserContext context);
    Set<String> getAllowedRoles();   // Role-based access control
}
```

**Tool Registry**:
- Discover and register all tools at startup (Spring component scanning)
- Filter tools by user role
- Generate tool definitions for Gemini API

**Tool Executor**:
- Validate tool existence and user authorization
- Parse and validate parameters against JSON schema
- Execute tool with error handling
- Format results for LLM consumption
- Audit logging for compliance

### 1.2 Database Query Tools

**OrderLookupTool**
- **Description**: Look up order details by ID or tracking number
- **Parameters**: `orderId` (UUID) OR `trackingNumber` (string)
- **Returns**: Order details (status, customer, addresses, items)
- **Allowed Roles**: CUSTOMER (own orders), WAREHOUSE_STAFF, MANAGER, CUSTOMER_SERVICE
- **Implementation**: Calls `OrderService.getOrderById()` or `OrderService.getOrderByTrackingNumber()`

**OrderStatusTool**
- **Description**: Check current status and event history of an order
- **Parameters**: `orderId` (UUID)
- **Returns**: Current status + recent events (last 5)
- **Allowed Roles**: CUSTOMER (own orders), WAREHOUSE_STAFF, MANAGER, CUSTOMER_SERVICE, DRIVER
- **Implementation**: Calls `OrderEventService.getOrderEvents()`

**WarehouseInventoryTool**
- **Description**: Query warehouse stock levels and capacity
- **Parameters**: `warehouseId` (UUID) OR `warehouseName` (string)
- **Returns**: Warehouse details, current capacity, active orders count
- **Allowed Roles**: WAREHOUSE_STAFF, MANAGER
- **Implementation**: Calls `WarehouseService.getWarehouseById()` or `WarehouseService.getAllWarehouses()`

**ShipmentTrackingTool**
- **Description**: Get real-time shipment tracking information
- **Parameters**: `shipmentId` (UUID) OR `trackingNumber` (string)
- **Returns**: Carrier info, current location, delivery ETA
- **Allowed Roles**: CUSTOMER (own shipments), DRIVER (assigned), CUSTOMER_SERVICE, MANAGER
- **Implementation**: Calls `ShipmentService.getShipmentById()` (from rate_carrier_module)

**DeliveryExceptionTool**
- **Description**: List active delivery exceptions/issues
- **Parameters**: `orderId` (UUID, optional), `severity` (enum, optional)
- **Returns**: List of exceptions with details
- **Allowed Roles**: CUSTOMER (own orders), CUSTOMER_SERVICE, MANAGER
- **Implementation**: Calls `DeliveryExceptionService.getExceptions()`

### 1.3 Update ChatbotService

**New Tool Calling Flow**:

```java
public ChatResponse generateResponse(ChatRequest request, String frontend, String role) {
    // 1. Get available tools for user role
    List<ToolDefinition> availableTools = toolRegistry.getToolsForRole(role);

    // 2. Generate embedding + RAG context (existing)
    float[] queryEmbedding = embeddingService.generateEmbedding(request.message());
    List<Match> relevantDocs = ragService.queryRelevantDocuments(...);
    String augmentedPrompt = ragService.augmentPrompt(request.message(), relevantDocs);

    // 3. Call Gemini with tools available
    GeminiResponse response = geminiClient.generateCompletionWithTools(
        systemPrompt,
        messages,
        availableTools
    );

    // 4. Check if LLM requested tool calls
    if (response.hasToolCalls()) {
        // Execute each tool call
        List<ToolResult> results = new ArrayList<>();
        for (ToolCall toolCall : response.getToolCalls()) {
            ToolResult result = toolExecutor.execute(
                toolCall.getName(),
                toolCall.getArguments(),
                new UserContext(role, userId)
            );
            results.add(result);
        }

        // 5. Send tool results back to LLM for final answer
        response = geminiClient.generateCompletionWithToolResults(
            systemPrompt,
            messages,
            response.getToolCalls(),
            results
        );
    }

    // 6. Return final response
    return new ChatResponse(response.getText(), sourceDocIds);
}
```

### 1.4 Update Gemini Client

**Add Function Calling Support**:

```java
public GeminiResponse generateCompletionWithTools(
    String systemPrompt,
    List<Map<String, String>> messages,
    List<ToolDefinition> tools
) {
    // Build request with tools
    GeminiChatRequest request = new GeminiChatRequest(
        contents,
        generationConfig,
        tools  // NEW: Include tool definitions
    );

    // Parse response for tool calls
    GeminiChatResponse response = restTemplate.postForObject(url, request, GeminiChatResponse.class);

    // Check if response contains function calls
    if (response.hasFunctionCalls()) {
        return GeminiResponse.withToolCalls(response.getFunctionCalls());
    }

    return GeminiResponse.withText(response.getText());
}
```

**Gemini Function Calling Format**:
```json
{
  "tools": [
    {
      "functionDeclarations": [
        {
          "name": "order_lookup",
          "description": "Look up order details by ID or tracking number",
          "parameters": {
            "type": "object",
            "properties": {
              "orderId": {
                "type": "string",
                "description": "The UUID of the order"
              },
              "trackingNumber": {
                "type": "string",
                "description": "The tracking number of the order"
              }
            },
            "required": []
          }
        }
      ]
    }
  ]
}
```

### 1.5 Testing

**Unit Tests**:
- `ToolRegistryTest` - Tool discovery and filtering
- `ToolExecutorTest` - Parameter validation and execution
- Each tool: Mock service dependencies, test happy path + error cases

**Integration Tests**:
- `ChatbotToolIntegrationTest` - End-to-end tool calling flow
- Test with real database queries (test data)
- Verify role-based access control
- Test error handling (invalid IDs, unauthorized access)

**Example Test Cases**:
```java
@Test
void customerCanLookupOwnOrder() {
    // Customer asks "What's the status of my order #12345?"
    ChatRequest request = new ChatRequest("What's the status of my order #12345?", ...);
    ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER");

    // Verify tool was called and response contains order details
    assertThat(response.response()).contains("Order #12345", "IN_TRANSIT");
}

@Test
void customerCannotAccessOtherCustomerOrders() {
    // Customer asks about order they don't own
    ChatRequest request = new ChatRequest("What's the status of order abc-123?", ...);

    // Should return error or "not found"
    ChatResponse response = chatbotService.generateResponse(request, "customer-portal", "CUSTOMER");
    assertThat(response.response()).doesNotContain("sensitive data");
}
```

---

## 📋 Phase 2: Streaming Responses

**Duration**: 1-1.5 days
**Priority**: HIGH

### 2.1 Update GeminiClient for Streaming

**Add Streaming Method**:

```java
@Component
public class GeminiClient {

    private final WebClient webClient;  // Switch from RestTemplate

    /**
     * Generate completion with streaming response.
     * Returns a Flux of text chunks as they arrive from Gemini.
     */
    public Flux<String> generateCompletionStream(
        String systemPrompt,
        List<Map<String, String>> messages,
        List<ToolDefinition> tools
    ) {
        String url = String.format(GEMINI_CHAT_URL, model, apiKey);

        GeminiChatRequest request = new GeminiChatRequest(
            contents,
            generationConfig,
            tools
        );

        return webClient
            .post()
            .uri(url)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(String.class)  // Stream of SSE events
            .map(this::parseStreamChunk)
            .filter(chunk -> chunk != null && !chunk.isEmpty());
    }

    private String parseStreamChunk(String sseEvent) {
        // Parse "data: {...}" format
        // Extract text from Gemini response chunk
        // Handle function calls mid-stream
    }
}
```

**Switch to WebClient**:
- Replace RestTemplate with WebClient for reactive support
- Configure timeouts and error handling
- Parse Server-Sent Events from Gemini

### 2.2 Create Streaming BFF Endpoints

**Update All 6 Chat Controllers**:

```java
@GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<ChatStreamEvent>> chatStream(
    @RequestParam String message,
    @RequestParam(required = false) String conversationId,
    @AuthenticationPrincipal Jwt jwt
) {
    String role = JwtSecurityUtils.extractPrimaryRole(jwt);
    String userId = JwtSecurityUtils.extractUserId(jwt);

    log.info("Streaming chat request - userId: {}, role: {}", userId, role);

    return chatbotService
        .generateResponseStream(request, FRONTEND, role, userId)
        .map(event -> ServerSentEvent.<ChatStreamEvent>builder()
            .event(event.getType())
            .data(event)
            .build()
        )
        .onErrorResume(error -> {
            log.error("Stream error: {}", error.getMessage());
            return Flux.just(ServerSentEvent.<ChatStreamEvent>builder()
                .event("error")
                .data(new ChatStreamEvent("error", error.getMessage()))
                .build()
            );
        });
}
```

**Stream Event Types**:
```java
public record ChatStreamEvent(
    String type,  // "token", "tool_call", "tool_result", "done", "error"
    String data,
    Map<String, Object> metadata
) {}
```

### 2.3 Add StreamingChatbotService

**Orchestrate Streaming Flow**:

```java
@Service
public class StreamingChatbotService {

    public Flux<ChatStreamEvent> generateResponseStream(
        ChatRequest request,
        String frontend,
        String role,
        String userId
    ) {
        return Flux.create(sink -> {
            try {
                // 1. RAG retrieval (non-streaming)
                float[] embedding = embeddingService.generateEmbedding(request.message());
                List<Match> docs = ragService.queryRelevantDocuments(...);
                String augmentedPrompt = ragService.augmentPrompt(request.message(), docs);

                // 2. Get tools
                List<ToolDefinition> tools = toolRegistry.getToolsForRole(role);

                // 3. Start streaming from Gemini
                geminiClient
                    .generateCompletionStream(systemPrompt, messages, tools)
                    .subscribe(
                        chunk -> {
                            // Check if chunk is tool call request
                            if (isToolCall(chunk)) {
                                // Emit tool call event
                                ToolCall toolCall = parseToolCall(chunk);
                                sink.next(new ChatStreamEvent("tool_call", toolCall));

                                // Execute tool
                                ToolResult result = toolExecutor.execute(...);
                                sink.next(new ChatStreamEvent("tool_result", result));

                                // Continue streaming with tool result
                                continueStreamWithToolResult(...).subscribe(sink::next);
                            } else {
                                // Regular text chunk
                                sink.next(new ChatStreamEvent("token", chunk));
                            }
                        },
                        error -> sink.error(error),
                        () -> {
                            sink.next(new ChatStreamEvent("done", ""));
                            sink.complete();
                        }
                    );

            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
```

### 2.4 Update Frontend API Client

**Add Streaming Support** (`chatbot-api.ts`):

```typescript
export async function* sendChatMessageStream(
  endpoint: string,
  request: ChatRequest,
  skipAuth: boolean = false
): AsyncGenerator<ChatStreamEvent, void, unknown> {
  const token = skipAuth ? null : getAccessToken();

  const response = await fetch(`${endpoint}/stream?message=${encodeURIComponent(request.message)}`, {
    method: 'GET',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error(`Stream request failed: ${response.status}`);
  }

  const reader = response.body?.getReader();
  const decoder = new TextDecoder();

  if (!reader) {
    throw new Error('Response body is not readable');
  }

  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();

    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() || '';

    for (const line of lines) {
      if (line.startsWith('data: ')) {
        const data = line.slice(6);
        if (data === '[DONE]') break;

        try {
          const event = JSON.parse(data) as ChatStreamEvent;
          yield event;
        } catch (e) {
          console.error('Failed to parse SSE event:', e);
        }
      }
    }
  }
}

export interface ChatStreamEvent {
  type: 'token' | 'tool_call' | 'tool_result' | 'done' | 'error';
  data: string;
  metadata?: Record<string, any>;
}
```

### 2.5 Update React Components

**Update useChatbot Hook**:

```typescript
const sendMessage = async (message: string) => {
  setIsTyping(true);
  setError(null);

  // Add user message immediately
  const userMessage: ChatMessage = { role: 'user', content: message, timestamp: new Date().toISOString() };
  addMessage(userMessage);

  try {
    // Create placeholder for assistant message
    let assistantContent = '';
    const assistantMessage: ChatMessage = { role: 'assistant', content: '', timestamp: new Date().toISOString() };

    // Stream response
    for await (const event of sendChatMessageStream(chatEndpoint, { message, conversationHistory: [] })) {
      if (event.type === 'token') {
        assistantContent += event.data;
        updateLastMessage({ ...assistantMessage, content: assistantContent });
      } else if (event.type === 'tool_call') {
        // Show "Using tool: order_lookup..." indicator
        setToolCallStatus(`Using tool: ${event.metadata?.toolName}...`);
      } else if (event.type === 'tool_result') {
        setToolCallStatus(null);
      } else if (event.type === 'done') {
        break;
      } else if (event.type === 'error') {
        setError(event.data);
        break;
      }
    }

  } catch (err) {
    setError('Failed to send message');
  } finally {
    setIsTyping(false);
  }
};
```

**Update ChatMessage Component**:

```typescript
export function ChatMessage({ message, isStreaming }: ChatMessageProps) {
  return (
    <div className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'} mb-4`}>
      <div className={`max-w-[80%] rounded-lg px-4 py-2 ${...}`}>
        <div className="text-sm whitespace-pre-wrap">
          {message.content}
          {isStreaming && <span className="animate-pulse">▊</span>}
        </div>
      </div>
    </div>
  );
}
```

### 2.6 Testing

**Backend Tests**:
- `StreamingChatbotServiceTest` - Mock Gemini stream, verify events emitted
- `ChatBffControllerStreamTest` - Test SSE endpoint with WebTestClient

**Frontend Tests**:
- Test streaming API client with mock SSE responses
- Test React component updates with streaming messages
- Test error handling (stream interruption, network failure)

---

## 📋 Phase 3: Enhanced RAG Documents

**Duration**: 1.5-2 days
**Priority**: HIGH

### 3.1 Better Chunking Strategy

**Create Semantic Text Splitter** (`DocumentChunkingService.java`):

```java
@Service
public class DocumentChunkingService {

    private static final int TARGET_CHUNK_SIZE = 600;  // tokens
    private static final int CHUNK_OVERLAP = 50;       // tokens
    private static final int MIN_CHUNK_SIZE = 100;

    /**
     * Split document into semantic chunks with overlap.
     * Preserves code blocks, lists, and paragraph boundaries.
     */
    public List<DocumentChunk> chunkDocument(String content, String title, Map<String, Object> metadata) {
        List<DocumentChunk> chunks = new ArrayList<>();

        // 1. Split by major sections (## headers in markdown)
        List<Section> sections = splitBySections(content);

        // 2. For each section, split by paragraphs
        for (Section section : sections) {
            List<String> paragraphs = splitByParagraphs(section.content());

            StringBuilder currentChunk = new StringBuilder();
            currentChunk.append(section.header()).append("\n\n");
            int tokenCount = estimateTokens(currentChunk.toString());

            for (String paragraph : paragraphs) {
                int paragraphTokens = estimateTokens(paragraph);

                // If adding this paragraph exceeds target, save current chunk
                if (tokenCount + paragraphTokens > TARGET_CHUNK_SIZE && tokenCount > MIN_CHUNK_SIZE) {
                    chunks.add(createChunk(currentChunk.toString(), title, section.header(), metadata));

                    // Start new chunk with overlap (last paragraph)
                    currentChunk = new StringBuilder();
                    currentChunk.append(section.header()).append("\n\n");
                    // Add last N tokens from previous chunk for context
                    currentChunk.append(getLastNTokens(chunks.get(chunks.size() - 1).content(), CHUNK_OVERLAP));
                    currentChunk.append("\n\n");
                    tokenCount = estimateTokens(currentChunk.toString());
                }

                currentChunk.append(paragraph).append("\n\n");
                tokenCount += paragraphTokens;
            }

            // Save remaining content
            if (currentChunk.length() > 0) {
                chunks.add(createChunk(currentChunk.toString(), title, section.header(), metadata));
            }
        }

        return chunks;
    }

    private int estimateTokens(String text) {
        // Rough estimate: 1 token ≈ 4 characters
        return text.length() / 4;
    }

    private List<Section> splitBySections(String content) {
        // Split by ## headers in markdown
        // Preserve code blocks (```...```)
    }

    private DocumentChunk createChunk(String content, String title, String sectionHeader, Map<String, Object> metadata) {
        String chunkId = UUID.randomUUID().toString();
        String chunkTitle = title + " - " + sectionHeader;

        return new DocumentChunk(chunkId, chunkTitle, content, metadata);
    }
}
```

**Update DocumentManagementService**:

```java
public List<AssistantDocument> upsertDocument(UpsertDocumentRequest request) {
    // 1. Chunk the document
    List<DocumentChunk> chunks = chunkingService.chunkDocument(
        request.content(),
        request.title(),
        Map.of("locale", request.locale(), "version", "1.0")
    );

    List<AssistantDocument> savedDocs = new ArrayList<>();

    // 2. For each chunk, generate embedding and save
    for (DocumentChunk chunk : chunks) {
        // Generate embedding
        float[] embedding = embeddingService.generateEmbedding(chunk.content());

        // Create AssistantDocument
        AssistantDocument doc = new AssistantDocument();
        doc.setChunkId(chunk.chunkId());
        doc.setTitle(chunk.title());
        doc.setContent(chunk.content());
        doc.setRoles(request.roles());
        doc.setFrontends(request.frontends());
        doc.setRoutes(request.routes());
        doc.setLocale(request.locale());
        doc.setMetadataJson(objectMapper.writeValueAsString(chunk.metadata()));

        // Save to database
        doc = repository.save(doc);
        savedDocs.add(doc);

        // Upsert to Pinecone
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", chunk.title());
        metadata.put("locale", request.locale());
        metadata.put("roles", request.roles());
        metadata.put("frontends", request.frontends());
        metadata.put("routes", request.routes());

        pineconeClient.upsertVectors(List.of(
            new PineconeVector(chunk.chunkId(), embedding, metadata)
        ));
    }

    return savedDocs;
}
```

### 3.2 Technical Documentation Auto-Sync

**Create JavaDoc Extractor** (`JavaDocExtractor.java`):

```java
@Service
public class JavaDocExtractor {

    /**
     * Extract API documentation from Spring controllers.
     * Parses @RequestMapping annotations and JavaDoc comments.
     */
    public List<ApiDocumentation> extractControllerDocs(String projectPath) {
        List<ApiDocumentation> docs = new ArrayList<>();

        // 1. Find all *Controller.java files
        List<Path> controllers = findControllers(projectPath);

        // 2. Parse each controller
        for (Path controllerPath : controllers) {
            CompilationUnit cu = parseJavaFile(controllerPath);

            // 3. Extract class-level metadata
            ClassOrInterfaceDeclaration clazz = cu.getPrimaryType().get().asClassOrInterfaceDeclaration();
            String baseMapping = extractRequestMapping(clazz);
            String moduleName = extractModuleName(controllerPath);

            // 4. Extract method endpoints
            for (MethodDeclaration method : clazz.getMethods()) {
                if (hasRestAnnotation(method)) {
                    ApiDocumentation doc = new ApiDocumentation();
                    doc.setHttpMethod(extractHttpMethod(method));
                    doc.setPath(baseMapping + extractMethodMapping(method));
                    doc.setDescription(extractJavaDoc(method));
                    doc.setParameters(extractParameters(method));
                    doc.setReturnType(extractReturnType(method));
                    doc.setModule(moduleName);
                    doc.setRequiredRoles(extractSecurityAnnotations(method));

                    docs.add(doc);
                }
            }
        }

        return docs;
    }

    private String extractJavaDoc(MethodDeclaration method) {
        return method.getJavadoc()
            .map(javadoc -> javadoc.getDescription().toText())
            .orElse("No description available");
    }
}
```

**Create TypeScript Doc Extractor** (`TypeScriptDocExtractor.java`):

```java
@Service
public class TypeScriptDocExtractor {

    /**
     * Extract type definitions and interfaces from TypeScript files.
     * Useful for documenting data models, API types, component props.
     */
    public List<TypeDocumentation> extractTypeDocs(String frontendPath) {
        List<TypeDocumentation> docs = new ArrayList<>();

        // 1. Find all .types.ts files
        List<Path> typeFiles = findTypeFiles(frontendPath);

        // 2. Parse each file (use regex or TSDoc parser)
        for (Path typeFile : typeFiles) {
            String content = Files.readString(typeFile);

            // Extract interfaces
            Matcher interfaceMatcher = Pattern.compile(
                "/\\*\\*([^*]|\\*(?!/))*\\*/\\s*export\\s+interface\\s+(\\w+)\\s*\\{([^}]+)\\}"
            ).matcher(content);

            while (interfaceMatcher.find()) {
                String tsDoc = interfaceMatcher.group(1);
                String interfaceName = interfaceMatcher.group(2);
                String fields = interfaceMatcher.group(3);

                TypeDocumentation doc = new TypeDocumentation();
                doc.setName(interfaceName);
                doc.setDescription(parseTSDoc(tsDoc));
                doc.setFields(parseFields(fields));
                doc.setModule(extractModuleName(typeFile));

                docs.add(doc);
            }
        }

        return docs;
    }
}
```

**Create Document Sync Service** (`DocumentSyncService.java`):

```java
@Service
public class DocumentSyncService {

    private final JavaDocExtractor javaDocExtractor;
    private final TypeScriptDocExtractor tsDocExtractor;
    private final DocumentManagementService documentManagement;

    /**
     * Sync technical documentation from codebase to RAG knowledge base.
     * Should be run periodically or triggered by CI/CD.
     */
    public SyncReport syncTechnicalDocs() {
        SyncReport report = new SyncReport();

        // 1. Extract API documentation from backend controllers
        List<ApiDocumentation> apiDocs = javaDocExtractor.extractControllerDocs(
            "src/backend/src/main/java"
        );

        report.addDiscovered("API endpoints", apiDocs.size());

        // 2. Generate help documents from API docs
        for (ApiDocumentation apiDoc : apiDocs) {
            String content = formatApiDocumentation(apiDoc);

            UpsertDocumentRequest request = new UpsertDocumentRequest(
                "API: " + apiDoc.getHttpMethod() + " " + apiDoc.getPath(),
                content,
                determineRolesForEndpoint(apiDoc),
                determineFrontendsForModule(apiDoc.getModule()),
                List.of(),  // Routes: empty for technical docs
                "en-US",
                Map.of("type", "api", "module", apiDoc.getModule())
            );

            documentManagement.upsertDocument(request);
            report.addSynced("API endpoints", 1);
        }

        // 3. Extract data model documentation from entities
        List<EntityDocumentation> entityDocs = javaDocExtractor.extractEntityDocs(
            "src/backend/src/main/java/.../entities"
        );

        for (EntityDocumentation entityDoc : entityDocs) {
            String content = formatEntityDocumentation(entityDoc);
            // ... similar upsert logic
        }

        // 4. Extract frontend type documentation (optional)
        List<TypeDocumentation> typeDocs = tsDocExtractor.extractTypeDocs(
            "src/frontend/shared/lib"
        );

        // ... similar processing

        return report;
    }

    private String formatApiDocumentation(ApiDocumentation apiDoc) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(apiDoc.getHttpMethod()).append(" ").append(apiDoc.getPath()).append("\n\n");
        sb.append("## Description\n");
        sb.append(apiDoc.getDescription()).append("\n\n");
        sb.append("## Module\n");
        sb.append(apiDoc.getModule()).append("\n\n");

        if (!apiDoc.getParameters().isEmpty()) {
            sb.append("## Parameters\n");
            for (Parameter param : apiDoc.getParameters()) {
                sb.append("- **").append(param.getName()).append("** (").append(param.getType()).append("): ");
                sb.append(param.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## Returns\n");
        sb.append(apiDoc.getReturnType()).append("\n\n");

        if (!apiDoc.getRequiredRoles().isEmpty()) {
            sb.append("## Required Roles\n");
            sb.append(String.join(", ", apiDoc.getRequiredRoles())).append("\n");
        }

        return sb.toString();
    }
}
```

**Add Sync CLI Command**:

```java
@Component
public class DocumentSyncCommand implements CommandLineRunner {

    private final DocumentSyncService syncService;

    @Override
    public void run(String... args) throws Exception {
        // Check if --sync-docs flag is present
        if (Arrays.asList(args).contains("--sync-docs")) {
            log.info("Starting technical documentation sync...");
            SyncReport report = syncService.syncTechnicalDocs();
            log.info("Sync completed: {}", report);
        }
    }
}
```

**Run sync manually**:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--sync-docs"
```

### 3.3 Add Technical Documentation Content

**Categories to Add**:

1. **API Reference** (auto-generated)
   - All BFF endpoints with parameters, responses, examples
   - Authentication requirements
   - Rate limits

2. **Data Models** (auto-generated)
   - Order entity (status values, lifecycle)
   - Shipment entity (carrier integration)
   - Warehouse entity (capacity management)
   - DeliveryException types and severity levels

3. **Integration Guides** (manual)
   - Keycloak authentication flow
   - External carrier APIs (UPS, FedEx, DHL)
   - Address validation service
   - Rate calculation process

4. **Error Reference** (manual)
   - Common error codes and meanings
   - Troubleshooting steps
   - Resolution procedures

5. **System Architecture** (manual)
   - Module overview (order_module, rate_carrier_module, etc.)
   - Database schema
   - Frontend architecture
   - Deployment topology

**Example Manual Document**:

```markdown
# Keycloak Authentication Integration

## Overview

SLMS uses Keycloak for authentication and authorization. All frontend applications integrate with Keycloak using OpenID Connect (OIDC).

## Roles

The system defines the following roles:
- **CUSTOMER**: End users tracking orders
- **DRIVER**: Delivery drivers accessing routes
- **WAREHOUSE_STAFF**: Warehouse operators managing inventory
- **CUSTOMER_SERVICE**: Support staff handling exceptions
- **MANAGER**: Logistics managers with full access

## Authentication Flow

1. User navigates to frontend application
2. Frontend redirects to Keycloak login page
3. User enters credentials
4. Keycloak issues JWT access token + refresh token
5. Frontend stores tokens securely
6. API requests include access token in Authorization header
7. Backend validates JWT signature and claims

## Token Structure

Access tokens contain:
- `sub`: User ID
- `realm_access.roles`: List of assigned roles
- `exp`: Token expiration timestamp
- `email`: User email address

## Troubleshooting

**Error: "Invalid token"**
- Token may be expired (default: 5 minutes)
- Use refresh token to obtain new access token
- Check token signature against Keycloak public key

**Error: "Forbidden"**
- User lacks required role for endpoint
- Verify role assignment in Keycloak admin console
```

### 3.4 Re-Index Existing Documents

**Create Migration Script** (`DocumentReindexService.java`):

```java
@Service
public class DocumentReindexService {

    /**
     * Re-chunk and re-index all existing documents.
     * WARNING: This will delete existing vectors from Pinecone and recreate them.
     */
    @Transactional
    public ReindexReport reindexAllDocuments() {
        log.info("Starting document re-indexing...");

        ReindexReport report = new ReindexReport();

        // 1. Get all unique documents (grouped by title + locale)
        List<AssistantDocument> existingDocs = repository.findAll();
        Map<String, List<AssistantDocument>> docsByTitle = existingDocs.stream()
            .collect(Collectors.groupingBy(doc -> doc.getTitle() + "|" + doc.getLocale()));

        report.setTotalDocuments(docsByTitle.size());

        // 2. For each unique document
        for (Map.Entry<String, List<AssistantDocument>> entry : docsByTitle.entrySet()) {
            List<AssistantDocument> chunks = entry.getValue();
            AssistantDocument firstChunk = chunks.get(0);

            // Combine all chunks into original document
            String fullContent = chunks.stream()
                .map(AssistantDocument::getContent)
                .collect(Collectors.joining("\n\n"));

            log.info("Re-indexing: {}", firstChunk.getTitle());

            try {
                // 3. Delete old chunks from database and Pinecone
                for (AssistantDocument oldChunk : chunks) {
                    pineconeClient.deleteVector(oldChunk.getChunkId());  // TODO: Implement this
                    repository.delete(oldChunk);
                }

                // 4. Re-chunk and re-ingest
                UpsertDocumentRequest request = new UpsertDocumentRequest(
                    firstChunk.getTitle(),
                    fullContent,
                    firstChunk.getRoles(),
                    firstChunk.getFrontends(),
                    firstChunk.getRoutes(),
                    firstChunk.getLocale(),
                    Map.of()
                );

                List<AssistantDocument> newChunks = documentManagement.upsertDocument(request);

                report.addReindexed(firstChunk.getTitle(), chunks.size(), newChunks.size());

            } catch (Exception e) {
                log.error("Failed to reindex: {}", firstChunk.getTitle(), e);
                report.addFailed(firstChunk.getTitle(), e.getMessage());
            }
        }

        log.info("Re-indexing completed: {}", report);
        return report;
    }
}
```

**Add CLI Command**:
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--reindex-docs"
```

### 3.5 Testing

**Chunking Tests**:
- Test chunk size targeting (600 ± 100 tokens)
- Test overlap preservation
- Test code block preservation
- Test section header preservation

**Extraction Tests**:
- Test JavaDoc extraction from sample controller
- Test TSDoc extraction from sample type file
- Verify generated content quality

**Retrieval Quality Tests**:
- Before/after comparison of retrieval accuracy
- Test queries with technical questions
- Measure top-3 hit rate improvement

**Example Test**:
```java
@Test
void chunkingProducesOptimalSizes() {
    String longDocument = generateDocument(5000);  // 5000 tokens

    List<DocumentChunk> chunks = chunkingService.chunkDocument(longDocument, "Test", Map.of());

    // Should produce ~8 chunks (5000 / 600 ≈ 8.3)
    assertThat(chunks).hasSizeBetween(7, 10);

    // Each chunk should be near target size
    for (DocumentChunk chunk : chunks) {
        int tokens = estimateTokens(chunk.content());
        assertThat(tokens).isBetween(500, 800);
    }
}

@Test
void technicalQuestionRetrievesApiDocs() {
    // Seed API documentation
    syncService.syncTechnicalDocs();

    // Query for technical help
    String query = "How do I create a new order via API?";
    float[] embedding = embeddingService.generateEmbedding(query);
    List<Match> results = ragService.queryRelevantDocuments(embedding, "admin-console", "MANAGER", "en-US", 5);

    // Should retrieve POST /api/orders endpoint documentation
    assertThat(results)
        .extracting(match -> match.getMetadata().get("title"))
        .anyMatch(title -> title.contains("POST /api/warehouse/orders"));
}
```

---

## 📋 Phase 4: Proactive Suggestions (Stretch Goal)

**Duration**: 0.5-1 day
**Priority**: MEDIUM

### 4.1 Backend Suggestion Engine

**Create ProactiveSuggestionService**:

```java
@Service
public class ProactiveSuggestionService {

    private final Map<String, List<Suggestion>> routeSuggestions = new HashMap<>();

    @PostConstruct
    public void initializeSuggestions() {
        // Define route-based suggestions
        routeSuggestions.put("/orders/create", List.of(
            new Suggestion("What information do I need to create an order?", "info"),
            new Suggestion("How do I upload shipping documents?", "help"),
            new Suggestion("Which warehouses are available?", "data")
        ));

        routeSuggestions.put("/orders", List.of(
            new Suggestion("How do I track an order?", "help"),
            new Suggestion("What do the different order statuses mean?", "info"),
            new Suggestion("Show me my recent orders", "data")
        ));

        routeSuggestions.put("/tracking", List.of(
            new Suggestion("Where is my order?", "data"),
            new Suggestion("When will my order arrive?", "data"),
            new Suggestion("How do I report a delivery issue?", "help")
        ));

        // ... more routes
    }

    /**
     * Get contextual suggestions for the current page.
     * @param route Current application route
     * @param role User role for filtering
     * @param recentActivity Recent user actions (optional)
     * @return List of suggested questions
     */
    public List<Suggestion> getSuggestions(String route, String role, UserActivity recentActivity) {
        List<Suggestion> suggestions = new ArrayList<>();

        // 1. Get route-specific suggestions
        List<Suggestion> routeSpecific = routeSuggestions.getOrDefault(route, List.of());
        suggestions.addAll(routeSpecific);

        // 2. Add role-specific suggestions
        suggestions.addAll(getRoleSpecificSuggestions(role));

        // 3. Add activity-based suggestions
        if (recentActivity != null) {
            if (recentActivity.hasErrors()) {
                suggestions.add(new Suggestion("I'm seeing an error, can you help?", "troubleshooting"));
            }
            if (recentActivity.isRepeatVisit()) {
                suggestions.add(new Suggestion("Continue where I left off", "context"));
            }
        }

        // 4. Rank by relevance and return top 3
        return suggestions.stream()
            .limit(3)
            .toList();
    }

    private List<Suggestion> getRoleSpecificSuggestions(String role) {
        return switch (role) {
            case "CUSTOMER" -> List.of(
                new Suggestion("Track my order", "data"),
                new Suggestion("Report a delivery problem", "help")
            );
            case "DRIVER" -> List.of(
                new Suggestion("Show my route for today", "data"),
                new Suggestion("How do I mark an order as delivered?", "help")
            );
            case "WAREHOUSE_STAFF" -> List.of(
                new Suggestion("Show pending orders", "data"),
                new Suggestion("How do I update order status?", "help")
            );
            default -> List.of();
        };
    }
}
```

**Add Suggestion Endpoint**:

```java
@GetMapping("/suggestions")
public ResponseEntity<List<Suggestion>> getSuggestions(
    @RequestParam String route,
    @AuthenticationPrincipal Jwt jwt
) {
    String role = JwtSecurityUtils.extractPrimaryRole(jwt);

    List<Suggestion> suggestions = suggestionService.getSuggestions(route, role, null);

    return ResponseEntity.ok(suggestions);
}
```

### 4.2 Frontend Integration

**Update ChatWidget Component**:

```typescript
export function ChatWidget({ endpoint }: ChatWidgetProps) {
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [suggestions, setSuggestions] = useState<Suggestion[]>([]);
  const router = useRouter();
  const pathname = usePathname();

  // Load suggestions when idle
  useEffect(() => {
    let timer: NodeJS.Timeout;

    const loadSuggestions = async () => {
      try {
        const response = await fetch(`${endpoint}/suggestions?route=${encodeURIComponent(pathname)}`);
        const data = await response.json();
        setSuggestions(data);
        setShowSuggestions(true);
      } catch (error) {
        console.error('Failed to load suggestions:', error);
      }
    };

    // Show suggestions after 10 seconds of inactivity
    timer = setTimeout(loadSuggestions, 10000);

    return () => clearTimeout(timer);
  }, [pathname, endpoint]);

  const handleSuggestionClick = (suggestion: Suggestion) => {
    setShowSuggestions(false);
    onSendMessage(suggestion.question);
    setIsOpen(true);
  };

  return (
    <>
      {showSuggestions && !isOpen && suggestions.length > 0 && (
        <div className="fixed bottom-24 right-6 bg-white rounded-lg shadow-xl border p-4 max-w-sm animate-slide-up">
          <div className="flex items-center justify-between mb-3">
            <h4 className="font-semibold text-sm">Need help?</h4>
            <button onClick={() => setShowSuggestions(false)}>
              <X className="h-4 w-4" />
            </button>
          </div>
          <div className="space-y-2">
            {suggestions.map((suggestion, idx) => (
              <button
                key={idx}
                onClick={() => handleSuggestionClick(suggestion)}
                className="w-full text-left px-3 py-2 rounded-lg bg-gray-50 hover:bg-gray-100 text-sm transition-colors"
              >
                {suggestion.question}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Regular chat widget... */}
    </>
  );
}
```

### 4.3 Advanced: User Activity Tracking

**Track User Actions** (optional):

```java
@Service
public class UserActivityService {

    private final Map<String, UserActivity> activeUsers = new ConcurrentHashMap<>();

    public void recordAction(String userId, String action, String route) {
        UserActivity activity = activeUsers.computeIfAbsent(userId, k -> new UserActivity(userId));
        activity.addAction(action, route);
    }

    public void recordError(String userId, String errorCode) {
        UserActivity activity = activeUsers.get(userId);
        if (activity != null) {
            activity.addError(errorCode);
        }
    }

    public UserActivity getActivity(String userId) {
        return activeUsers.get(userId);
    }
}
```

**Use in Suggestion Service**:
```java
UserActivity activity = activityService.getActivity(userId);
List<Suggestion> suggestions = suggestionService.getSuggestions(route, role, activity);
```

---

## 📊 Testing & Validation

### Unit Tests
- [ ] Tool execution with valid/invalid parameters
- [ ] Tool role-based access control
- [ ] Streaming event emission
- [ ] Chunk size optimization
- [ ] Document extraction accuracy

### Integration Tests
- [ ] End-to-end tool calling flow
- [ ] Streaming with tool calls mid-stream
- [ ] RAG retrieval with new chunks
- [ ] Document sync from codebase
- [ ] Suggestion loading by route

### Manual Testing Checklist
- [ ] Ask "What's the status of order X?" → Should use OrderStatusTool
- [ ] Ask "Show me warehouse inventory" → Should use WarehouseInventoryTool
- [ ] Test streaming with long response → Should see typewriter effect
- [ ] Test streaming with tool call → Should pause, execute, continue
- [ ] Ask technical question → Should retrieve API docs
- [ ] Visit /orders page and wait 10s → Should show suggestions
- [ ] Click suggestion → Should open chat with pre-filled question

### Performance Tests
- [ ] Tool execution time < 2 seconds
- [ ] Streaming first token < 500ms
- [ ] RAG retrieval time < 1 second
- [ ] Document sync time for full codebase < 5 minutes

---

## 🔧 Configuration Updates

### Backend `application.yml`

```yaml
chatbot:
  gemini:
    api-key: ${GEMINI_API_KEY}
    model: gemini-1.5-flash
    embedding-model: gemini-embedding-001
    streaming-enabled: true  # NEW

  pinecone:
    api-key: ${PINECONE_API_KEY}
    host: ${PINECONE_HOST}
    index: ${PINECONE_INDEX}
    namespace: default

  chunking:
    target-size: 600        # NEW
    overlap: 50             # NEW
    min-size: 100           # NEW

  tools:
    enabled: true           # NEW
    max-execution-time: 5000  # NEW (milliseconds)

  suggestions:
    enabled: true           # NEW
    idle-timeout: 10000     # NEW (milliseconds)
```

### Environment Variables

```bash
# .env
GEMINI_API_KEY=your_gemini_api_key
PINECONE_API_KEY=your_pinecone_api_key
PINECONE_HOST=your_pinecone_host
PINECONE_INDEX=slms-assistant
```

---

## 📝 Documentation Updates

### Files to Update
- `docs/handbook/chatbot-module.md` - Add tool calling, streaming, chunking sections
- `docs/adr/0007-chatbot-module-integration.md` - Update with new capabilities
- `README.md` - Mention AI assistant with database access
- API documentation - Document new streaming endpoints

### New Documentation
- `docs/handbook/chatbot-tools.md` - Complete tool reference
- `docs/handbook/chatbot-streaming.md` - Streaming implementation guide
- `docs/handbook/rag-optimization.md` - Chunking strategy and best practices

---

## 🚀 Deployment & Rollout

### Phase 1: Backend Infrastructure (Day 1-2)
1. Merge tool calling infrastructure
2. Deploy to dev environment
3. Test with Postman/curl
4. Monitor logs for errors

### Phase 2: Streaming (Day 2-3)
1. Deploy streaming endpoints
2. Test with SSE clients
3. Monitor performance metrics

### Phase 3: Enhanced RAG (Day 3-4)
1. Run document re-indexing script (off-hours)
2. Deploy sync service
3. Run initial sync
4. Verify retrieval quality

### Phase 4: Frontend Integration (Day 4-5)
1. Deploy frontend changes to dev
2. Test end-to-end flows
3. Fix any issues
4. Deploy to production

### Phase 5: Proactive Suggestions (Day 5 if time)
1. Deploy suggestion service
2. Test on each route
3. Monitor user interaction rates

---

## 📊 Success Metrics

### Functional Metrics
- **Tool Calling Success Rate**: Target 80%+ (tools execute successfully)
- **Streaming Performance**: First token < 500ms, full response smooth
- **RAG Accuracy**: Top-3 retrieval accuracy > 90%
- **Suggestion Click Rate**: > 10% of users engage with suggestions

### Technical Metrics
- **Response Time**: P95 < 3 seconds (including tool execution)
- **Error Rate**: < 5% of requests fail
- **Code Coverage**: All new services > 80%
- **API Cost**: Monitor Gemini/Pinecone usage, optimize if needed

### User Metrics
- **Chat Usage**: 20%+ increase in chat interactions
- **Question Complexity**: Users ask more specific questions (with order IDs, etc.)
- **User Satisfaction**: Collect thumbs up/down feedback (future feature)

---

## ⚠️ Risks & Mitigations

### Risk 1: Tool Execution Delays
- **Impact**: User waits too long for response
- **Mitigation**: Set 5-second timeout, show loading indicator, stream partial response

### Risk 2: Streaming Failures
- **Impact**: User sees incomplete response
- **Mitigation**: Fallback to non-streaming mode, retry logic, error messages

### Risk 3: RAG Quality Degradation
- **Impact**: Chunking breaks document coherence
- **Mitigation**: Test retrieval quality before/after, adjust chunk size, manual review

### Risk 4: Unauthorized Data Access via Tools
- **Impact**: Security breach, users access other users' data
- **Mitigation**: Strict role validation, parameter sanitization, audit logging

### Risk 5: Increased API Costs
- **Impact**: Gemini/Pinecone bills spike
- **Mitigation**: Monitor usage, implement rate limiting, cache aggressive, set budget alerts

---

## 🎉 Post-Implementation

### Documentation
- Update all handbook pages
- Record demo video
- Write blog post (optional)

### Monitoring
- Set up alerts for error rate > 10%
- Monitor P95 latency
- Track API costs daily

### Future Enhancements
1. **Conversation Persistence** - Store chat history in database
2. **Multi-turn Tool Calling** - Chain multiple tool calls
3. **User Feedback Loop** - Thumbs up/down on responses
4. **Advanced Caching** - Semantic cache for similar questions
5. **Analytics Dashboard** - Popular questions, RAG coverage, tool usage
6. **Voice Input** - Speech-to-text integration
7. **Image Support** - Upload images for context (delivery proof, damage photos)

---

## 📅 Implementation Timeline

| Day | Phase | Tasks | Status |
|-----|-------|-------|--------|
| 1 | Tool Infrastructure | Create Tool interface, ToolRegistry, ToolExecutor | ⏳ |
| 1 | Database Tools | Implement OrderLookupTool, OrderStatusTool | ⏳ |
| 2 | Database Tools | Implement WarehouseInventoryTool, ShipmentTrackingTool, DeliveryExceptionTool | ⏳ |
| 2 | Streaming Backend | Update GeminiClient for streaming, create streaming BFF endpoints | ⏳ |
| 3 | Streaming Frontend | Update chatbot-api.ts, React components for streaming | ⏳ |
| 3 | Chunking | Create DocumentChunkingService, update DocumentManagementService | ⏳ |
| 4 | Doc Extraction | Create JavaDocExtractor, TypeScriptDocExtractor, DocumentSyncService | ⏳ |
| 4 | Re-indexing | Run document re-indexing, test retrieval quality | ⏳ |
| 5 | Proactive Suggestions | Create ProactiveSuggestionService, update frontend | ⏳ |
| 5 | Testing & Polish | Integration tests, manual testing, bug fixes | ⏳ |

---

## 🔗 References

- [Gemini API Function Calling](https://ai.google.dev/docs/function_calling)
- [Gemini API Streaming](https://ai.google.dev/docs/streaming)
- [Pinecone Metadata Filtering](https://docs.pinecone.io/docs/metadata-filtering)
- [Server-Sent Events (SSE)](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [Semantic Text Splitting Strategies](https://www.pinecone.io/learn/chunking-strategies/)
- [JavaParser Documentation](https://javaparser.org/)

---

**Document Status**: Planning
**Last Updated**: 2025-12-04
**Author**: Claude AI
**Reviewers**: Development Team

---

This document will be deleted after successful implementation.
