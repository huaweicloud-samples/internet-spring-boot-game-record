#!/usr/bin/env bash
#
# 打印站点访问地址与接口基础路径。

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TF="${TERRAFORM_BIN:-terraform}"

cd "$ROOT/infra"

echo "站点地址：$("$TF" output -raw website_url)"
echo "接口路径：$("$TF" output -raw api_base_url)"
