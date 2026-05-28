# GitHub 项目上传完整指南

## 一、前置准备

### 1.1 注册 GitHub 账号

1. 访问 https://github.com
2. 点击右上角 "Sign up"（注册）
3. 填写信息：
   - 邮箱地址
   - 密码（至少 15 个字符或 8 个字符+数字）
   - 用户名（这个就是你的 GitHub 主页地址的一部分）
4. 验证邮箱
5. 完成注册

**你的 GitHub 主页地址**：`https://github.com/你的用户名`

### 1.2 安装 Git

**Windows 系统**：
1. 下载：https://git-scm.com/download/win
2. 双击安装包，一路 Next（使用默认配置即可）
3. 安装完成后，打开 PowerShell 或 CMD，输入：
   ```bash
   git --version
   ```
   如果显示版本号，说明安装成功

### 1.3 配置 Git

打开 PowerShell 或 CMD，执行以下命令：

```bash
# 配置用户名（显示在提交记录中）
git config --global user.name "你的名字"

# 配置邮箱（使用 GitHub 注册的邮箱）
git config --global user.email "你的邮箱@example.com"

# 查看配置
git config --list
```

---

## 二、创建 GitHub 仓库

### 2.1 在 GitHub 网站创建仓库

1. 登录 GitHub
2. 点击右上角 "+" → "New repository"（新建仓库）
3. 填写仓库信息：
   - **Repository name**：`qiyun-restaurant`（仓库名称）
   - **Description**：`七云菜馆 - 微服务餐厅管理系统`（描述）
   - **Public/Private**：选择 Public（公开）
   - **不要勾选** "Add a README file"（我们本地已有）
4. 点击 "Create repository"（创建仓库）

**你的项目地址**：`https://github.com/你的用户名/qiyun-restaurant`

---

## 三、上传项目到 GitHub

### 3.1 方式一：使用命令行（推荐）


**步骤 1：打开项目目录**

在 PowerShell 中进入项目根目录：
```bash
cd "C:\Users\Administrator\Desktop\七云菜馆"
```

**步骤 2：初始化 Git 仓库**

```bash
# 初始化 Git 仓库
git init

# 查看当前状态
git status
```

**步骤 3：添加文件到暂存区**

```bash
# 添加所有文件
git add .

# 或者选择性添加
git add README.md
git add restaurant-server/
git add restaurant-order/
# ... 其他目录
```

**步骤 4：提交到本地仓库**

```bash
git commit -m "初始提交：七云菜馆微服务项目"
```

**步骤 5：关联远程仓库**

```bash
# 添加远程仓库（替换成你的 GitHub 用户名）
git remote add origin https://github.com/你的用户名/qiyun-restaurant.git

# 查看远程仓库
git remote -v
```

**步骤 6：推送到 GitHub**

```bash
# 推送到 main 分支
git branch -M main
git push -u origin main
```

**首次推送会要求登录**：
- 输入 GitHub 用户名
- 输入密码（现在需要使用 Personal Access Token，见下文）

---

### 3.2 方式二：使用 GitHub Desktop（图形界面）

1. 下载 GitHub Desktop：https://desktop.github.com
2. 安装并登录 GitHub 账号
3. 点击 "File" → "Add local repository"
4. 选择项目目录
5. 点击 "Publish repository"（发布仓库）
6. 选择 Public，点击 "Publish"

---

## 四、配置 Personal Access Token（重要）

GitHub 已不再支持密码登录，需要使用 Token。

### 4.1 创建 Token

1. 登录 GitHub
2. 点击右上角头像 → "Settings"（设置）
3. 左侧菜单最下方 → "Developer settings"（开发者设置）
4. 左侧 → "Personal access tokens" → "Tokens (classic)"
5. 点击 "Generate new token" → "Generate new token (classic)"
6. 填写信息：
   - **Note**：`qiyun-restaurant-token`（备注）
   - **Expiration**：选择过期时间（建议 90 days）
   - **Select scopes**：勾选 `repo`（仓库权限）
7. 点击 "Generate token"
8. **复制 Token**（只显示一次，务必保存）

### 4.2 使用 Token 推送

```bash
# 推送时输入：
# Username: 你的 GitHub 用户名
# Password: 粘贴刚才复制的 Token
git push -u origin main
```

### 4.3 保存凭据（避免每次输入）

```bash
# Windows 系统
git config --global credential.helper wincred

# 下次推送时输入一次 Token，之后会自动保存
```

---

## 五、优化项目展示

### 5.1 创建精美的 README.md


你的项目已经有 README.md，但可以优化一下，添加以下内容：

**建议添加的内容**：
- 项目 Logo 或截图
- 技术栈徽章（Badges）
- 在线演示链接（如果有）
- 快速开始指南
- 项目架构图
- 贡献指南

**技术栈徽章示例**：
```markdown
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.x-blue)
![Vue](https://img.shields.io/badge/Vue-3.3.4-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Redis](https://img.shields.io/badge/Redis-7.2-red)
```

### 5.2 添加 .gitignore 文件

创建 `.gitignore` 文件，避免上传不必要的文件：

```bash
# 在项目根目录创建 .gitignore
```

**.gitignore 内容**：
```
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties

# Node
node_modules/
dist/
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# IDE
.idea/
*.iml
.vscode/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# 配置文件（包含敏感信息）
application-local.yml
application-prod.yml

# 临时文件
*.tmp
*.bak
```

### 5.3 创建项目截图目录

```bash
# 创建截图目录
mkdir docs/screenshots

# 将项目截图放入该目录
# 然后在 README.md 中引用
```

---

## 六、常用 Git 命令

### 6.1 日常操作

```bash
# 查看状态
git status

# 查看修改内容
git diff

# 添加文件
git add 文件名
git add .  # 添加所有文件

# 提交
git commit -m "提交说明"

# 推送到远程
git push

# 拉取远程更新
git pull
```

### 6.2 分支操作

```bash
# 查看分支
git branch

# 创建分支
git branch 分支名

# 切换分支
git checkout 分支名

# 创建并切换分支
git checkout -b 分支名

# 合并分支
git merge 分支名

# 删除分支
git branch -d 分支名
```

### 6.3 撤销操作

```bash
# 撤销工作区修改
git checkout -- 文件名

# 撤销暂存区修改
git reset HEAD 文件名

# 撤销最后一次提交
git reset --soft HEAD^

# 查看提交历史
git log
git log --oneline  # 简洁模式
```

---

## 七、项目上传检查清单

### 7.1 上传前检查

- [ ] 删除敏感信息（密码、密钥、Token）
- [ ] 添加 .gitignore 文件
- [ ] 更新 README.md
- [ ] 检查代码注释是否完整
- [ ] 确保项目可以正常运行
- [ ] 添加开源协议（LICENSE）

### 7.2 敏感信息处理

**需要删除或替换的内容**：
```yaml
# application.yml 中的敏感信息
spring:
  datasource:
    password: 你的密码  # 改为 ${DB_PASSWORD}
  
# 微信配置
wechat:
  appid: 你的AppID  # 改为 ${WECHAT_APPID}
  secret: 你的Secret  # 改为 ${WECHAT_SECRET}
```

**使用环境变量替代**：
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:root}  # 默认值 root
```

### 7.3 添加开源协议

在项目根目录创建 `LICENSE` 文件：

**MIT 协议（推荐）**：
```
MIT License

Copyright (c) 2024 你的名字

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 八、完整操作流程（快速版）

### 8.1 一键上传脚本

创建 `上传到GitHub.ps1` 脚本：

```powershell
# 上传到GitHub.ps1

Write-Host "开始上传项目到 GitHub..." -ForegroundColor Green

# 1. 初始化 Git 仓库
if (-not (Test-Path ".git")) {
    git init
    Write-Host "✓ Git 仓库初始化完成" -ForegroundColor Green
}

# 2. 添加所有文件
git add .
Write-Host "✓ 文件添加完成" -ForegroundColor Green

# 3. 提交
$commitMessage = Read-Host "请输入提交说明（默认：更新项目）"
if ([string]::IsNullOrWhiteSpace($commitMessage)) {
    $commitMessage = "更新项目"
}
git commit -m $commitMessage
Write-Host "✓ 提交完成" -ForegroundColor Green

# 4. 推送到远程
git push -u origin main
Write-Host "✓ 推送完成" -ForegroundColor Green

Write-Host "`n项目已成功上传到 GitHub！" -ForegroundColor Green
```

### 8.2 使用脚本

```bash
# 在 PowerShell 中执行
.\上传到GitHub.ps1
```

---

## 九、GitHub 项目优化建议

### 9.1 添加项目主页

在 GitHub 仓库页面：
1. 点击 "Settings"（设置）
2. 找到 "GitHub Pages"
3. 选择分支和目录
4. 保存后会生成项目主页链接

### 9.2 添加项目标签

在仓库页面：
1. 点击右侧 "About" 旁边的齿轮图标
2. 添加 Topics（标签）：
   - `spring-boot`
   - `spring-cloud`
   - `microservices`
   - `vue3`
   - `restaurant-management`
   - `wechat-miniprogram`

### 9.3 创建 Release

发布正式版本：
1. 点击 "Releases" → "Create a new release"
2. 填写版本号：`v1.0.0`
3. 填写发布说明
4. 上传编译好的文件（可选）
5. 点击 "Publish release"

---

## 十、常见问题解决

### 10.1 推送失败：403 错误

**原因**：Token 过期或权限不足

**解决**：
1. 重新生成 Token（见第四章）
2. 更新凭据：
   ```bash
   git config --global credential.helper wincred
   git push  # 重新输入 Token
   ```

### 10.2 推送失败：文件过大

**原因**：单个文件超过 100MB

**解决**：
1. 找到大文件：
   ```bash
   find . -size +100M
   ```
2. 添加到 .gitignore
3. 使用 Git LFS（大文件存储）

### 10.3 推送失败：冲突

**原因**：远程仓库有更新

**解决**：
```bash
# 拉取远程更新
git pull origin main

# 解决冲突后重新推送
git push origin main
```

### 10.4 忘记添加 .gitignore

**已经提交了不该提交的文件**：

```bash
# 从 Git 中删除，但保留本地文件
git rm --cached 文件名

# 添加到 .gitignore
echo "文件名" >> .gitignore

# 提交
git commit -m "移除敏感文件"
git push
```

---

## 十一、你的项目链接

完成上传后，你的项目链接将是：

**GitHub 仓库**：`https://github.com/你的用户名/qiyun-restaurant`

**个人主页**：`https://github.com/你的用户名`

**项目展示**：
- 可以在简历中添加这个链接
- 可以分享给面试官查看
- 可以作为作品集展示

---

## 十二、下一步建议

### 12.1 完善项目文档

- [ ] 添加详细的 API 文档
- [ ] 添加部署文档
- [ ] 添加开发指南
- [ ] 添加贡献指南

### 12.2 持续更新

```bash
# 每次修改后
git add .
git commit -m "描述你的修改"
git push
```

### 12.3 展示项目

- 在简历中添加 GitHub 链接
- 在 LinkedIn 中添加项目
- 在技术博客中介绍项目
- 参加开源活动

---

**恭喜！你现在拥有了自己的 GitHub 项目主页！** 🎉
