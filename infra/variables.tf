# ============================================================================
# 变量定义
# ============================================================================

variable "region" {
  description = "区域 ID"
  type        = string
  default     = "cn-north-4"
}

variable "project_name" {
  description = "项目名称，用作资源命名前缀"
  type        = string
  default     = "scorekeep"
}

variable "availability_zone" {
  description = "承载实例与负载均衡所在可用区"
  type        = string
  default     = "cn-north-4a"
}

variable "vpc_cidr" {
  description = "VPC 网段"
  type        = string
  default     = "10.10.0.0/16"
}

variable "subnet_cidr" {
  description = "子网网段"
  type        = string
  default     = "10.10.1.0/24"
}

# ---------------------------------------------------------------------------
# 对象存储
# ---------------------------------------------------------------------------

variable "obs_bucket_name" {
  description = "存放发布产物的对象存储桶名（全局唯一，请按需修改）"
  type        = string
  default     = "scorekeep-practice-artifacts"
}

# ---------------------------------------------------------------------------
# 数据库（兼容接口实例）
# ---------------------------------------------------------------------------

variable "geminidb_password" {
  description = "兼容接口实例管理员密码（8-32 位，须含大写、小写、数字、特殊字符）"
  type        = string
  sensitive   = true
  default     = "<GEMINIDB_PASSWORD>"
}

variable "geminidb_spec_code" {
  description = "兼容接口实例规格码，默认取 2 vCPU / 8 GB"
  type        = string
  default     = "geminidb.cassandra.large.4"
}

variable "geminidb_availability_zones" {
  description = "兼容接口实例的可用区列表（逗号分隔，数量应与节点数匹配）"
  type        = string
  default     = "cn-north-4a,cn-north-4b,cn-north-4c"
}

variable "geminidb_node_count" {
  description = "兼容接口实例计算节点数"
  type        = number
  default     = 3
}

variable "geminidb_storage_size" {
  description = "兼容接口实例存储容量（GB）"
  type        = number
  default     = 100
}

# ---------------------------------------------------------------------------
# 通知
# ---------------------------------------------------------------------------

variable "notification_email" {
  description = "接收业务通知的邮箱；留空则不创建邮件订阅"
  type        = string
  default     = ""
}

# ---------------------------------------------------------------------------
# 委托
# ---------------------------------------------------------------------------

variable "delegated_service_name" {
  description = "被委托的云服务身份，承载应用的弹性云服务器对应固定取值 op_svc_ecs"
  type        = string
  default     = "op_svc_ecs"
}

# ---------------------------------------------------------------------------
# 计算
# ---------------------------------------------------------------------------

variable "ecs_flavor" {
  description = "承载应用的计算实例规格"
  type        = string
  default     = "s6.large.2"
}

variable "ecs_image_regex" {
  description = "计算实例镜像名称正则（首尾锚定，避免命中带图形驱动的变体）"
  type        = string
  default     = "^Ubuntu 22.04 server 64bit$"
}

variable "instance_count" {
  description = "应用实例数量（全部注册为负载均衡后端）"
  type        = number
  default     = 1

  validation {
    condition     = var.instance_count >= 1 && var.instance_count <= 4
    error_message = "instance_count 取值范围为 1-4。"
  }
}

variable "ssh_public_key" {
  description = "用于登录应用实例的 SSH 公钥（ssh-ed25519 / ssh-rsa 开头的公钥字符串）"
  type        = string
}

variable "ssh_remote_private_key_path" {
  description = "本地私钥文件路径，供部署期间完成应用下发与进程守护配置使用"
  type        = string
}

variable "ssh_source_cidr" {
  description = "允许 SSH 访问实例的源网段，建议收敛为运维出口 IP/32"
  type        = string
  default     = "0.0.0.0/0"
}

# ---------------------------------------------------------------------------
# 负载均衡
# ---------------------------------------------------------------------------

variable "l7_flavor_name" {
  description = "七层负载均衡规格名称（按区域可用规格填写）"
  type        = string
  default     = "L7_flavor.elb.s1.small"
}

variable "eip_type" {
  description = "弹性公网 IP 类型"
  type        = string
  default     = "5_bgp"
}

variable "bandwidth_size" {
  description = "弹性公网 IP 带宽上限（Mbit/s）"
  type        = number
  default     = 5
}

# ---------------------------------------------------------------------------
# 发布
# ---------------------------------------------------------------------------

variable "app_revision" {
  description = "应用版本标记；变更该值会触发重新下发应用与进程守护配置"
  type        = string
  default     = "1"
}

variable "app_build_jar" {
  description = "本地已构建的可执行包路径（相对 infra 目录）"
  type        = string
  default     = "../src/api/build/libs/scorekeep-api.jar"
}

variable "app_package_key" {
  description = "发布产物在对象存储桶中的对象名"
  type        = string
  default     = "releases/scorekeep-api.jar"
}
