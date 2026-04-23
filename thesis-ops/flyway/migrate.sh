#!/usr/bin/env bash
# Flyway 迁移脚本 — 支持本地多实例 MySQL 路由
# 使用：./migrate.sh [local|dev|staging|prod]
#
# 本地环境 MySQL 实例分布：
#   mysql-core  :3306 — thesis_auth, thesis_user
#   mysql-paper :3307 — thesis_paper, thesis_template
#   mysql-im    :3308 — thesis_im
#   mysql-biz   :3309 — thesis_notify, thesis_statistics
#
# 对于 dev/staging/prod 环境，默认使用单实例（所有库在同一 host:port）。

set -e

ENV=${1:-local}

# 数据库列表（固定顺序）
DATABASES=(
  thesis_auth
  thesis_user
  thesis_paper
  thesis_template
  thesis_im
  thesis_notify
  thesis_statistics
)

# 根据数据库名返回本地 MySQL 实例端口
get_db_port() {
  case "$1" in
    thesis_auth|thesis_user)      echo 3306 ;;
    thesis_paper|thesis_template) echo 3307 ;;
    thesis_im)                    echo 3308 ;;
    thesis_notify|thesis_statistics) echo 3309 ;;
    *) echo 3306 ;;
  esac
}

# 解析环境配置
case "$ENV" in
  local)
    DB_HOST="127.0.0.1"
    DB_PASS="${MYSQL_ROOT_PASSWORD:-root123}"
    USE_MULTI_INSTANCE=true
    ;;
  dev)
    DB_HOST="${DEV_DB_HOST:-localhost}"
    DB_PORT="${DEV_DB_PORT:-3306}"
    DB_PASS="${MYSQL_PASSWORD:-root123}"
    USE_MULTI_INSTANCE=false
    ;;
  staging)
    DB_HOST="${STAGING_DB_HOST:-localhost}"
    DB_PORT="${STAGING_DB_PORT:-3306}"
    DB_PASS="${MYSQL_PASSWORD:-root123}"
    USE_MULTI_INSTANCE=false
    ;;
  prod)
    DB_HOST="${PROD_DB_HOST:-localhost}"
    DB_PORT="${PROD_DB_PORT:-3306}"
    DB_PASS="${MYSQL_PASSWORD:-root123}"
    USE_MULTI_INSTANCE=false
    ;;
  *)
    echo "Unknown env: $ENV"
    echo "Usage: $0 [local|dev|staging|prod]"
    exit 1
    ;;
esac

# Flyway 可执行文件路径
FLYWAY_CMD="${FLYWAY_HOME:-/opt/flyway-10.12.0}/flyway"
if [[ ! -f "$FLYWAY_CMD" ]]; then
  FLYWAY_CMD="flyway"
fi

# 运行 Flyway migrate（兼容无执行权限时使用 bash/sh 调用）
run_flyway() {
  local db=$1
  local host=$2
  local port=$3
  local pass=$4

  local url="jdbc:mysql://${host}:${port}/${db}?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
  local loc="filesystem:$(cd "$(dirname "$0")" && pwd)/${db}"

  echo ">>> Migrating $db on $ENV ($host:$port) ..."

  # 检测是否需要 baseline：库中有表但无 flyway_schema_history
  local need_baseline=false
  local has_tables
  local has_history
  has_tables=$(mysql -h"$host" -P"$port" -uroot -p"$pass" "$db" \
    -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$db' AND table_name != 'flyway_schema_history'" 2>/dev/null \
    | tail -n1 | tr -d '[:space:]')
  has_history=$(mysql -h"$host" -P"$port" -uroot -p"$pass" "$db" \
    -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$db' AND table_name='flyway_schema_history'" 2>/dev/null \
    | tail -n1 | tr -d '[:space:]')

  if [[ "$has_tables" -gt 0 && "$has_history" -eq 0 ]]; then
    need_baseline=true
    echo "    [INFO] 检测到已有表结构但无 Flyway 历史记录，将自动 baseline V1.0.0"
  fi

  if [[ "$need_baseline" == true ]]; then
    bash "$FLYWAY_CMD" \
      -url="$url" \
      -user="root" \
      -password="$pass" \
      -locations="$loc" \
      -baselineOnMigrate=true \
      -baselineVersion=1.0.0 \
      migrate
  else
    bash "$FLYWAY_CMD" \
      -url="$url" \
      -user="root" \
      -password="$pass" \
      -locations="$loc" \
      migrate
  fi

  echo ">>> $db migration done"
  echo ""
}

# 执行迁移
for DB in "${DATABASES[@]}"; do
  if [[ "$USE_MULTI_INSTANCE" == true ]]; then
    PORT=$(get_db_port "$DB")
    run_flyway "$DB" "$DB_HOST" "$PORT" "$DB_PASS"
  else
    run_flyway "$DB" "$DB_HOST" "$DB_PORT" "$DB_PASS"
  fi
done

echo "✓ All databases migrated successfully on [$ENV]"
