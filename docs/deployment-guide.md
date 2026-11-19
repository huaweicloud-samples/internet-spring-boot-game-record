# 部署指导

本文给出从零到可访问的完整部署过程，以及常见问题的排查路径。

## 一、部署前准备

### 1.1 账号与权限

在控制台的「我的凭证」页面取得以下信息并留存：

- Access Key 与 Secret Key
- 账号域名

部署所使用的账号需要具备创建 VPC、ECS、ELB、EIP、GeminiDB、OBS、SMN 与 IAM 委托的权限。

### 1.2 开通独享型负载均衡

本实践使用独享型七层负载均衡。账号未开通时，创建负载均衡会返回 `ELB.8902`。请先提交工单开通后再部署。

### 1.3 准备密钥对

```bash
ssh-keygen -t ed25519 -f ~/.ssh/scorekeep_ed25519 -C "scorekeep"
```

公钥内容填入 `ssh_public_key`，私钥路径填入 `ssh_remote_private_key_path`。私钥仅用于部署期把应用下发到实例，请妥善保管。

### 1.4 安装工具

| 工具 | 说明 |
|---|---|
| Terraform | 1.5 及以上 |
| JDK | 17 |
| Bash | Windows 建议使用 Git Bash |

Gradle 无需单独安装，仓库内已带包装器。

## 二、填写配置

```bash
cp infra/terraform.tfvars.example infra/terraform.tfvars
```

| 变量 | 必填 | 说明 |
|---|---|---|
| `region` | 否 | 区域，缺省 `cn-north-4` |
| `project_name` | 否 | 资源命名前缀，缺省 `scorekeep` |
| `availability_zone` | 否 | 实例与负载均衡所在可用区，缺省 `cn-north-4a` |
| `obs_bucket_name` | 是 | 发布产物桶名，全局唯一 |
| `geminidb_password` | 是 | 兼容接口实例管理员密码，8 至 32 位，须含大写字母、小写字母、数字与特殊字符 |
| `delegated_service_name` | 否 | 被委托的云服务身份，默认 `op_svc_ecs` |
| `notification_email` | 否 | 通知邮箱，留空则不创建订阅 |
| `ssh_public_key` | 是 | SSH 公钥内容 |
| `ssh_remote_private_key_path` | 是 | 本地私钥路径 |
| `ssh_source_cidr` | 否 | 允许 SSH 的来源网段，建议收敛为部署机出口地址 |
| `ecs_flavor` | 否 | 应用实例规格，缺省 `s6.large.2` |
| `instance_count` | 否 | 应用实例数量，取值 1 至 4，缺省 1 |
| `l7_flavor_name` | 否 | 七层负载均衡规格名 |
| `geminidb_spec_code` | 否 | 兼容接口实例规格码 |
| `app_revision` | 否 | 应用版本标记，变更即触发重新下发 |

密码请勿使用包含 `#` 的取值：该字符在连接串中会被截断，导致应用无法连接存储。

## 三、部署

```bash
export HW_ACCESS_KEY="<your-access-key>"
export HW_SECRET_KEY="<your-secret-key>"

bash scripts/1-build.sh
bash scripts/2-deploy.sh
```

`2-deploy.sh` 依次执行格式检查、配置校验、生成执行计划并应用。首次创建 GeminiDB 实例耗时较长，属正常现象。

## 四、部署后核对

```bash
bash scripts/3-open-website.sh
curl http://<负载均衡公网地址>/api/rules
bash scripts/5-test-api.sh http://<负载均衡公网地址>/api
```

三条命令分别确认访问地址可用、规则接口返回正常、完整对局流程（创建参与者与场次、开局、落子至终局、清理）全部通过。

若接口探活成功但网页空白，说明应用已就绪而前端资源缺失，检查构建时 `src/frontend/` 是否存在，随后重新构建并调大 `app_revision` 触发重新下发。

## 五、运维

### 5.1 查看应用日志

```bash
sudo journalctl -u scorekeep -n 200 --no-pager
```

### 5.2 重启应用

```bash
sudo systemctl restart scorekeep
```

### 5.3 发布新版本

```bash
bash scripts/1-build.sh
# 修改 infra/terraform.tfvars 中的 app_revision，例如从 1 改为 2
bash scripts/2-deploy.sh
```

### 5.4 横向扩容

把 `instance_count` 从 1 调整为 2 至 4。新增实例会自动注册到后端池，健康检查通过后开始承接流量；无需改动应用配置。

## 六、清理

```bash
bash scripts/6-cleanup.sh
```

脚本会先列出将销毁的资源并要求输入 `yes` 确认。

清理时的顺序约束：负载均衡的监听器与后端池存在绑定关系，销毁时会先解除绑定再删除监听器，因此该步骤耗时略长，请等待命令自行结束，不要中途中断。

## 七、排查路径

| 现象 | 排查起点 |
|---|---|
| 创建负载均衡报 `ELB.8902` | 账号未开通独享型负载均衡，先提交工单 |
| 应用启动失败、服务反复重启 | 查看 `journalctl -u scorekeep`；多数是存储连接串中的端口与实例部署形态不匹配（集群形态 80、单节点形态 8000） |
| 健康检查始终不通过 | 在实例上执行 `curl -fsS http://127.0.0.1:8080/api/rules`，区分应用问题与网络问题 |
| 按场次查询对局为空 | 二级索引缺失，重启应用触发补齐 |
| 收不到通知邮件 | 邮件订阅未确认，或主题 URN 未写入实例配置 |
| 网页可打开但接口报错 | 浏览器开发者工具的网络面板查看具体请求与响应 |
| 自定义域名访问异常 | 本实践不含域名与证书，如需要请自行接入域名解析并配置证书监听器 |
