#!/bin/bash

# Quick Backup and Restore Script
# Este script fornece uma interface simples para operações de backup

set -e

COMPOSE_FILE="infra/docker-compose.yaml"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

show_help() {
    echo -e "${BLUE}╔════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║     Sistema de Backup - SLMS Database              ║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo "Uso: $0 [comando] [opções]"
    echo ""
    echo "Comandos disponíveis:"
    echo "  backup           - Criar backup manual da base de dados"
    echo "  list             - Listar todos os backups disponíveis"
    echo "  restore <file>   - Restaurar um backup específico"
    echo "  download <file>  - Fazer download de um backup"
    echo "  clean            - Remover backups antigos (>30 dias)"
    echo "  logs             - Ver logs do serviço de backup"
    echo "  space            - Verificar espaço usado"
    echo "  help             - Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0 backup"
    echo "  $0 list"
    echo "  $0 restore backup_slms_20240129_120000.sql.gz"
    echo "  $0 download backup_slms_20240129_120000.sql.gz"
    echo ""
}

create_backup() {
    echo -e "${BLUE}🔄 Criando backup manual...${NC}"
    docker compose -f "$COMPOSE_FILE" exec backup /usr/local/bin/backup.sh
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Backup criado com sucesso!${NC}"
    else
        echo -e "${RED}❌ Erro ao criar backup${NC}"
        exit 1
    fi
}

list_backups() {
    echo -e "${BLUE}📋 Backups disponíveis:${NC}"
    docker compose -f "$COMPOSE_FILE" exec backup ls -lh /backups
}

restore_backup() {
    if [ -z "$1" ]; then
        echo -e "${RED}❌ Erro: Especifique o arquivo de backup${NC}"
        echo "Uso: $0 restore <backup_file>"
        echo ""
        list_backups
        exit 1
    fi
    
    echo -e "${YELLOW}⚠️  ATENÇÃO: Esta operação irá substituir TODOS os dados atuais!${NC}"
    echo -e "Backup a restaurar: ${BLUE}$1${NC}"
    echo ""
    read -p "Tem certeza que deseja continuar? (sim/não): " -r
    echo
    
    if [[ ! $REPLY =~ ^[Ss][Ii][Mm]$ ]]; then
        echo -e "${YELLOW}Operação cancelada.${NC}"
        exit 0
    fi
    
    echo -e "${BLUE}🔄 Restaurando backup...${NC}"
    docker compose -f "$COMPOSE_FILE" exec backup /usr/local/bin/restore.sh "$1"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Restore concluído com sucesso!${NC}"
    else
        echo -e "${RED}❌ Erro ao restaurar backup${NC}"
        exit 1
    fi
}

download_backup() {
    if [ -z "$1" ]; then
        echo -e "${RED}❌ Erro: Especifique o arquivo de backup${NC}"
        echo "Uso: $0 download <backup_file>"
        echo ""
        list_backups
        exit 1
    fi
    
    echo -e "${BLUE}📥 Fazendo download do backup: $1${NC}"
    docker cp slms-backup:/backups/"$1" ./"$1"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Backup salvo em: ./$1${NC}"
    else
        echo -e "${RED}❌ Erro ao fazer download do backup${NC}"
        exit 1
    fi
}

clean_old_backups() {
    echo -e "${YELLOW}🧹 Removendo backups com mais de 30 dias...${NC}"
    docker compose -f "$COMPOSE_FILE" exec backup find /backups -name "backup_*.sql.gz" -mtime +30 -delete
    echo -e "${GREEN}✅ Limpeza concluída${NC}"
}

show_logs() {
    echo -e "${BLUE}📜 Logs do serviço de backup:${NC}"
    docker compose -f "$COMPOSE_FILE" logs --tail=50 -f backup
}

check_space() {
    echo -e "${BLUE}💾 Espaço usado por backups:${NC}"
    docker compose -f "$COMPOSE_FILE" exec backup du -sh /backups
    echo ""
    echo "Detalhes:"
    docker compose -f "$COMPOSE_FILE" exec backup du -h /backups/*.sql.gz 2>/dev/null || echo "Nenhum backup encontrado"
}

# Main
case "${1:-help}" in
    backup)
        create_backup
        ;;
    list)
        list_backups
        ;;
    restore)
        restore_backup "$2"
        ;;
    download)
        download_backup "$2"
        ;;
    clean)
        clean_old_backups
        ;;
    logs)
        show_logs
        ;;
    space)
        check_space
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo -e "${RED}❌ Comando desconhecido: $1${NC}"
        echo ""
        show_help
        exit 1
        ;;
esac
