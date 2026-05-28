# 微信小程序配置说明

## 重要提示
**AppSecret 是敏感信息，不应该直接写在代码中！**

## 配置方法

### 方法一：使用 uniCloud Web 控制台配置（推荐）

1. 登录 [uniCloud 控制台](https://unicloud.dcloud.net.cn/)
2. 选择你的云服务空间（阿里云）
3. 点击左侧菜单"云函数/云对象"
4. 点击"公共模块或扩展库"
5. 找到并配置"uni-open-bridge"扩展
6. 在配置中填入：
   - AppID: `wxc63d199886077877`
   - AppSecret: `e51393d39a5e8515bc97542eca91f74b`

### 方法二：使用云函数环境变量

1. 在 uniCloud 控制台选择你的云服务空间
2. 进入"云函数/云对象" → 选择 `zhuohao` 云对象
3. 点击"云函数配置"
4. 添加环境变量：
   ```
   WX_APPID=wxc63d199886077877
   WX_APPSECRET=e51393d39a5e8515bc97542eca91f74b
   ```

### 方法三：使用 uniCloud 的配置中心

在云对象中通过 `uniCloud.getWeixinAPI()` 调用时，uniCloud 会自动从配置中心读取凭证。

## 当前配置

- **AppID**: wxc63d199886077877
- **AppSecret**: e51393d39a5e8515bc97542eca91f74b（请在云端配置，不要提交到代码仓库）

## 使用说明

代码中已经使用了 `uniCloud.getWeixinAPI()` 方法，这个方法会自动从云端配置中读取 AppID 和 AppSecret，无需在代码中硬编码。

```javascript
const wxApi = uniCloud.getWeixinAPI()
const qrcodeResult = await wxApi.getUnlimitedQRCode({
  scene: tableNumber,
  page: 'pages/index/index',
  width: 280,
  isHyaline: false
})
```

## 安全建议

1. ✅ 已在 manifest.json 中配置 AppID
2. ⚠️ 请在 uniCloud 控制台配置 AppSecret
3. ⚠️ 不要将 AppSecret 提交到 Git 仓库
4. ⚠️ 定期更换 AppSecret
5. ✅ 使用 uniCloud 的安全机制管理凭证
