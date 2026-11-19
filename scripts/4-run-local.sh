#!/usr/bin/env bash
#
# 在本机启动接口服务，连到已部署的存储与通知资源，便于调试。
#
# 前置条件：已执行 scripts/2-deploy.sh
# 需要自行导出：GEMINIDB_PASSWORD（兼容接口实例的管理员密码）
#
# 启动后访问 http://localhost:8080 即可看到页面与接口。

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TF="${TERRAFORM_BIN:-terraform}"

cd "$ROOT/infra"

ENDPOINT="$("$TF" output -raw store_endpoint)"
export GEMINIDB_HOST="${ENDPOINT%%:*}"
export GEMINIDB_PORT="${ENDPOINT##*:}"
export GEMINIDB_USER="${GEMINIDB_USER:-rwuser}"
export HW_REGION="${HW_REGION:-cn-north-4}"
export SMN_TOPIC_URN="$("$TF" output -raw notification_topic_urn)"
export SERVER_PORT="${SERVER_PORT:-8080}"

: "${GEMINIDB_PASSWORD:?请先导出 GEMINIDB_PASSWORD}"

echo "存储接入点：$GEMINIDB_HOST:$GEMINIDB_PORT"
echo "签名区域：$HW_REGION"
echo "通知主题：$SMN_TOPIC_URN"
echo "本地端口：$SERVER_PORT"
echo ""

cd "$ROOT/src/api"
./gradlew bootRun
