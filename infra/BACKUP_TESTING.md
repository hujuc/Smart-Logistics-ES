# Guia de Teste do Sistema de Backup

Este guia ajuda a verificar se o sistema de backup está configurado e funcionando corretamente.

## ✅ Pré-requisitos

- Docker e Docker Compose instalados
- Projeto configurado com `.env` correto
- Serviços em execução

## 🧪 Teste 1: Verificar Serviço de Backup

```bash
# Verificar se o container de backup está em execução
docker compose -f infra/docker-compose.yaml ps backup

# Deve mostrar:
# NAME           STATUS
# slms-backup    Up X minutes
```

**✓ Esperado**: Container `slms-backup` está "Up"

## 🧪 Teste 2: Criar Backup Manual

```bash
# Criar um backup de teste
make backup

# OU usando o script CLI
./infra/scripts/backup-cli.sh backup
```

**✓ Esperado**: 
- Mensagem "Backup criado com sucesso!"
- Sem erros no output

## 🧪 Teste 3: Verificar Backup Criado

```bash
# Listar backups
make backup-list

# OU
./infra/scripts/backup-cli.sh list
```

**✓ Esperado**: 
- Pelo menos um arquivo `backup_slms_TIMESTAMP.sql.gz`
- Tamanho do arquivo > 0 bytes

## 🧪 Teste 4: Verificar Conteúdo do Backup

```bash
# Verificar integridade do backup
docker compose -f infra/docker-compose.yaml exec backup \
  gunzip -t /backups/backup_slms_*.sql.gz

# Visualizar primeiras linhas do backup
docker compose -f infra/docker-compose.yaml exec backup \
  sh -c "gunzip -c /backups/backup_slms_*.sql.gz | head -20"
```

**✓ Esperado**: 
- Sem erros de corrupção
- Output mostra SQL válido (CREATE TABLE, INSERT, etc.)

## 🧪 Teste 5: Download de Backup

```bash
# Listar backups disponíveis
make backup-list

# Fazer download de um backup
make backup-download BACKUP_FILE=backup_slms_TIMESTAMP.sql.gz

# Verificar arquivo local
ls -lh backup_slms_*.sql.gz
```

**✓ Esperado**: 
- Arquivo baixado com sucesso
- Tamanho igual ao do container

## 🧪 Teste 6: Teste de Restore (Ambiente de DEV)

⚠️ **ATENÇÃO**: Execute apenas em ambiente de desenvolvimento!

```bash
# 1. Criar dados de teste na BD
docker compose -f infra/docker-compose.yaml exec postgres \
  psql -U slms -d slms -c "CREATE TABLE IF NOT EXISTS test_backup (id INT, data TEXT);"

docker compose -f infra/docker-compose.yaml exec postgres \
  psql -U slms -d slms -c "INSERT INTO test_backup VALUES (1, 'dados_originais');"

# 2. Criar backup com os dados
make backup

# 3. Modificar dados
docker compose -f infra/docker-compose.yaml exec postgres \
  psql -U slms -d slms -c "UPDATE test_backup SET data = 'dados_modificados' WHERE id = 1;"

# 4. Verificar modificação
docker compose -f infra/docker-compose.yaml exec postgres \
  psql -U slms -d slms -c "SELECT * FROM test_backup;"

# Deve mostrar: 1 | dados_modificados

# 5. Restaurar backup (use o nome do backup criado no passo 2)
make backup-list
make backup-restore BACKUP_FILE=backup_slms_TIMESTAMP.sql.gz

# 6. Verificar restauração
docker compose -f infra/docker-compose.yaml exec postgres \
  psql -U slms -d slms -c "SELECT * FROM test_backup;"

# Deve mostrar: 1 | dados_originais

# 7. Limpar tabela de teste
docker compose -f infra/docker-compose.yaml exec postgres \
  psql -U slms -d slms -c "DROP TABLE IF EXISTS test_backup;"
```

**✓ Esperado**: 
- Dados restaurados com sucesso
- Valores voltam ao estado do backup

## 🧪 Teste 7: Verificar Logs

```bash
# Ver logs do serviço de backup
make backup-logs

# OU pressionar Ctrl+C para sair
```

**✓ Esperado**: 
- Logs mostram inicialização do serviço
- Sem erros críticos

## 🧪 Teste 8: Verificar Espaço

```bash
# Verificar espaço usado
make backup-space

# OU
./infra/scripts/backup-cli.sh space
```

**✓ Esperado**: 
- Output mostra tamanho total de backups
- Lista individual de cada backup

## 🧪 Teste 9: Script CLI Interativo

```bash
# Testar todas as funções do CLI
./infra/scripts/backup-cli.sh help
./infra/scripts/backup-cli.sh backup
./infra/scripts/backup-cli.sh list
./infra/scripts/backup-cli.sh space
```

**✓ Esperado**: 
- Todos os comandos executam sem erros
- Interface amigável com cores e emojis

## 🧪 Teste 10: Limpeza de Backups Antigos

```bash
# Criar backup antigo de teste (modificar timestamp)
docker compose -f infra/docker-compose.yaml exec backup sh -c \
  "cd /backups && touch -t 202301010000 backup_slms_old.sql.gz"

# Verificar que existe
make backup-list

# Executar limpeza (remove backups > 30 dias)
make backup-clean

# Verificar que foi removido
make backup-list
```

**✓ Esperado**: 
- Backup antigo é removido
- Backups recentes permanecem

## 🧪 Teste 11: Backup Automático (Opcional)

Para testar o backup automático sem esperar pelas 2:00 AM:

```bash
# Modificar temporariamente o docker-compose.yaml para executar imediatamente
# OU aguardar até o horário agendado e verificar:

# Ver logs na manhã seguinte
docker compose -f infra/docker-compose.yaml logs backup | grep -A 20 "Iniciando backup"

# Verificar se novo backup foi criado
make backup-list
```

**✓ Esperado**: 
- Novo backup criado automaticamente
- Logs mostram execução bem-sucedida

## 📋 Checklist Final

Marque os testes concluídos:

- [ ] Teste 1: Serviço de backup em execução
- [ ] Teste 2: Backup manual funciona
- [ ] Teste 3: Backup aparece na listagem
- [ ] Teste 4: Backup não está corrompido
- [ ] Teste 5: Download de backup funciona
- [ ] Teste 6: Restore funciona corretamente
- [ ] Teste 7: Logs estão acessíveis
- [ ] Teste 8: Verificação de espaço funciona
- [ ] Teste 9: Script CLI funciona
- [ ] Teste 10: Limpeza automática funciona
- [ ] Teste 11: Backup automático funciona (opcional)

## 🐛 Troubleshooting

### Problema: Container de backup não inicia

```bash
# Verificar logs
docker compose -f infra/docker-compose.yaml logs backup

# Verificar configuração
docker compose -f infra/docker-compose.yaml config | grep -A 20 backup
```

### Problema: Backup falha com erro de permissão

```bash
# Verificar permissões do volume
docker volume inspect backup-data

# Recriar volume se necessário
docker compose -f infra/docker-compose.yaml down
docker volume rm backup-data
docker compose -f infra/docker-compose.yaml up -d
```

### Problema: PostgreSQL não está acessível

```bash
# Verificar se postgres está rodando
docker compose -f infra/docker-compose.yaml ps postgres

# Testar conexão
docker compose -f infra/docker-compose.yaml exec backup \
  pg_isready -h postgres -U slms -d slms
```

### Problema: Backup muito grande

```bash
# Verificar tamanho da base de dados
docker compose -f infra/docker-compose.yaml exec postgres \
  psql -U slms -d slms -c "SELECT pg_size_pretty(pg_database_size('slms'));"

# Considerar:
# - Arquivar dados antigos
# - Aumentar compressão
# - Backups incrementais (implementação futura)
```

## 📞 Suporte

Se algum teste falhar:

1. Verificar logs: `make backup-logs`
2. Verificar configuração: `.env` está correto?
3. Verificar conectividade: PostgreSQL está acessível?
4. Consultar documentação: `infra/docker/backup/README.md`
5. Verificar disaster recovery: `infra/DISASTER_RECOVERY.md`

## 🎯 Próximos Passos

Após todos os testes passarem:

1. ✅ Configurar backup externo (S3, cloud storage)
2. ✅ Agendar testes de restore mensais
3. ✅ Configurar alertas de falha de backup
4. ✅ Documentar RPO/RTO para a equipa
5. ✅ Treinar equipa em procedimentos de recovery
