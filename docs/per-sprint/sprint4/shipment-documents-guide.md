# Guia de Documentos de Shipment - Implementação

## 📦 Documentos Implementados

### 1. ✅ **Documento de Resumo do Shipment** (IMPLEMENTADO)
**O que é**: Documento de texto com resumo completo do shipment incluindo origem, destinos e informações do carrier.

**Geração**: Automática na criação do shipment  
**Formato**: TXT (texto simples)  
**Localização MinIO**: `shipments/{shipmentId}/shipment-summary`

### 2. ✅ **Customs Documents / Documentos Alfandegários** (IMPLEMENTADO) 🌍
**O que é**: Declarações aduaneiras oficiais geradas automaticamente para envios internacionais.

**Geração**: Automática via Shippo API quando origem ≠ destino (país diferente)  
**Formato**: PDF (oficial da transportadora)  
**Localização MinIO**: `shipments/{shipmentId}/orders/{orderId}/customs-declaration-{orderId}.pdf`  
**API**: Shippo `commercial_invoice_url`

---

## 📋 Estrutura de Armazenamento Implementada

```
MinIO Bucket: documents/
└── shipments/
    └── {shipmentId}/
        ├── shipment-summary                                ← ✅ Resumo do shipment
        └── orders/
            └── {orderId}/
                ├── carrier-label-{orderId}.pdf             ← ✅ Label da order
                └── customs-declaration-{orderId}.pdf       ← ✅ Documentos alfandegários (se internacional)
```

---

## 📄 Exemplo de Conteúdo do Shipment Summary

```text
===========================================
     SHIPMENT SUMMARY / RESUMO DO ENVIO     
===========================================

SHIPMENT INFORMATION / INFORMAÇÃO DO ENVIO
------------------------------------------
Shipment Code / Código: SHIP-001
Carrier / Transportadora: UPS (ups)
Driver / Motorista: driver-uuid-123
Total Orders / Total Encomendas: 3
Total Weight / Peso Total: 5.50 kg
Estimated Delivery / Entrega Prevista: 2025-11-15

ORIGIN / ORIGEM
------------------------------------------
Warehouse / Armazém: SLMS Main Warehouse
Address / Morada: Rua A 1
City / Cidade: Porto
Postal Code / Código Postal: 4000-001
Country / País: PT

DESTINATIONS / DESTINOS
------------------------------------------
1. Order / Encomenda: ORD-001
   Address / Morada: Rua Cliente 123
   City / Cidade: Lisboa
   Postal Code / Código: 1000-001
   Tracking / Rastreio: 1Z999AA10123456784

2. Order / Encomenda: ORD-002
   Address / Morada: Av. Customer 456
   City / Cidade: Coimbra
   Postal Code / Código: 3000-001
   Tracking / Rastreio: 1Z999AA10123456785

3. Order / Encomenda: ORD-003
   Address / Morada: Travessa do Cliente 789
   City / Cidade: Braga
   Postal Code / Código: 4700-001
   Tracking / Rastreio: 1Z999AA10123456786

===========================================
Generated / Gerado: 2025-11-12T14:30:00
===========================================
```

---

## 🚀 Como Funciona

### 1. Geração Automática
Quando um shipment é criado com orders atribuídas, o `ShipmentLabelGenerationService` automaticamente:
1. Gera **labels individuais** para cada order via Shippo API
2. Cria um **documento de resumo do shipment** com todas as informações

### 2. Conteúdo dos Documentos

**Shipment Summary (TXT):**
- ✅ Código do shipment
- ✅ Informações da transportadora (carrier)
- ✅ ID do motorista (driver)
- ✅ Total de orders e peso total
- ✅ Data estimada de entrega
- ✅ **Origem**: Informações completas do warehouse
- ✅ **Destinos**: Lista de todas as orders com endereços e tracking numbers

**Customs Documents (PDF):**
- ✅ Formulário alfandegário oficial gerado pelo Shippo
- ✅ Inclui declaração de conteúdo, valor, país de origem/destino
- ✅ Necessário para desalfandegamento internacional
- ✅ Só é gerado quando `origin_country != destination_country`

### 3. Metadados Armazenados

**Shipment Summary:**
```json
{
  "document-type": "SHIPMENT_SUMMARY",
  "carrier-code": "ups",
  "total-orders": "3",
  "driver-id": "driver-uuid-123"
}
```

**Customs Declaration:**
```json
{
  "document-type": "CUSTOMS_DECLARATION",
  "order-reference": "ORD-001",
  "carrier-code": "ups",
  "shippo-transaction-id": "txn_abc123",
  "tracking-number": "1Z999AA10123456784"
}
```

---

## 🔧 Implementação Técnica

### Arquivo Modificado
📁 `ShipmentLabelGenerationService.java`

### Método Principal
```java
private void generateShipmentSummary(Shipment shipment, Carrier carrier, List<Order> orders)
```

**Chamado automaticamente após gerar todas as labels das orders.**

---

## 📡 Endpoints de Acesso

### Listar documentos do shipment (por order)
```
GET /api/warehouse/orders/{orderId}/shipment-documents
```

**Response**:
```json
[
  {
    "shipmentId": 5,
    "orderId": 8,
    "documentId": "carrier-label-8.pdf",
    "fileName": "Carrier Label - ORD-001.pdf",
    "contentType": "application/pdf",
    "size": 45678,
    "url": "http://localhost:9000/documents/shipments/5/orders/8/carrier-label-8.pdf"
  },
  {
    "shipmentId": 5,
    "orderId": null,
    "documentId": "shipment-summary",
    "fileName": "Shipment-Summary-SHIP-001.txt",
    "contentType": "text/plain; charset=utf-8",
    "size": 1234,
    "url": "http://localhost:9000/documents/shipments/5/shipment-summary"
  }
]
```

### Download de documento
```
GET /api/warehouse/orders/{orderId}/shipment-documents/{documentId}/download
```

**Response**: Conteúdo do arquivo (PDF ou TXT)

---

## ✅ Checklist de Implementação

**Shipment Summary:**
- [x] Documento de Resumo do Shipment gerado automaticamente
- [x] Armazenamento em MinIO no caminho `shipments/{shipmentId}/shipment-summary`
- [x] Formato TXT com informações bilíngues (PT/EN)
- [x] Geração automática ao criar shipment com orders
- [x] Metadados incluídos para busca e filtragem
- [x] Integração com `ShipmentDocumentService` existente

**Customs Documents:**
- [x] Adicionado campo `commercialInvoiceUrl` ao `ShippoLabelResponse`
- [x] Atualizado mapping no `ShippoApiClient` para capturar URL do customs document
- [x] Método `generateCustomsDocumentIfAvailable()` implementado
- [x] Download automático do PDF alfandegário via Shippo
- [x] Armazenamento em `shipments/{id}/orders/{orderId}/customs-declaration.pdf`
- [x] Geração automática para envios internacionais (quando disponível)
- [x] Logging apropriado para debug e troubleshooting

---

## 🎯 Fluxo Completo

```
1. Criar Shipment
   ↓
2. Atribuir Orders ao Shipment
   ↓
3. ShipmentService.create() chama
   ↓
4. ShipmentLabelGenerationService.generateCarrierDocuments()
   ↓
5. Para cada order:
   - Calcular rates via Shippo
   - Comprar label via Shippo API
   - Guardar label em MinIO: shipments/{id}/orders/{orderId}/carrier-label.pdf
   ↓
   - Verificar se há commercialInvoiceUrl (envio internacional)
   - Se sim: Download customs document via Shippo
   - Guardar em MinIO: shipments/{id}/orders/{orderId}/customs-declaration.pdf
   ↓
6. Após todas as labels:
   - Gerar documento de resumo do shipment
   - Guardar em MinIO: shipments/{id}/shipment-summary
   ↓
7. ✅ Documentos disponíveis para download
```

---

## 🔮 Extensões Futuras (Opcional)

Se precisares de documentos adicionais futuramente:

### 1. **SCAN Forms / Manifests** 📋
- Manifesto consolidado para entrega ao carrier
- API: `POST https://api.goshippo.com/manifests/`
- Formato: PDF

### 2. **Customs Documents** 🌍
- Declarações aduaneiras para envios internacionais
- Automático em labels internacionais via Shippo
- Formato: PDF

### 3. **Packing Slips** 📄
- Lista de conteúdos do shipment
- API: ShipEngine `GET /v1/labels/{id}/forms/packing_slip`
- Formato: PDF

### 4. **Return Labels** 🔄
- Etiquetas de devolução
- API: Shippo (inverter origem/destino)
- Formato: PDF

---

## 📚 Referências

- **Shippo API Docs**: https://docs.goshippo.com/
- **ShipEngine API Docs**: https://www.shipengine.com/docs/
- **Projeto Docs**: `/docs/per-sprint/sprint3/carrier-module-readme.md`

---

## 🌍 Quando são Gerados Customs Documents?

Os documentos alfandegários (customs documents) são **gerados automaticamente pelo Shippo** apenas quando:

### Condições para Geração:
1. ✅ **Envio Internacional**: O país de origem ≠ país de destino
2. ✅ **Carrier Suporta**: A transportadora (UPS, FedEx, DHL, etc.) suporta envios internacionais
3. ✅ **Shippo Gera Automaticamente**: Ao comprar a label, o Shippo detecta envio internacional e gera o documento

### Exemplos:
- ✅ **PT → ES**: Gera customs document (países diferentes)
- ✅ **PT → UK**: Gera customs document (após Brexit)
- ✅ **PT → US**: Gera customs document
- ❌ **PT → PT**: NÃO gera (envio nacional)
- ❌ **ES → ES**: NÃO gera (envio nacional)

### Fluxo Técnico:
```java
// 1. Comprar label via Shippo
ShippoLabelResponse labelResponse = shippoApiClient.purchaseLabel(rateId, "PDF");

// 2. Shippo detecta envio internacional e preenche commercialInvoiceUrl
if (labelResponse.getCommercialInvoiceUrl() != null) {
    // 3. Download automático do PDF
    byte[] customsDoc = shippoApiClient.downloadLabel(labelResponse.getCommercialInvoiceUrl());
    
    // 4. Guardar no MinIO
    shipmentDocumentService.saveDocument(...);
}
```

**Nota**: O Shippo gera o documento baseado nas informações de origem/destino fornecidas no `buildRateRequest()`.
