# Chatbot Module

## Overview

The Chatbot Module provides context-aware AI assistance to users across all SLMS frontends. It combines role-based access control, frontend-specific content filtering, and route-based contextual help to deliver relevant guidance.

## Architecture

### Components

**AI Integration**
- LLM: Gemini API for natural language processing
- Vector Database: Pinecone for semantic search and document retrieval
- Backend Service: Business layer handles query processing and response generation

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

1. User submits question
2. Question is embedded into vector
3. Pinecone performs semantic search to find relevant chunk IDs
4. Backend queries AssistantDocument table using retrieved chunk IDs
5. Results filtered by role/frontend/route
6. Gemini LLM generates natural language response using retrieved context

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

## Related Documentation

- [ADR-0007: Chatbot Module Integration](../adr/0007-chatbot-module-integration.md) - Architecture decision record
- [Data Model](data-model.md) - Database schema details
