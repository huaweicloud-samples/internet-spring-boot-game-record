# ============================================================================
# 接入层：七层负载均衡（对外入口）
# ============================================================================

resource "huaweicloud_vpc_eip" "elb" {
  publicip {
    type = var.eip_type
  }

  bandwidth {
    name        = "${var.project_name}-elb-bw"
    size        = var.bandwidth_size
    share_type  = "PER"
    charge_mode = "traffic"
  }

  tags = local.common_tags
}

resource "huaweicloud_elb_loadbalancer" "main" {
  name   = "${var.project_name}-elb"
  vpc_id = huaweicloud_vpc.main.id
  # 该字段要 Neutron 子网 ID（.ipv4_subnet_id），不是 VPC 层资源 ID（.id）
  ipv4_subnet_id    = huaweicloud_vpc_subnet.main.ipv4_subnet_id
  availability_zone = [var.availability_zone]

  # 规格取自由规格名精确锁定的唯一元素
  l7_flavor_id = one(local.l7_flavor_ids)

  # 对外入口：绑定弹性公网 IP
  ipv4_eip_id = huaweicloud_vpc_eip.elb.id

  tags = local.common_tags
}

resource "huaweicloud_elb_pool" "app" {
  name            = "${var.project_name}-pool"
  protocol        = "HTTP"
  lb_method       = "ROUND_ROBIN"
  loadbalancer_id = huaweicloud_elb_loadbalancer.main.id
}

resource "huaweicloud_elb_listener" "http" {
  name            = "${var.project_name}-http"
  protocol        = "HTTP"
  protocol_port   = 80
  loadbalancer_id = huaweicloud_elb_loadbalancer.main.id
  default_pool_id = huaweicloud_elb_pool.app.id
}

resource "huaweicloud_elb_member" "app" {
  count = var.instance_count

  address       = huaweicloud_compute_instance.app[count.index].network[0].fixed_ip_v4
  protocol_port = local.app_port
  pool_id       = huaweicloud_elb_pool.app.id
  subnet_id     = huaweicloud_vpc_subnet.main.ipv4_subnet_id
}

resource "huaweicloud_elb_monitor" "app" {
  pool_id     = huaweicloud_elb_pool.app.id
  protocol    = "HTTP"
  url_path    = "/api/rules"
  interval    = 10
  timeout     = 5
  max_retries = 3
}

# 说明：监听器 default_pool_id 与后端池存在绑定关系，销毁时需先解除绑定再删监听器，
#       详见部署指导书「清理资源」章节。
