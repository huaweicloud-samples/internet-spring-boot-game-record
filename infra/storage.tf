# ============================================================================
# 存储层：对象存储桶（存放发布产物）
# ============================================================================

resource "huaweicloud_obs_bucket" "artifacts" {
  bucket        = var.obs_bucket_name
  acl           = "private"
  force_destroy = true
  versioning    = true

  tags = local.common_tags
}

# 发布产物：以当前构建出的可执行包为准，内容变化时自动重新上传。
# 该对象是发布记录的权威来源，实例下发的包与之一致（见 deploy 脚本的执行顺序）。
resource "huaweicloud_obs_bucket_object" "app_package" {
  bucket       = huaweicloud_obs_bucket.artifacts.bucket
  key          = var.app_package_key
  source       = var.app_build_jar
  etag         = filemd5(var.app_build_jar)
  content_type = "application/java-archive"
}

# 说明：桶保持私有。部署工具以操作者自身凭据写入，实例不访问该桶。
