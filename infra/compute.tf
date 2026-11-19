# ============================================================================
# 计算层：应用实例 + 弹性公网 IP + 部署下发
# ============================================================================

resource "huaweicloud_compute_keypair" "app" {
  name       = "${var.project_name}-keypair"
  public_key = var.ssh_public_key
}

resource "huaweicloud_vpc_eip" "app" {
  count = var.instance_count

  publicip {
    type = var.eip_type
  }

  bandwidth {
    name        = "${var.project_name}-app-bw-${count.index + 1}"
    size        = var.bandwidth_size
    share_type  = "PER"
    charge_mode = "traffic"
  }

  tags = local.common_tags
}

resource "huaweicloud_compute_instance" "app" {
  count = var.instance_count

  name               = "${var.project_name}-app-${count.index + 1}"
  image_id           = data.huaweicloud_images_image.app.id
  flavor_name        = var.ecs_flavor
  availability_zone  = var.availability_zone
  security_group_ids = [huaweicloud_networking_secgroup.app.id]
  key_pair           = huaweicloud_compute_keypair.app.name
  agency_name        = huaweicloud_identity_agency.app_instance.name

  system_disk_type = "SSD"
  system_disk_size = 40

  network {
    uuid = huaweicloud_vpc_subnet.main.id
  }

  # 双保险注入运维公钥：部分公共镜像的初始化进程偶发不读取实例元数据中的公钥。
  user_data = <<-EOT
    #cloud-config
    users:
      - name: ubuntu
        sudo: ALL=(ALL) NOPASSWD:ALL
        shell: /bin/bash
        ssh_authorized_keys:
          - ${var.ssh_public_key}
    ssh_pwauth: false
  EOT

  tags = local.common_tags

  lifecycle {
    # 该字段变更不会触发实例原地更新，显式让出所有权，避免 plan 出现无法生效的差异。
    ignore_changes = [user_data]
  }
}

resource "huaweicloud_compute_eip_associate" "app" {
  count = var.instance_count

  public_ip   = huaweicloud_vpc_eip.app[count.index].address
  instance_id = huaweicloud_compute_instance.app[count.index].id
}

# ---------------------------------------------------------------------------
# 应用下发与进程守护配置（部署期间经 SSH 完成）
# ---------------------------------------------------------------------------

resource "terraform_data" "app_bootstrap" {
  count = var.instance_count

  triggers_replace = [
    var.app_revision,
    huaweicloud_compute_instance.app[count.index].id,
  ]

  connection {
    type        = "ssh"
    host        = huaweicloud_compute_eip_associate.app[count.index].public_ip
    user        = "ubuntu"
    private_key = file(var.ssh_remote_private_key_path)
    timeout     = "15m"
  }

  provisioner "file" {
    source      = var.app_build_jar
    destination = "/tmp/scorekeep-api.jar"
  }

  provisioner "remote-exec" {
    inline = [
      "sudo apt-get update -y",
      "sudo DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-17-jre-headless",
      "sudo useradd --system --no-create-home --shell /usr/sbin/nologin ${var.project_name} 2>/dev/null || true",
      "sudo mkdir -p /opt/scorekeep",
      "sudo install -m 0644 /tmp/scorekeep-api.jar /opt/scorekeep/scorekeep-api.jar",
      "sudo chown -R ${var.project_name}:${var.project_name} /opt/scorekeep",

      "printf '%s\\n' 'GEMINIDB_HOST=${huaweicloud_geminidb_instance.store.lb_ip_address}' 'GEMINIDB_PORT=${local.store_port}' 'GEMINIDB_USER=rwuser' 'GEMINIDB_PASSWORD=${var.geminidb_password}' 'HW_REGION=${var.region}' 'SMN_TOPIC_URN=${huaweicloud_smn_topic.game_events.topic_urn}' 'NOTIFICATION_EMAIL=${var.notification_email}' 'SERVER_PORT=${local.app_port}' | sudo tee /etc/scorekeep.env >/dev/null",
      "sudo chmod 0600 /etc/scorekeep.env",

      "printf '%s\\n' '[Unit]' 'Description=Scorekeep REST API' 'After=network-online.target' 'Wants=network-online.target' '' '[Service]' 'User=${var.project_name}' 'EnvironmentFile=/etc/scorekeep.env' 'ExecStart=/usr/bin/java -Xms256m -Xmx768m -jar /opt/scorekeep/scorekeep-api.jar' 'Restart=always' 'RestartSec=5' 'SuccessExitStatus=143' '' '[Install]' 'WantedBy=multi-user.target' | sudo tee /etc/systemd/system/scorekeep.service >/dev/null",

      "sudo systemctl daemon-reload",
      "sudo systemctl enable scorekeep",
      "sudo systemctl restart scorekeep",
      "for i in $(seq 1 60); do curl -fsS http://127.0.0.1:${local.app_port}/api/rules >/dev/null && break || sleep 5; done",
      "curl -fsS http://127.0.0.1:${local.app_port}/api/rules",
    ]
  }

  depends_on = [
    huaweicloud_compute_eip_associate.app,
    huaweicloud_identity_agency.app_instance,
    huaweicloud_geminidb_instance.store,
  ]
}
