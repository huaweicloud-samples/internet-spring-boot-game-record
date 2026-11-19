#!/usr/bin/env bash
#
# 构建可执行包。
#
# 产物：src/api/build/libs/scorekeep-api.jar
# 该产物同时是发布到对象存储桶的内容，必须先于部署执行。

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/src/api"

if [ ! -x "./gradlew" ]; then
  chmod +x ./gradlew
fi

./gradlew clean build

echo ""
echo "构建完成：$ROOT/src/api/build/libs/scorekeep-api.jar"
