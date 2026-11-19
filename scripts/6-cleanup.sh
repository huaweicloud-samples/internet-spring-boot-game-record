#!/usr/bin/env bash
#
# 清理本次部署创建的全部资源。
#
# 该操作会删除计算实例、负载均衡、兼容接口实例、对象存储桶与通知主题，
# 其中对象存储桶会连同其中的发布产物一并删除，且不可恢复。

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TF="${TERRAFORM_BIN:-terraform}"

cd "$ROOT/infra"

echo "即将销毁以下资源："
"$TF" state list 2>/dev/null || true
echo ""
read -r -p "确认销毁？输入 yes 继续：" ANSWER
if [ "$ANSWER" != "yes" ]; then
  echo "已取消。"
  exit 1
fi

"$TF" destroy -auto-approve

rm -f tfplan
echo "清理完成。"
