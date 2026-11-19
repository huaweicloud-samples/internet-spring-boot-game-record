# ============================================================================
# 数据层：兼容接口实例（宽表模型，业务存储）
# ============================================================================
# 说明：
#   - 数据面鉴权使用实例自带的数据库账号（固定用户名 rwuser + 实例管理员密码），
#     不接受 IAM 委托临时凭据，也不支持安全令牌。
#   - 数据面端口随部署形态而定（本配置为集群形态，走高可用端口 80），
#     连接地址使用实例的负载均衡地址。
#   - 业务用表与全局二级索引由应用启动时经 DDL 幂等创建，无需在此声明表结构。
# ============================================================================

resource "huaweicloud_geminidb_instance" "store" {
  name              = "${var.project_name}-store"
  mode              = "Cluster"
  password          = var.geminidb_password
  availability_zone = var.geminidb_availability_zones
  vpc_id            = huaweicloud_vpc.main.id
  subnet_id         = huaweicloud_vpc_subnet.main.id
  security_group_id = huaweicloud_networking_secgroup.app.id

  datastore {
    # 引擎名取小写；兼容 DynamoDB 的实例对外不暴露版本号，version 必须为空字符串。
    type           = "dynamodb"
    storage_engine = "rocksDB"
    version        = ""
  }

  flavor {
    num       = var.geminidb_node_count
    size      = var.geminidb_storage_size
    spec_code = var.geminidb_spec_code
    storage   = "ULTRAHIGH"
  }

  tags = local.common_tags
}
