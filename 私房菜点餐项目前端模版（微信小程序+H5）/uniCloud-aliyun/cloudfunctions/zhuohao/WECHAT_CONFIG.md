# 微信小程序凭证配置指南

## ⚠️ 重要提示
AppSecret 是敏感信息，必须在云端配置，不能写在代码中！

## 🎯 推荐方法：uniCloud Web 控制台配置

这是最简单、最安全的方法：

### 详细步骤：

#### 1. 打开 uniCloud 控制台
在浏览器中访问：**https://unicloud.dcloud.net.cn/**

#### 2. 登录并选择云服务空间
- 使用你的 DCloud 账号登录
- 选择你的云服务空间（如果没有，先创建一个阿里云空间）

#### 3. 配置微信小程序凭证

**方式 A：使用 uni-open-bridge（推荐）**

1. 点击左侧菜单 **"云函数/云对象"**
2. 点击顶部 **"公共模块或扩展库"** 标签
3. 找到 **"uni-open-bridge"** 或点击 **"新建"**
4. 添加配置：

```json
{
  "mp-weixin": {
    "appid": "wxc63d199886077877",
    "appsecret": "e51393d39a5e8515bc97542eca91f74b"
  }
}
```

5. 保存配置

**方式 B：使用云函数环境变量**

1. 在 uniCloud 控制台，点击左侧 **"云函数/云对象"**
2. 找到并点击 **"zhuohao"** 云对象
3. 点击 **"云函数配置"** 或 **"环境变量"** 标签
4. 添加以下环境变量：

| 变量名 | 值 |
|--------|-----|
| WX_APPID | wxc63d199886077877 |
| WX_APPSECRET | e51393d39a5e8515bc97542eca91f74b |

5. 保存配置

#### 4. 验证配置

配置完成后，重新上传云对象，然后测试生成小程序码功能。

## 🔍 如何找到配置入口

### 在 uniCloud Web 控制台：

```
登录 uniCloud 控制台
    ↓
选择云服务空间
    ↓
左侧菜单：云函数/云对象
    ↓
顶部标签：公共模块或扩展库
    ↓
找到或新建：uni-open-bridge
    ↓
添加微信配置
```

## 📱 在 HBuilderX 中（如果找不到菜单）

如果在 HBuilderX 中找不到配置菜单，**直接使用 Web 控制台**更简单！

但如果你想在 HBuilderX 中操作：

1. **右键点击** `uniCloud-aliyun` 目录
2. 选择 **"打开 uniCloud Web 控制台"**
3. 会自动在浏览器中打开控制台
4. 按照上面的 Web 控制台步骤操作

## ✅ 配置验证

配置完成后，可以通过以下方式验证：

### 方法 1：在云函数测试窗口

```javascript
// 测试微信 API 是否可用
const wxApi = uniCloud.getWeixinAPI()
console.log('微信 API 对象:', wxApi)

// 测试生成小程序码
const result = await this.generateQRCode('测试桌1')
console.log('生成结果:', result)
```

### 方法 2：查看云函数日志

在 uniCloud 控制台：
1. 点击 **"云函数/云对象"** → **"zhuohao"**
2. 点击 **"日志"** 标签
3. 查看是否有微信 API 相关的错误

## 🚨 常见问题

### Q1: 找不到 uni-open-bridge？
**A**: 在 uniCloud 控制台的 "公共模块或扩展库" 中点击 "新建"，手动创建一个。

### Q2: 配置后还是报错？
**A**: 
1. 确认配置格式正确（JSON 格式）
2. 重新上传云对象
3. 等待 1-2 分钟让配置生效
4. 查看云函数日志

### Q3: HBuilderX 版本太旧？
**A**: 直接使用 Web 控制台配置，不依赖 HBuilderX 版本。

## 📝 配置信息

**你的微信小程序凭证：**

- AppID: `wxc63d199886077877`
- AppSecret: `e51393d39a5e8515bc97542eca91f74b`

**⚠️ 安全提醒：**
- 不要将 AppSecret 提交到 Git 仓库
- 不要在前端代码中使用 AppSecret
- 定期更换 AppSecret
- 只在云端配置中使用

## 🎯 快速链接

- [uniCloud 控制台](https://unicloud.dcloud.net.cn/)
- [uni-open-bridge 文档](https://uniapp.dcloud.net.cn/uniCloud/uni-open-bridge.html)
- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)

---

**总结：最简单的方法就是直接在浏览器中打开 uniCloud 控制台进行配置！**
