#!/usr/bin/env bash
#
# 部署基础设施与应用。
#
# 前置条件：
#   1. 已执行 scripts/1-build.sh
#   2. 已复制 infra/terraform.tfvars.example 为 infra/terraform.tfvars 并填写
#   3. 已导出访问密钥：HW_ACCESS_KEY / HW_SECRET_KEY
#
# 说明：provider 已由命令行配置的本地镜像提供时不必执行 init；
#       需要从注册中心拉取时，按 TERRAFORM_INIT=1 重跑本脚本。

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
INFRA="$ROOT/infra"
JAR="$ROOT/src/api/build/libs/scorekeep-api.jar"

if [ ! -f "$JAR" ]; then
  echo "未找到构建产物：$JAR" >&2
  echo "请先执行 scripts/1-build.sh" >&2
  exit 1
fi

if [ ! -f "$INFRA/terraform.tfvars" ]; then
  echo "未找到 infra/terraform.tfvars" >&2
  echo "请先复制 infra/terraform.tfvars.example 并填写实际取值" >&2
  exit 1
fi

: "${HW_ACCESS_KEY:?请先导出 HW_ACCESS_KEY}"
: "${HW_SECRET_KEY:?请先导出 HW_SECRET_KEY}"

TF="${TERRAFORM_BIN:-terraform}"

cd "$INFRA"

if [ "${TERRAFORM_INIT:-0}" = "1" ]; then
  "$TF" init -input=false
else
  echo "[提示] 跳过 terraform init（provider 由本地镜像提供）。"
  echo "       如需从注册中心拉取 provider，请以 TERRAFORM_INIT=1 重跑本脚本。"
fi

"$TF" fmt -check -recursive
"$TF" validate

"$TF" plan -out=tfplan
"$TF" apply -auto-approve tfplan

echo ""
echo "部署完成。访问地址："
"$TF" output -raw website_url
echo ""
