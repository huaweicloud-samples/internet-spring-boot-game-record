# ============================================================================
# 通知层：消息通知主题与邮件订阅
# ============================================================================

resource "huaweicloud_smn_topic" "game_events" {
  name         = "${var.project_name}-game-events"
  display_name = "对局与用户事件通知"

  tags = local.common_tags
}

# 邮件订阅需收件人点击确认链接后生效；notification_email 留空则不创建订阅。
resource "huaweicloud_smn_subscription" "email" {
  count = var.notification_email == "" ? 0 : 1

  topic_urn = huaweicloud_smn_topic.game_events.topic_urn
  endpoint  = var.notification_email
  protocol  = "email"
  remark    = "对局结束与用户创建事件通知"
}
