# ADR-0004 – Authentication and Security Architecture

**Sprint 3**

## Context

As SLMS has grown to support multiple user roles (Logistics Manager, Warehouse Staff, CSR, Driver, Customer) across three separate frontend applications, we need a centralized authentication and authorization solution. Currently, user management is planned through a basic `users` table, but this approach has limitations:

- No single sign-on across applications
- Each frontend would need separate login implementation  
- Role-based access control would be scattered across modules
- No standardized token management
- Difficult to implement advanced features (2FA, social login, password policies)

**Key requirements:**
- Unified authentication across all three frontends
- Role-based access control (RBAC) with fine-grained permissions
- Single sign-on (SSO) capability
- Token-based authentication for stateless API
- Audit trail for security events
- User session management
- Future-proof for microservices migration

**Constraints:**
- Must integrate with existing modular monolith architecture
- Cannot disrupt existing module boundaries
- Must work with PostgreSQL database
- Should follow existing Docker containerization strategy
- Need to maintain backward compatibility with existing API structure

## Decision

We will implement a **centralized authentication architecture** using:

1. **Keycloak** as an external Identity and Access Management (IAM) service
2. **New User/Security Module** within the Spring Boot monolith to bridge Keycloak with SLMS
3. **Landing Page Frontend** as the single entry point for all users
4. **Role-based routing** to direct users to appropriate frontends post-authentication

### Architecture Components

**1. Landing Page (New Frontend #4)**
- Public-facing welcome page at `http://localhost/`
- Handles login/signup flow via Keycloak
- Routes authenticated users to appropriate frontend based on roles
- Provides public order tracking capability

**2. User Management Module (New Backend Module)**
- Manages token validation and refresh
- Synchronizes user profiles between Keycloak and SLMS
- Handles role-based authorization
- Maintains security audit logs
- Provides SecurityContext for other modules
- Exposes BFF endpoints for each frontend application

**3. Keycloak Integration (External Service)**
- Deployed as Docker container alongside other services
- Manages user authentication and credentials
- Provides OAuth 2.0/OpenID Connect protocols
- Handles advanced features (2FA, password policies, social login)

**4. Enhanced Module Communication**
```bash
Landing Page → Keycloak → User Management Module → Route to Target Frontend
                              ↓
                    Other Backend Modules
                    (Order, Carrier, Route, etc.)
```

Refer to updated architecture diagrams:

- [Updated Logic Architecture](../per-sprint/sprint2/architecture-update-security-&-auth/logic-architecture.png)
- [Updated Installation Architecture](../per-sprint/sprint2/architecture-update-security-&-auth/instalation-architecture.png)

## Consequences

**Positive:**

- **Single Sign-On** - Users login once and access all authorized applications
- **Centralized User Management** - One place to manage all users and roles
- **Enhanced Security** - Industry-standard OAuth 2.0/OIDC protocols
- **Reduced Development Effort** - No need to build authentication features
- **Audit Compliance** - Comprehensive security event logging
- **Scalability** - Ready for future microservices architecture
- **Module Cohesion** - Security concerns properly encapsulated in dedicated module
- **Better UX** - Users have single entry point and seamless navigation

**Negative:**

- **Additional Complexity** - One more service to deploy and manage
- **External Dependency** - Keycloak becomes critical infrastructure
- **Learning Curve** - Team needs to understand OAuth 2.0/OIDC flows
- **Network Latency** - Additional hop for token validation
- **Migration Effort** - Existing users need to be migrated to Keycloak
- **Frontend Changes** - All frontends need token-based auth implementation

## Alternatives Considered

### Option 1: Build Custom Authentication in Each Frontend

**Why not chosen:**

- Duplicate authentication logic across three frontends
- No single sign-on capability
- Harder to maintain consistent security policies
- More development effort

### Option 2: Simple JWT Implementation in Backend

**Why not chosen:**

- Would need to build user management, password reset, 2FA ourselves
- No built-in admin interface for user management  
- Limited features compared to mature IAM solution
- Still need to handle token refresh, session management

### Option 3: Use Spring Security Only (No Keycloak)

**Why not chosen:**

- More coupled to Spring ecosystem
- Less suitable for multi-frontend architecture
- Harder to implement SSO
- Limited built-in features

## Implementation Plan

### Phase 1: Infrastructure

- Deploy Keycloak container
- Create SLMS realm and configure clients
- Set up basic User Management Module structure
- Implement landing page with basic routing

### Phase 2: Integration

- Complete User Management Module implementation
- Deploy Redis container for session caching
- Integrate with existing backend modules via BFFs
- Implement token validation across all endpoints
- Add role-based routing logic

### Phase 3: Migration

- Migrate existing users to Keycloak
- Implement full SSO across all frontends
- Add security audit logging
- Complete landing page features

## Related Decisions

- ADR-0001 – Initial System Architecture (Sprint 1)
- ADR-0002 – Data Model Definition (Sprint 2)
- ADR-0003 – Architecture Update (Sprint 2)

## Notes

- User Management Module follows same modular pattern as other domain modules
- Keycloak configuration will be version-controlled as JSON export
- Landing page will use same Next.js stack as other frontends
- Session data cached in Redis for performance (with PostgreSQL as persistent backup)
- Token expiry set to 30 minutes with automatic refresh
- Each frontend gets its own Keycloak client configuration
- Role names in Keycloak match existing SLMS roles (MANAGER, WAREHOUSE, CSR, DRIVER, CUSTOMER)
- Security audit logs retained for 90 days minimum for compliance
