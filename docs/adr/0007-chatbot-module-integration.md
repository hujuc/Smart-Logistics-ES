# 0007 – Chatbot Module Integration

**Sprint 4**

## Context

SLMS has multiple user roles (logistics managers, warehouse staff, customer service representatives, drivers, customers) using different frontend applications (admin console, customer portal, driver PWA, landing page). Each role needs contextual help specific to their tasks and current application context.

### Problem

- Users need in-app assistance without leaving the application
- Help content must be role-specific (warehouse staff shouldn't see driver help)
- Context matters: help should be relevant to current page/route
- Different frontends need different help content
- Multilingual support required for Portuguese and English users
- Static documentation is not contextual or conversational

### Requirements

- Context-aware AI assistant accessible from all frontends
- Role-based content filtering (only show authorized content)
- Frontend-based content filtering (admin-console vs customer-portal)
- Route-based content filtering (help relevant to current page)
- Multilingual support (Portuguese, English)
- Semantic search capability (find relevant content by meaning, not just keywords)
- Natural language interaction (conversational responses)
- Minimal impact on existing frontend architecture

### Constraints

- Must integrate with existing authentication (Keycloak roles)
- Should not require major frontend refactoring
- External services preferred over self-hosted ML infrastructure
- Must work with Next.js 15 and React 19 frontends
- Need cost-effective solution for small-to-medium scale

## Decision

We adopted an **AI assistant (chatbot) module** using Gemini LLM and Pinecone vector database with role/frontend/route-based content filtering.

### Architecture

**Components:**
1. **AssistantDocument Entity** - Stores help document chunks with metadata
2. **Pinecone Vector Database** - Semantic search via vector embeddings
3. **Gemini API** - Natural language processing and response generation
4. **Backend Service** - Business layer handling queries and filtering
5. **Frontend Integration** - Chat UI component in all frontends

### Database Design

**AssistantDocument Table**
```sql
CREATE TABLE assistant_documents (
    id UUID PRIMARY KEY,
    chunk_id VARCHAR UNIQUE NOT NULL,  -- Pinecone vector ID
    title VARCHAR NOT NULL,
    locale VARCHAR(10) NOT NULL,       -- 'en', 'pt'
    content TEXT NOT NULL,              -- Document text
    metadata_json TEXT,                 -- Additional metadata
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE assistant_document_roles (
    document_id UUID REFERENCES assistant_documents(id),
    role VARCHAR NOT NULL                -- 'MANAGER', 'WAREHOUSE_STAFF', etc.
);

CREATE TABLE assistant_document_frontends (
    document_id UUID REFERENCES assistant_documents(id),
    frontend VARCHAR NOT NULL            -- 'admin-console', 'customer-portal', etc.
);

CREATE TABLE assistant_document_routes (
    document_id UUID REFERENCES assistant_documents(id),
    route VARCHAR NOT NULL               -- '/orders', '/carriers', etc.
);
```

### Content Filtering Strategy

Multi-layer filtering ensures relevant responses:

1. **Locale Filtering**: Query only documents matching user's language
2. **Role Filtering**: Filter by user's Keycloak role(s)
3. **Frontend Filtering**: Filter by current application
4. **Route Filtering**: Prioritize content related to current page
5. **Semantic Search**: Use Pinecone vector similarity for relevance

### Query Flow

1. User submits question from frontend
2. Backend receives: question, user role, frontend name, current route, locale
3. Question embedded into vector using Gemini embedding model
4. Pinecone performs semantic search, returns top-k chunk IDs
5. Backend queries AssistantDocument table with chunk IDs
6. Results filtered by role/frontend/route/locale
7. Filtered content sent to Gemini LLM with original question
8. Gemini generates natural language response
9. Response returned to frontend

### Integration Points

**Backend API**
- `POST /api/assistant/query` - Submit question with context
- Request body: `{question, role, frontend, route, locale}`
- Response: `{answer, sources, confidence}`

**Frontend Components**
- Chat UI component integrated into all 4 frontends
- Floating chat button in bottom-right corner
- Context automatically captured (route, locale from Next.js)
- Role extracted from Keycloak authentication token

### External Services

**Gemini API**
- Model: gemini-pro (or gemini-1.5-pro)
- Used for: text embeddings and response generation
- Configuration: `GEMINI_API_KEY` environment variable

**Pinecone**
- Index for vector storage and similarity search
- Configuration: `PINECONE_API_KEY`, `PINECONE_INDEX_NAME`
- Dimension: 768 (matches Gemini embedding size)

## Consequences

**Positive:**

- Context-aware help improves user experience
- Role-based filtering ensures users only see authorized content
- Frontend-specific content keeps help relevant to application
- Route-based filtering provides contextual assistance
- Multilingual support serves Portuguese and English users
- Semantic search finds relevant content even with different wording
- Natural language responses more user-friendly than static docs
- Centralized content management (update once, available everywhere)
- Scalable: add more documents without code changes
- External services (Gemini, Pinecone) avoid ML infrastructure overhead

**Negative:**

- External service dependencies (Gemini and Pinecone availability)
- API costs scale with usage (per-query pricing)
- Requires document ingestion process (create embeddings, upload to Pinecone)
- Response latency depends on external API calls
- Content quality depends on document maintenance
- Potential for incorrect or hallucinated responses (LLM limitation)
- Additional database tables increase schema complexity
- User trust in AI-generated responses may vary

## Alternatives Considered

### Option 1: Static Help Documentation
**Why not chosen:**
- Not contextual or conversational
- Requires users to search and navigate
- No role-based filtering
- Less user-friendly experience
- Cannot handle natural language queries

### Option 2: Self-Hosted LLM (Ollama, llama.cpp)
**Why not chosen:**
- Requires dedicated GPU infrastructure
- Operational overhead (model management, scaling)
- Team lacks ML operations expertise
- Higher initial cost and complexity
- Latency considerations for self-hosting
- Managed services (Gemini) more cost-effective at current scale

### Option 3: Rule-Based Chatbot (Intent Matching)
**Why not chosen:**
- Requires manual intent definition for every question
- Brittle: cannot handle variations in phrasing
- High maintenance burden
- No semantic understanding
- Less natural conversation flow

### Option 4: Elasticsearch for Document Search
**Why not chosen:**
- Keyword-based, not semantic search
- Cannot generate conversational responses
- Would still need LLM integration for natural language
- Pinecone specialized for vector similarity search

## Related Decisions

- ADR-0002 – Data Model Definition (database schema)
- ADR-0004 – Authentication and Authorization (Keycloak integration for roles)
- ADR-0005 – Observability (monitoring chatbot performance)

## Notes

- Document ingestion process: Create chunks → Generate embeddings → Store in Pinecone → Create AssistantDocument records
- Recommended chunk size: 500-1000 tokens for optimal retrieval
- Top-k configuration: Retrieve 3-5 most relevant chunks per query
- Future enhancement: User feedback on response quality (thumbs up/down)
- Future enhancement: Conversation history (multi-turn dialogue)
- Security: Document content must not include sensitive data (credentials, PII)
- Gemini API key security: Environment variables, never committed to repository
- Monitoring: Track query volume, response time, error rate via Elastic APM
