#!/usr/bin/env bash
# Flyway 迁移脚本 — 按环境执行所有数据库迁移
# 使用：./migrate.sh [local|dev|staging|prod]
set -e

ENV=${1:-local}

case "$ENV" in
  local)   DB_HOST="localhost"; DB_PORT="3306"; DB_PASS="${MYSQL_ROOT_PASSWORD:-root123}" ;;
  dev)     DB_HOST="${DEV_DB_HOST}";     DB_PORT="3306"; DB_PASS="${MYSQL_PASSWORD}" ;;
  staging) DB_HOST="${STAGING_DB_HOST}"; DB_PORT="3306"; DB_PASS="${MYSQL_PASSWORD}" ;;
  prod)    DB_HOST="${PROD_DB_HOST}";    DB_PORT="3306"; DB_PASS="${MYSQL_PASSWORD}" ;;
  *) echo "Unknown env: $ENV"; exit 1 ;;
esac

DATABASES=(thesis_auth thesis_user thesis_paper thesis_template thesis_im thesis_statistics thesis_notify)

for DB in "${DATABASES[@]}"; do
  echo ">>> Migrating $DB on $ENV ($DB_HOST)..."
  flyway \
    -url="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB}?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
    -user="root" \
    -password="${DB_PASS}" \
    -locations="filesystem:$(dirname "$0")/${DB}" \
    migrate
  echo ">>> $DB migration done"
done

echo ""
echo "✓ All databases migrated successfully on [$ENV]"
