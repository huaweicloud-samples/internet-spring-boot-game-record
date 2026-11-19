# ============================================================================
# 主入口：Provider、版本约束、公共数据源与本地取值
# ============================================================================

terraform {
  required_version = ">= 1.5"

  required_providers {
    huaweicloud = {
      source  = "huaweicloud/huaweicloud"
      version = "~> 1.97"
    }
  }

  # 状态文件建议存放于对象存储桶
  backend "local" {}
}

provider "huaweicloud" {
  region = var.region
  # 部署前在华为云「我的凭证」页面获取 Access Key / Secret Key，并以环境变量方式提供给 Terraform：
  #   export HW_ACCESS_KEY="<your-access-key>"
  #   export HW_SECRET_KEY="<your-secret-key>"
  # 也可使用临时凭证：
  #   export HW_SECURITY_TOKEN="<your-security-token>"
}

# ---------------------------------------------------------------------------
# 公共数据源
# ---------------------------------------------------------------------------

# 计算实例镜像（正则首尾锚定，避免命中同名的图形驱动变体）
data "huaweicloud_images_image" "app" {
  name_regex  = var.ecs_image_regex
  most_recent = true
}

# 七层负载均衡规格（按规格名精确取值，避免按下标取到更高规格）
data "huaweicloud_elb_flavors" "l7" {
  type = "L7"
}

# 兼容接口实例可用规格族；不传 engine_name —— 传任何值都会被服务端拒绝
data "huaweicloud_geminidb_flavors" "compatible" {}

locals {
  # 按规格名精确锁定唯一元素
  l7_flavor_ids = [
    for f in data.huaweicloud_elb_flavors.l7.flavors : f.id if f.name == var.l7_flavor_name
  ]

  common_tags = {
    project = var.project_name
    managed = "terraform"
  }

  app_port = 8080

  # 兼容接口实例的数据面端口随部署形态而变：
  # 集群形态（多节点/跨可用区，即本配置）走高可用端口 80；单节点形态才是 8000。
  store_port = 80
}
