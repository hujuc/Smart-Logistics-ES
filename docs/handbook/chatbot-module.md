# Chatbot Module

## Overview

The Chatbot Module provides context-aware AI assistance to users across all SLMS frontends. It combines role-based access control, frontend-specific content filtering, and route-based contextual help to deliver relevant guidance. The chatbot can access real-time database information through a tool calling system, enabling it to answer specific queries about orders, shipments, warehouses, and delivery exceptions.

## Architecture

### Components

**AI Integration**
- LLM: Gemini API with function calling support for natural language processing
- Vector Database: Pinecone for semantic search and document retrieval
- Backend Service: Business layer handles query processing, tool execution, and response generation
- Tool System: Provides database access capabilities with role-based authorization

**Database**
- AssistantDocument entity stores document chunks with metadata
- Collection tables enable many-to-many filtering (roles, frontends, routes)

### AssistantDocument Entity

Stores contextual help documents as searchable chunks:

```java
@Entity
@Table(name = "assistant_documents")
public class AssistantDocument {
    UUID id;                    // Primary key
    String chunkId;             // Pinecone vector ID (unique)
    String title;               // Document title
    Set<String> roles;          // Allowed roles (e.g., "MANAGER", "WAREHOUSE_STAFF")
    Set<String> frontends;      // Target frontends (e.g., "admin-console", "customer-portal")
    Set<String> routes;         // Relevant routes (e.g., "/orders", "/carriers")
    String locale;              // Language code (e.g., "en", "pt")
    String content;             // Document text content
    String metadataJson;        // Additional metadata
    Instant createdAt;          // Creation timestamp
}
```

### Collection Tables

**assistant_document_roles**
- Maps documents to authorized roles
- Enables role-based content filtering
- Example roles: MANAGER, WAREHOUSE_STAFF, DRIVER, CUSTOMER_SERVICE, CUSTOMER

**assistant_document_frontends**
- Maps documents to specific frontend applications
- Enables frontend-specific help content
- Example frontends: admin-console, customer-portal, driver-pwa, landing-page

**assistant_document_routes**
- Maps documents to application routes
- Enables contextual help based on current page
- Example routes: /orders, /orders/create, /carriers, /shipments

## Content Filtering

The chatbot applies multiple layers of filtering to ensure relevant responses:

1. **Role Filtering**: Only show documents authorized for user's role
2. **Frontend Filtering**: Only include content relevant to current application
3. **Route Filtering**: Prioritize content related to current page
4. **Locale Filtering**: Serve content in user's preferred language
5. **Semantic Search**: Use vector similarity to find relevant content chunks
6. **Tool Authorization**: Filter available tools based on user role and enforce authorization at execution time

## Tool Calling System

The chatbot can execute database queries through a secure tool calling system. Tools are functions that the LLM can invoke to retrieve real-time information from the SLMS database.

### Architecture

**Tool Interface**
```java
public interface Tool {
    String getName();                           // Unique tool identifier
    String getDescription();                    // Description for LLM
    List<ToolParameter> getParameters();        // Parameter schema
    Set<String> getAllowedRoles();              // Roles authorized to use this tool
    ToolResult execute(Map<String, Object> params, UserContext context);
}
```

**ToolRegistry**
- Auto-discovers tool implementations at startup via Spring component scanning
- Provides all tool definitions to LLM for authorization-aware responses
- Filters executable tools by user role
- Maintains thread-safe registry of available tools

**ToolExecutor**
- Validates tool existence and parameters
- Enforces role-based authorization before execution
- Executes tools with timeout protection (5 second limit)
- Returns structured results or error messages
- Logs all tool executions for audit purposes

### Available Tools

**OrderLookupTool**
- **Purpose**: Look up detailed order information by ID or tracking number
- **Parameters**: `orderId` (integer) OR `trackingNumber` (string)
- **Returns**: Order details including status, customer info, addresses, items
- **Allowed Roles**: CUSTOMER (own orders), DRIVER, WAREHOUSE_STAFF, CUSTOMER_SERVICE, LOGISTICS_MANAGER
- **Authorization**: Customers can only access their own orders

**OrderStatusTool**
- **Purpose**: Check current status and event history of an order
- **Parameters**: `orderId` (integer)
- **Returns**: Current status and recent order events
- **Allowed Roles**: CUSTOMER (own orders), DRIVER, WAREHOUSE_STAFF, CUSTOMER_SERVICE, LOGISTICS_MANAGER

**WarehouseInventoryTool**
- **Purpose**: Query warehouse information, capacity, and active order count
- **Parameters**: `warehouseId` (integer, optional) - omit to list all warehouses
- **Returns**: Warehouse details, capacity, current order count
- **Allowed Roles**: WAREHOUSE_STAFF, LOGISTICS_MANAGER

**ShipmentTrackingTool**
- **Purpose**: Get real-time shipment tracking information
- **Parameters**: `shipmentId` (integer) OR `trackingNumber` (string)
- **Returns**: Carrier info, current location, delivery ETA
- **Allowed Roles**: CUSTOMER (own shipments), DRIVER (assigned), CUSTOMER_SERVICE, LOGISTICS_MANAGER

**DeliveryExceptionTool**
- **Purpose**: List active delivery exceptions and issues
- **Parameters**: `orderId` (integer, optional), `severity` (string, optional)
- **Returns**: List of exceptions with details and severity levels
- **Allowed Roles**: CUSTOMER (own orders), CUSTOMER_SERVICE, LOGISTICS_MANAGER

**UserCountTool**
- **Purpose**: Count total users in the system
- **Parameters**: None
- **Returns**: Total user count
- **Allowed Roles**: LOGISTICS_MANAGER

**ShipmentCountTool**
- **Purpose**: Count total shipments in the system
- **Parameters**: None
- **Returns**: Total shipment count
- **Allowed Roles**: LOGISTICS_MANAGER

### Authorization System

The tool system implements defense-in-depth authorization:

**LLM-Level Awareness**
- LLM receives definitions for ALL tools in the system
- System prompt includes explicit authorization rules for the user's role
- LLM is instructed to explain access restrictions when users request unauthorized information
- Provides helpful messages suggesting alternative actions within user's permissions

**Execution-Level Enforcement**
- ToolExecutor validates user role against tool's allowed roles before execution
- Authorization failures return error messages to LLM
- Customers accessing orders are validated to ensure they own the requested orders
- All authorization checks are logged for security auditing

**Example Authorization Flow**

When a customer asks "What's in warehouse 5?":
1. LLM sees the `warehouse_inventory` tool definition
2. System prompt indicates this tool is not allowed for CUSTOMER role
3. LLM responds: "I'm sorry, but warehouse inventory information is only accessible to warehouse staff and logistics managers. As a customer, I can help you track your orders, check delivery status, or answer questions about your shipments."
4. If LLM somehow calls the tool anyway, ToolExecutor blocks execution and returns authorization error

## Multilingual Support

Documents are stored with locale identifiers (ISO 639-1 codes):
- `en` - English
- `pt` - Portuguese
- Additional locales can be added as needed

The system queries only documents matching the user's locale setting.

## Vector Storage Integration

### Pinecone Configuration

- Document content is embedded into vectors using text embedding models
- Vectors stored in Pinecone with chunkId as the identifier
- AssistantDocument.chunkId links database records to Pinecone vectors

### Query Flow

1. User submits question through frontend chatbot
2. Backend extracts user context (role, userId, frontend, locale)
3. Question is embedded into vector using Gemini embedding model
4. Pinecone performs semantic search to find relevant chunk IDs
5. Backend queries AssistantDocument table using retrieved chunk IDs
6. Results filtered by role/frontend/route/locale
7. Retrieved documents augment the user's question with context
8. ToolRegistry provides all tool definitions to LLM
9. System prompt includes authorization guidance for user's role
10. Gemini LLM receives:
    - System prompt with role-specific authorization rules
    - Augmented user question with RAG context
    - Full conversation history
    - All available tool definitions (not just allowed ones)
11. LLM generates response and optionally requests tool calls
12. If tool calls requested:
    - ToolExecutor validates authorization for each tool
    - Executes authorized tools with timeout protection
    - Returns tool results to LLM
    - LLM generates final response incorporating tool results
13. Final natural language response returned to user

## Usage Examples

### Role-Based Help

User with role "WAREHOUSE_STAFF" viewing "/orders/create" will receive:
- Documents where roles contains "WAREHOUSE_STAFF"
- Documents where frontends contains "admin-console"
- Documents where routes contains "/orders" or "/orders/create"
- Documents matching user's locale

### Contextual Assistance

Customer viewing tracking page:
- Role: CUSTOMER
- Frontend: customer-portal
- Route: /tracking
- Locale: pt
- Receives Portuguese-language help about tracking their orders

## Implementation Details

### Document Ingestion

1. Create document chunks from help content
2. Generate embeddings using embedding model
3. Store vectors in Pinecone with unique chunkId
4. Create AssistantDocument record with metadata
5. Associate with roles, frontends, and routes

### Query Processing

1. Extract user context (role, frontend, route, locale)
2. Embed user question into vector
3. Query Pinecone for top-k similar vectors
4. Filter results by user context
5. Generate response using Gemini with retrieved context
6. Return natural language answer

## Configuration

Required environment variables:
- `GEMINI_API_KEY` - Gemini API authentication
- `PINECONE_API_KEY` - Pinecone vector database access
- `PINECONE_INDEX_NAME` - Index name for vector storage

Application configuration:
```yaml
chatbot:
  tools:
    enabled: true              # Enable tool calling system
    max-execution-time: 5000   # Tool timeout in milliseconds
```

## Related Documentation

- [ADR-0007: Chatbot Module Integration](../adr/0007-chatbot-module-integration.md) - Architecture decision record
- [Data Model](data-model.md) - Database schema details
