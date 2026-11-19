#!/usr/bin/env bash
#
# 接口回归：创建参与者与场次、开出一局、按顺序落子至终局、校验结果，随后清理数据。
#
# 用法：
#   scripts/5-test-api.sh http://<站点地址>/api
#   scripts/5-test-api.sh http://localhost:8080/api
#
# 依赖：curl。退出码 0 表示全部检查通过。

set -uo pipefail

API="${1:-http://localhost:8080/api}"
API="${API%/}"

PASS=0
FAIL=0

json_field() {
  # 从简单 JSON 响应中取第一个同名字段值
  sed -n "s/.*\"$1\":\"\([^\"]*\)\".*/\1/p" | head -1
}

check() {
  local label="$1" actual="$2" expected="$3"
  if [ "$actual" = "$expected" ]; then
    echo "  [通过] $label = $actual"
    PASS=$((PASS + 1))
  else
    echo "  [失败] $label 期望 $expected，实际 $actual"
    FAIL=$((FAIL + 1))
  fi
}

echo "目标接口：$API"
echo ""

echo "1) 准备参与者与场次"
PLAYER1="$(curl -sS -X POST "$API/user" | json_field id)"
PLAYER2="$(curl -sS -X POST "$API/user" | json_field id)"
SESSION="$(curl -sS -X POST "$API/session" | json_field id)"
check "参与者一" "$([ -n "$PLAYER1" ] && echo ok)" "ok"
check "参与者二" "$([ -n "$PLAYER2" ] && echo ok)" "ok"
check "场次" "$([ -n "$SESSION" ] && echo ok)" "ok"

echo ""
echo "2) 开通一局并配置"
GAME="$(curl -sS -X POST "$API/game/$SESSION" | json_field id)"
check "对局" "$([ -n "$GAME" ] && echo ok)" "ok"
curl -sS -X POST "$API/game/$SESSION/$GAME/users" \
  -H "Content-Type: application/json" \
  --data "[\"$PLAYER1\",\"$PLAYER2\"]" >/dev/null
curl -sS -X PUT "$API/game/$SESSION/$GAME/rules/TicTacToe" >/dev/null
curl -sS -X PUT "$API/game/$SESSION/$GAME/name/regression" >/dev/null

RULES="$(curl -sS "$API/rules" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1)"
check "规则目录首项" "$RULES" "TicTacToe"

STATE_IDS="$(curl -sS "$API/game/$SESSION/$GAME" | sed -n 's/.*"states":\[\([^]]*\)\].*/\1/p')"
check "开局后已有局面" "$([ -n "$STATE_IDS" ] && echo ok)" "ok"

echo ""
echo "3) 按顺序落子至终局"
for MOVE in X1 O4 X2 O5 X3; do
  PLAYER="$PLAYER1"
  case "${MOVE:0:1}" in
    O) PLAYER="$PLAYER2" ;;
  esac
  curl -sS -X POST "$API/move/$SESSION/$GAME/$PLAYER" \
    -H "Content-Type: text/plain" --data "$MOVE" >/dev/null
done

LAST_STATE="$(curl -sS "$API/state/$SESSION/$GAME/$(curl -sS "$API/game/$SESSION/$GAME" | sed -n 's/.*"states":\[\([^]]*\)\].*/\1/p' | sed 's/.*"\([^"]*\)".*/\1/')" \
  | sed -n 's/.*"state":"\([^"]*\)".*/\1/p')"
check "终局结果" "${LAST_STATE:0:1}" "A"
check "终局棋盘" "${LAST_STATE:1}" "XXXOO...."

MOVES="$(curl -sS "$API/move/$SESSION/$GAME" | grep -o '"move"' | wc -l | tr -d ' ')"
check "已记录落子数" "$MOVES" "5"

echo ""
echo "4) 清理本次数据"
curl -sS -X DELETE "$API/game/$SESSION/$GAME" >/dev/null
curl -sS -X DELETE "$API/session/$SESSION" >/dev/null
curl -sS -X DELETE "$API/user/$PLAYER1" >/dev/null
curl -sS -X DELETE "$API/user/$PLAYER2" >/dev/null
echo "  已清理对局、场次与参与者"

echo ""
echo "结果：通过 $PASS 项，失败 $FAIL 项"
[ "$FAIL" -eq 0 ]
