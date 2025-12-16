#!/bin/bash

# Script de Restore para PostgreSQL
# Este script restaura um backup específico da base de dados PostgreSQL

set -e

# Variáveis de ambiente (com valores padrão)
POSTGRES_HOST=${POSTGRES_HOST:-postgres}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
POSTGRES_DB=${POSTGRES_DB:-slms}
POSTGRES_USER=${POSTGRES_USER:-postgres}
BACKUP_DIR=${BACKUP_DIR:-/backups}

# Verificar se foi fornecido um arquivo de backup
if [ -z "$1" ]; then
    echo "=========================================="
    echo "Uso: $0 <arquivo_backup>"
    echo "=========================================="
    echo "Backups disponíveis em $BACKUP_DIR:"
    echo "=========================================="
    ls -lh "$BACKUP_DIR"/backup_*.sql.gz 2>/dev/null || echo "Nenhum backup encontrado."
    echo "=========================================="
    echo ""
    echo "Exemplo: $0 backup_slms_20240129_120000.sql.gz"
    exit 1
fi

BACKUP_FILE="$1"

# Verificar se o arquivo é um caminho absoluto ou relativo
if [[ "$BACKUP_FILE" != /* ]]; then
    BACKUP_FILE="$BACKUP_DIR/$BACKUP_FILE"
fi

# Verificar se o arquivo de backup existe
if [ ! -f "$BACKUP_FILE" ]; then
    echo "=========================================="
    echo "✗ Erro: Arquivo de backup não encontrado!"
    echo "Arquivo: $BACKUP_FILE"
    echo "=========================================="
    exit 1
fi

echo "=========================================="
echo "Restaurando backup da base de dados"
echo "=========================================="
echo "Host: $POSTGRES_HOST"
echo "Database: $POSTGRES_DB"
echo "Arquivo: $BACKUP_FILE"
echo "=========================================="

# Aguardar a base de dados estar disponível
echo "Aguardando a base de dados ficar disponível..."
until PGPASSWORD=$POSTGRES_PASSWORD pg_isready -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" > /dev/null 2>&1; do
    echo "Base de dados não está pronta. Tentando novamente em 5 segundos..."
    sleep 5
done

echo "Base de dados disponível."

# Confirmação antes de restaurar
echo "=========================================="
echo "⚠️  ATENÇÃO: Esta operação irá substituir"
echo "   todos os dados atuais da base de dados!"
echo "=========================================="
read -p "Tem certeza que deseja continuar? (sim/não): " -r
echo

if [[ ! $REPLY =~ ^[Ss][Ii][Mm]$ ]]; then
    echo "Operação cancelada."
    exit 0
fi

echo "Iniciando restore..."

# Desconectar usuários conectados (opcional, pode causar problemas em produção)
# PGPASSWORD=$POSTGRES_PASSWORD psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$POSTGRES_DB' AND pid <> pg_backend_pid();"

# Dropar e recriar a base de dados (cuidado em produção!)
echo "Recriando base de dados..."
PGPASSWORD=$POSTGRES_PASSWORD psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres -c "DROP DATABASE IF EXISTS $POSTGRES_DB;"
PGPASSWORD=$POSTGRES_PASSWORD psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d postgres -c "CREATE DATABASE $POSTGRES_DB;"

# Restaurar backup
echo "Restaurando dados..."
gunzip -c "$BACKUP_FILE" | PGPASSWORD=$POSTGRES_PASSWORD psql \
    -h "$POSTGRES_HOST" \
    -p "$POSTGRES_PORT" \
    -U "$POSTGRES_USER" \
    -d "$POSTGRES_DB" \
    --single-transaction

# Verificar resultado
if [ $? -eq 0 ]; then
    echo "=========================================="
    echo "✓ Restore concluído com sucesso!"
    echo "=========================================="
else
    echo "=========================================="
    echo "✗ Erro ao restaurar backup!"
    echo "=========================================="
    exit 1
fi
