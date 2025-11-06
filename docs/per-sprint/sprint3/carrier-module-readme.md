# Carrier Module & External API Integrations

This backend integrates with Shippo and ShipEngine APIs to manage carriers and shipping operations. The system maintains a local `carriers` table and synchronizes data from external APIs, filtering to only include carriers with ShipEngine integration.

## Environment Variables

Set these in `infra/.env`:

- `SHIPPO_API_KEY` – Shippo sandbox token (format `shippo_test_...`)
- `SHIPPO_BASE_URL` – optional, defaults to `https://api.goshippo.com`
- `SHIPENGINE_API_KEY` – ShipEngine sandbox key (format `TEST_...`)
- `SANDBOX_API_KEY` – optional fallback for ShipEngine
- `SHIPENGINE_BASE_URL` – optional, defaults to `https://api.shipengine.com/v1`

## API Integration Files

### ShipEngine Integration
- **Client**: `ShipEngineApiClient.java`
- **Endpoints**: Fetches carriers and service levels
- **Test Account Carriers**: UPS (`se-338909`), FedEx (`se-338910`)

### Shippo Integration  
- **Client**: `ShippoApiClient.java`
- **Endpoints**: Carrier accounts, address validation, rate calculation
- **Test Account Carriers**: UPS, USPS, Canada Post, DHL Express, and others

### Integration Service
- **Service**: `CarrierIntegrationService.java`
- **Function**: Merges data from both APIs, filters to ShipEngine-enabled carriers only
- **Logic**: Only carriers with `shipEngineCarrierId != null` are synchronized

## Carrier Endpoints

All endpoints are under `/api/admin/carriers`:

### CRUD Operations
- `GET /` – List all carriers stored in database
- `GET /{id}` – Fetch specific carrier by ID
- `POST /` – Create new carrier manually
- `PUT /{id}` – Update existing carrier
- `DELETE /{id}` – Remove carrier

### External API Integration
- `GET /integrations/snapshot` – Fetch live data from Shippo+ShipEngine without storing
- `POST /integrations/sync` – Synchronize carriers from external APIs to local database

### Shipping Operations
- `POST /addresses/validate` – Validate and correct shipping addresses
- `POST /rates/calculate` – Calculate shipping rates, costs, and delivery times between two addresses

### Tested Endpoints Results

**Integration Snapshot** (`GET /integrations/snapshot`):
- Returns 2 carriers: UPS (both APIs), FedEx (ShipEngine only)
- UPS: Has both Shippo ID and ShipEngine ID
- FedEx: Has only ShipEngine ID (no Shippo in test account)

**Synchronization** (`POST /integrations/sync`):
- Successfully syncs 2 carriers to database
- Filters out carriers without ShipEngine integration
- Creates local records with external API references

## Warehouse Management

Endpoints under `/api/admin/warehouses`:

- `GET /` – List warehouses
- `POST /` – Create warehouse (requires `code`, `name`, address fields)
- `GET /{id}` – Get warehouse details

**Tested**: Created test warehouse (ID: 1) for shipping operations.

## Order Management

Endpoints under `/api/admin/orders`:

- `GET /` – List all orders
- `POST /` – Create order with carrier assignment
- `GET /{id}` – Get order details  
- `PUT /{id}` – Update order
- `POST /{id}/collect` – Confirm pickup (changes status)

### Order-Carrier Integration

Orders can be linked to carriers via `carrierId` field:

```json
{
  "reference": "ORDER-TEST-001",
  "description": "Test order description",
  "shippingAddress": "Customer address",
  "warehouseId": 1,
  "carrierId": 16
}
```

**Tested Orders**:
- Order 1: UPS carrier (ID: 16)
- Order 2: FedEx carrier (ID: 17)

## External API Capabilities

### Shippo API Features (Working)

1. **Address Validation**
   ```bash
   POST https://api.goshippo.com/addresses/
   ```
   - [OK] Validates and corrects addresses
   - [OK] Returns validation results with corrections

2. **Rate Calculation**
   ```bash
   POST https://api.goshippo.com/shipments/
   ```
   - [OK] Returns shipping rates from multiple carriers
   - [OK] USPS, Canada Post rates working
   - [WARN]️ UPS rates limited in test account

3. **Carrier Accounts**
   ```bash
   GET https://api.goshippo.com/carrier_accounts/
   ```
   - [OK] Returns available carrier accounts
   - [OK] Integrated via `fetchCarrierAccounts()` method

### ShipEngine API Features (Working)

1. **Address Validation**
   ```bash
   POST https://api.shipengine.com/v1/addresses/validate
   ```
   - [OK] Validates and standardizes addresses
   - [OK] Returns validation status and normalized addresses
   - [OK] Provides address quality scores
   - [OK] Identifies residential vs commercial addresses

2. **Carrier Management**
   - [OK] Fetches UPS and FedEx carrier details
   - [OK] Provides comprehensive service levels
   - [OK] Returns 23 UPS services, 26 FedEx services

3. **Account Information**
   - [OK] Provides carrier IDs and account numbers
   - [OK] Active status and contact information

## Current Integration Status

### Synchronized Carriers
| Carrier | ShipEngine ID | Shippo ID | Status |
|---------|---------------|-----------|---------|
| UPS | se-338909 | fb695995b21f4f80b55ea6b9d73f2a1c | [OK] Full Integration |
| FedEx | se-338910 | null | [OK] ShipEngine Only |

### Filtering Logic
- **Inclusion Criteria**: Must have `shipEngineCarrierId != null`
- **Exclusion**: Shippo-only carriers are filtered out
- **Result**: Only 2 carriers synchronized (UPS, FedEx)

## Testing Workflow

1. **Environment Setup**: Configure API keys in `infra/.env`
2. **Service Status**: Verify backend is running with security disabled
3. **Integration Test**: `GET /api/admin/carriers/integrations/snapshot`
4. **Synchronization**: `POST /api/admin/carriers/integrations/sync`
5. **Warehouse Setup**: Create test warehouse for shipping
6. **Order Creation**: Create orders linked to carriers
7. **External API Testing**: Direct API calls for validation/rates

## Security Configuration

Authentication is temporarily disabled for API testing:
- Security configuration allows all requests
- OAuth2/JWT temporarily commented out
- Suitable for development/testing only

## Typical Usage Workflow

1. Set environment variables for external APIs
2. Start backend application
3. Test integration snapshot to verify API connectivity
4. Synchronize carriers to populate local database
5. Create warehouses for shipping origins
6. Create orders referencing carrier IDs
7. Use external APIs for address validation and rate calculation

---

# Módulo de Carriers e Integrações com APIs Externas

Este backend integra com as APIs do Shippo e ShipEngine para gerir transportadoras e operações de envio. O sistema mantém uma tabela local `carriers` e sincroniza dados das APIs externas, filtrando para incluir apenas transportadoras com integração ShipEngine.

## Variáveis de Ambiente

Definir em `infra/.env`:

- `SHIPPO_API_KEY` – Token sandbox do Shippo (formato `shippo_test_...`)
- `SHIPPO_BASE_URL` – opcional, padrão `https://api.goshippo.com`
- `SHIPENGINE_API_KEY` – Chave sandbox do ShipEngine (formato `TEST_...`)
- `SANDBOX_API_KEY` – fallback opcional para ShipEngine
- `SHIPENGINE_BASE_URL` – opcional, padrão `https://api.shipengine.com/v1`

## Ficheiros de Integração API

### Integração ShipEngine
- **Cliente**: `ShipEngineApiClient.java`
- **Endpoints**: Obtém transportadoras e níveis de serviço
- **Transportadoras Conta Teste**: UPS (`se-338909`), FedEx (`se-338910`)

### Integração Shippo
- **Cliente**: `ShippoApiClient.java`
- **Endpoints**: Contas de transportadoras, validação de endereços, cálculo de tarifas
- **Transportadoras Conta Teste**: UPS, USPS, Canada Post, DHL Express, e outras

### Serviço de Integração
- **Serviço**: `CarrierIntegrationService.java`
- **Função**: Combina dados de ambas as APIs, filtra apenas transportadoras com ShipEngine
- **Lógica**: Apenas transportadoras com `shipEngineCarrierId != null` são sincronizadas

## Endpoints de Carriers

Todos os endpoints estão em `/api/admin/carriers`:

### Operações CRUD
- `GET /` – Listar todas as transportadoras na base de dados
- `GET /{id}` – Obter transportadora específica por ID
- `POST /` – Criar nova transportadora manualmente
- `PUT /{id}` – Atualizar transportadora existente
- `DELETE /{id}` – Remover transportadora

### Integração API Externa
- `GET /integrations/snapshot` – Obter dados em tempo real do Shippo+ShipEngine sem armazenar
- `POST /integrations/sync` – Sincronizar transportadoras das APIs externas para a base de dados

### Operações de Envio
- `POST /addresses/validate` – Validar e corrigir endereços de envio
- `POST /rates/calculate` – Calcular tarifas, custos e tempos de entrega entre dois endereços

### Resultados dos Endpoints Testados

**Snapshot de Integração** (`GET /integrations/snapshot`):
- Retorna 2 transportadoras: UPS (ambas APIs), FedEx (apenas ShipEngine)
- UPS: Tem ID Shippo e ID ShipEngine
- FedEx: Tem apenas ID ShipEngine (sem Shippo na conta teste)

**Sincronização** (`POST /integrations/sync`):
- Sincroniza com sucesso 2 transportadoras para a base de dados
- Filtra transportadoras sem integração ShipEngine
- Cria registos locais com referências de APIs externas

## Gestão de Armazéns

Endpoints em `/api/admin/warehouses`:

- `GET /` – Listar armazéns
- `POST /` – Criar armazém (requer `code`, `name`, campos de endereço)
- `GET /{id}` – Obter detalhes do armazém

**Testado**: Criado armazém de teste (ID: 1) para operações de envio.

## Gestão de Encomendas

Endpoints em `/api/admin/orders`:

- `GET /` – Listar todas as encomendas
- `POST /` – Criar encomenda com atribuição de transportadora
- `GET /{id}` – Obter detalhes da encomenda
- `PUT /{id}` – Atualizar encomenda
- `POST /{id}/collect` – Confirmar recolha (altera estado)

### Integração Encomenda-Transportadora

Encomendas podem ser ligadas a transportadoras via campo `carrierId`:

```json
{
  "reference": "ORDER-TEST-001",
  "description": "Descrição da encomenda teste",
  "shippingAddress": "Endereço do cliente",
  "warehouseId": 1,
  "carrierId": 16
}
```

**Encomendas Testadas**:
- Encomenda 1: Transportadora UPS (ID: 16)
- Encomenda 2: Transportadora FedEx (ID: 17)

## Capacidades das APIs Externas

### Funcionalidades API Shippo (Funcionais)

1. **Validação de Endereços**
   ```bash
   POST https://api.goshippo.com/addresses/
   ```
   - [OK] Valida e corrige endereços
   - [OK] Retorna resultados de validação com correções

2. **Cálculo de Tarifas**
   ```bash
   POST https://api.goshippo.com/shipments/
   ```
   - [OK] Retorna tarifas de envio de múltiplas transportadoras
   - [OK] Tarifas USPS, Canada Post funcionais
   - [WARN]️ Tarifas UPS limitadas na conta teste

3. **Contas de Transportadoras**
   ```bash
   GET https://api.goshippo.com/carrier_accounts/
   ```
   - [OK] Retorna contas de transportadoras disponíveis
   - [OK] Integrado via método `fetchCarrierAccounts()`

### Funcionalidades API ShipEngine (Funcionais)

1. **Validação de Endereços**
   ```bash
   POST https://api.shipengine.com/v1/addresses/validate
   ```
   - [OK] Valida e normaliza endereços
   - [OK] Retorna estado de validação e endereços normalizados
   - [OK] Fornece scores de qualidade do endereço
   - [OK] Identifica endereços residenciais vs comerciais

2. **Gestão de Transportadoras**
   - [OK] Obtém detalhes das transportadoras UPS e FedEx
   - [OK] Fornece níveis de serviço abrangentes
   - [OK] Retorna 23 serviços UPS, 26 serviços FedEx

3. **Informação de Contas**
   - [OK] Fornece IDs de transportadoras e números de conta
   - [OK] Estado ativo e informações de contacto

## Estado Atual da Integração

### Transportadoras Sincronizadas
| Transportadora | ID ShipEngine | ID Shippo | Estado |
|----------------|---------------|-----------|---------|
| UPS | se-338909 | fb695995b21f4f80b55ea6b9d73f2a1c | [OK] Integração Completa |
| FedEx | se-338910 | null | [OK] Apenas ShipEngine |

### Lógica de Filtragem
- **Critério de Inclusão**: Deve ter `shipEngineCarrierId != null`
- **Exclusão**: Transportadoras apenas-Shippo são filtradas
- **Resultado**: Apenas 2 transportadoras sincronizadas (UPS, FedEx)

## Fluxo de Testes

1. **Configuração Ambiente**: Configurar chaves API em `infra/.env`
2. **Estado do Serviço**: Verificar backend em execução com segurança desativada
3. **Teste Integração**: `GET /api/admin/carriers/integrations/snapshot`
4. **Sincronização**: `POST /api/admin/carriers/integrations/sync`
5. **Configuração Armazém**: Criar armazém teste para envios
6. **Criação Encomendas**: Criar encomendas ligadas a transportadoras
7. **Teste APIs Externas**: Chamadas diretas da API para validação/tarifas

## Configuração de Segurança

Autenticação temporariamente desativada para testes da API:
- Configuração de segurança permite todos os pedidos
- OAuth2/JWT temporariamente comentado
- Adequado apenas para desenvolvimento/testes

## Fluxo de Utilização Típico

1. Definir variáveis de ambiente para APIs externas
2. Iniciar aplicação backend
3. Testar snapshot de integração para verificar conectividade da API
4. Sincronizar transportadoras para popular base de dados local
5. Criar armazéns para origens de envio
6. Criar encomendas referenciando IDs de transportadoras
7. Usar APIs externas para validação de endereços e cálculo de tarifas
