# Guia Rápido de Disaster Recovery

Este guia descreve como recuperar o sistema em caso de falha catastrófica da VM.

## 📋 Pré-requisitos

- Backups regulares da base de dados
- Acesso a uma nova VM com Docker instalado
- Cópia dos ficheiros de configuração (.env)

## 🚨 Cenário 1: Perda Total da VM

### Passo 1: Preparar Nova VM

```bash
# Instalar Docker e Docker Compose
sudo apt-get update
sudo apt-get install -y docker.io docker-compose git

# Clonar repositório
git clone <repo-url>
cd group-project-es2526_304
```

### Passo 2: Configurar Ambiente

```bash
# Copiar configurações
cd infra
cp .env.example .env

# Editar .env com as configurações corretas
nano .env
```

### Passo 3: Iniciar Infraestrutura

```bash
# Iniciar serviços (exceto aplicação principal)
docker compose up -d postgres backup keycloak minio

# Aguardar serviços ficarem prontos
docker compose logs -f
```

### Passo 4: Restaurar Backup

```bash
# Opção A: Se tem backup local
docker cp backup_slms_TIMESTAMP.sql.gz slms-backup:/backups/

# Opção B: Se backup está em cloud storage
# (download primeiro para a máquina)

# Restaurar
make backup-restore BACKUP_FILE=backup_slms_TIMESTAMP.sql.gz

# OU usar script CLI
./infra/scripts/backup-cli.sh restore backup_slms_TIMESTAMP.sql.gz
```

### Passo 5: Iniciar Aplicação Completa

```bash
# Iniciar todos os serviços
make prod-up

# Verificar logs
make prod-logs
```

## 🔄 Cenário 2: Corrupção de Base de Dados

### Passo 1: Parar Serviços que Usam a BD

```bash
docker compose stop backend admin-console customer-portal driver-pwa
```

### Passo 2: Criar Backup de Segurança (se possível)

```bash
# Tentar criar backup do estado atual
make backup
```

### Passo 3: Restaurar Último Backup Bom

```bash
# Listar backups
make backup-list

# Restaurar backup anterior à corrupção
make backup-restore BACKUP_FILE=<ultimo_backup_bom>
```

### Passo 4: Reiniciar Serviços

```bash
docker compose start backend admin-console customer-portal driver-pwa

# Verificar logs
docker compose logs -f backend
```

## 💾 Cenário 3: Migração para Nova VM

### Na VM Antiga:

```bash
# 1. Criar backup final
make backup

# 2. Download do backup
make backup-download BACKUP_FILE=<ultimo_backup>

# 3. Backup das configurações
cp infra/.env backup_env
cp -r infra/keycloak/realm-export.json backup_keycloak.json

# 4. Listar volumes importantes
docker volume ls

# 5. (Opcional) Backup de volumes MinIO se necessário
docker run --rm -v minio-data:/data -v $(pwd):/backup alpine tar czf /backup/minio-backup.tar.gz /data
```

### Na VM Nova:

```bash
# 1. Clonar e configurar
git clone <repo-url>
cd group-project-es2526_304/infra

# 2. Restaurar configurações
cp backup_env .env
cp backup_keycloak.json keycloak/realm-export.json

# 3. Iniciar infraestrutura
make prod-up

# 4. Aguardar serviços
sleep 30

# 5. Copiar backup
docker cp backup_slms_TIMESTAMP.sql.gz slms-backup:/backups/

# 6. Restaurar dados
make backup-restore BACKUP_FILE=backup_slms_TIMESTAMP.sql.gz

# 7. (Opcional) Restaurar MinIO
docker run --rm -v minio-data:/data -v $(pwd):/backup alpine tar xzf /backup/minio-backup.tar.gz -C /

# 8. Verificar
docker compose ps
curl http://localhost/api/health
```

## 🧪 Cenário 4: Teste de Restore (Recomendado Mensalmente)

```bash
# 1. Em ambiente de teste/desenvolvimento
cd infra
docker compose -f docker-compose.dev.yml up -d postgres backup

# 2. Copiar backup de produção
docker cp backup_production.sql.gz slms-backup:/backups/

# 3. Restaurar em ambiente de teste
docker compose -f docker-compose.dev.yml exec backup /usr/local/bin/restore.sh backup_production.sql.gz

# 4. Iniciar aplicação e testar
docker compose -f docker-compose.dev.yml up -d

# 5. Verificar funcionalidades críticas
# - Login
# - Operações CRUD
# - Geração de relatórios
```

## 📊 Checklist Pós-Recovery

Após qualquer operação de recovery, verificar:

- [ ] Base de dados acessível
- [ ] Keycloak funcional (autenticação)
- [ ] MinIO acessível (ficheiros)
- [ ] Backend responde a health checks
- [ ] Frontend apps carregam
- [ ] Login funciona
- [ ] Operações CRUD funcionam
- [ ] Relatórios podem ser gerados
- [ ] APIs externas conectam (se aplicável)
- [ ] Logs não mostram erros críticos

## 🔐 Boas Práticas

1. **Backups Regulares Externos**
   ```bash
   # Script para backup externo (executar via cron)
   #!/bin/bash
   make backup
   LATEST=$(docker compose exec backup ls -t /backups | head -1)
   make backup-download BACKUP_FILE=$LATEST
   # Upload para cloud storage
   aws s3 cp $LATEST s3://seu-bucket/backups/
   ```

2. **Teste de Recovery Mensal**
   - Agendar testes regulares
   - Documentar tempo de recovery
   - Atualizar procedimentos conforme necessário

3. **Documentação de Configurações**
   - Manter .env.example atualizado
   - Documentar configurações específicas
   - Versionar configurações de infraestrutura

4. **Monitorização**
   - Verificar logs de backup diariamente
   - Alertas se backup falhar
   - Monitorizar espaço em disco

## 🆘 Contactos de Emergência

Em caso de problemas durante recovery:

- **Equipa DevOps**: [adicionar contacto]
- **DBA**: [adicionar contacto]
- **Documentação**: [adicionar links]

## 📝 Logs Importantes

Localização de logs para troubleshooting:

```bash
# Logs de backup
docker compose logs backup

# Logs de PostgreSQL
docker compose logs postgres

# Logs de aplicação
docker compose logs backend

# Logs do sistema
journalctl -u docker
```

## ⏱️ RTO e RPO

**Recovery Time Objective (RTO)**: Tempo máximo aceitável para restaurar o serviço
- Objetivo: < 2 horas

**Recovery Point Objective (RPO)**: Quantidade máxima de dados que pode ser perdida
- Objetivo: < 24 horas (com backups diários)
- Melhorar: Implementar backups mais frequentes ou replicação em tempo real

## 🔮 Melhorias Futuras

- [ ] Backups incrementais para RPO menor
- [ ] Replicação em tempo real para outra VM
- [ ] Backups automáticos para cloud storage
- [ ] Testes de recovery automatizados
- [ ] Dashboard de monitorização de backups
