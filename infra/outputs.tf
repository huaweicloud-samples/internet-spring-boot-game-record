# ============================================================================
# 输出
# ============================================================================

output "region" {
  description = "部署所在区域"
  value       = var.region
}

output "website_url" {
  description = "站点访问地址（负载均衡弹性公网 IP）"
  value       = "http://${huaweicloud_vpc_eip.elb.address}"
}

output "api_base_url" {
  description = "API 基础路径"
  value       = "http://${huaweicloud_vpc_eip.elb.address}/api"
}

output "elb_id" {
  description = "负载均衡实例 ID"
  value       = huaweicloud_elb_loadbalancer.main.id
}

output "app_instance_public_ips" {
  description = "应用实例的公网 IP（运维用）"
  value       = huaweicloud_vpc_eip.app[*].address
}

output "app_instance_private_ips" {
  description = "应用实例的内网 IP"
  value       = [for i in huaweicloud_compute_instance.app : i.network[0].fixed_ip_v4]
}

output "store_endpoint" {
  description = "兼容接口实例连接地址与端口"
  value       = "${huaweicloud_geminidb_instance.store.lb_ip_address}:${local.store_port}"
  sensitive   = true
}

output "artifacts_bucket" {
  description = "发布产物所在的对象存储桶"
  value       = huaweicloud_obs_bucket.artifacts.bucket
}

output "notification_topic_urn" {
  description = "事件通知主题 URN"
  value       = huaweicloud_smn_topic.game_events.topic_urn
}

output "store_available_spec_codes" {
  description = "当前区域可用于兼容接口实例的规格码清单（便于按需调整）"
  value       = [for f in data.huaweicloud_geminidb_flavors.compatible.flavors : f.spec_code]
}
