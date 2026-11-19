# ============================================================================
# 网络层：VPC / 子网 / 安全组 / 互联网网关
# ============================================================================

resource "huaweicloud_vpc" "main" {
  name = "${var.project_name}-vpc"
  cidr = var.vpc_cidr

  tags = local.common_tags
}

resource "huaweicloud_vpc_subnet" "main" {
  name       = "${var.project_name}-subnet"
  vpc_id     = huaweicloud_vpc.main.id
  cidr       = var.subnet_cidr
  gateway_ip = cidrhost(var.subnet_cidr, 1)

  tags = local.common_tags
}

# 互联网网关：弹性公网 IP 做地址转换的基础设施。
# add_route = false —— 默认路由由弹性公网 IP 的隐式路由承担，不在此重复声明 0.0.0.0/0。
resource "huaweicloud_vpc_internet_gateway" "main" {
  vpc_id    = huaweicloud_vpc.main.id
  name      = "${var.project_name}-igw"
  add_route = false

  depends_on = [huaweicloud_vpc_subnet.main]
}

resource "huaweicloud_networking_secgroup" "app" {
  name        = "${var.project_name}-app-sg"
  description = "应用实例安全组"
}

# 应用端口仅对 VPC 内开放，负载均衡经内网转发访问
resource "huaweicloud_networking_secgroup_rule" "app_port_from_vpc" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = local.app_port
  port_range_max    = local.app_port
  remote_ip_prefix  = var.vpc_cidr
  security_group_id = huaweicloud_networking_secgroup.app.id
}

# SSH 运维入口，建议将 ssh_source_cidr 收敛为运维出口 IP
resource "huaweicloud_networking_secgroup_rule" "ssh" {
  count = var.ssh_source_cidr == "" ? 0 : 1

  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 22
  port_range_max    = 22
  remote_ip_prefix  = var.ssh_source_cidr
  security_group_id = huaweicloud_networking_secgroup.app.id
}

# 说明：出方向沿用平台自动注入的放行规则，不重复声明。
# 实例之间的互访由平台自动注入的「同安全组内互访」入方向规则覆盖，同样不重复声明。

# 说明：出方向沿用平台自动注入的放行规则，不重复声明。
