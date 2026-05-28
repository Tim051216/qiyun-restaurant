# 桌号管理功能部署指南

## 前置条件

- [x] 已安装 HBuilderX
- [x] 已创建 uniCloud 云服务空间（阿里云）
- [x] 已获取微信小程序 AppID 和 AppSecret
- [x] 已完成代码开发

## 部署步骤

### 第一步：配置微信小程序凭证

#### 1.1 在 uniCloud 控制台配置

1. 打开 [uniCloud 控制台](https://unicloud.dcloud.net.cn/)
2. 选择你的云服务空间
3. 点击左侧"云函数/云对象"
4. 点击"公共模块或扩展库"
5. 找到"uni-open-bridge"或创建新的配置
6. 配置微信小程序信息：
   ```
   AppID: wxc63d199886077877
   AppSecret: e51393d39a5e8515bc97542eca91f74b
   ```

#### 1.2 或使用环境变量（备选方案）

在云对象配置中添加环境变量：
```
WX_APPID=wxc63d199886077877
WX_APPSECRET=e51393d39a5e8515bc97542eca91f74b
```

### 第二步：上传数据库 Schema

1. 在 HBuilderX 中，右键点击 `uniCloud-aliyun/database/zhuohao.schema.json`
2. 选择"上传 DB Schema"
3. 等待上传完成

或者在 uniCloud 控制台：
1. 进入"云数据库"
2. 点击"新建表"
3. 表名输入：`zhuohao`
4. 复制 `zhuohao.schema.json` 的内容粘贴到 Schema 编辑器
5. 保存

### 第三步：创建数据库索引

在 uniCloud 控制台的云数据库中：

1. 选择 `zhuohao` 表
2. 点击"索引管理"
3. 创建唯一索引：
   - 字段：`table_number`
   - 类型：唯一索引
   - 名称：`idx_table_number_unique`

或使用 JQL 命令：
```javascript
db.collection('zhuohao').createIndex({
  table_number: 1
}, { unique: true })
```

### 第四步：上传云对象

1. 在 HBuilderX 中，右键点击 `uniCloud-aliyun/cloudfunctions/zhuohao` 目录
2. 选择"上传部署"
3. 选择"上传并运行"
4. 等待部署完成

### 第五步：测试云对象

#### 5.1 在 HBuilderX 中测试

1. 右键点击 `zhuohao` 云对象
2. 选择"运行-本地云函数"
3. 在弹出的测试窗口中测试各个方法

#### 5.2 测试生成小程序码

```javascript
// 测试代码
const zhuohao = uniCloud.importObject('zhuohao')

// 生成桌号小程序码
const result = await zhuohao.generateQRCode('桌1')
console.log('生成结果:', result)

// 查询桌号信息
const info = await zhuohao.getTableInfo('桌1')
console.log('桌号信息:', info)

// 获取桌号列表
const list = await zhuohao.getTableList()
console.log('桌号列表:', list)
```

### 第六步：配置云存储

1. 在 uniCloud 控制台进入"云存储"
2. 确保有足够的存储空间
3. 查看 `qrcodes/` 目录（小程序码会自动上传到这里）

### 第七步：发布小程序

#### 7.1 编译小程序

1. 在 HBuilderX 中点击"运行" → "运行到小程序模拟器" → "微信开发者工具"
2. 在微信开发者工具中预览效果
3. 测试扫码功能

#### 7.2 上传小程序

1. 在 HBuilderX 中点击"发行" → "小程序-微信"
2. 填写版本号和项目备注
3. 点击"发行"
4. 在微信开发者工具中点击"上传"
5. 登录微信小程序后台提交审核

### 第八步：生成测试桌号

#### 8.1 使用云函数测试

在 HBuilderX 的云函数测试窗口中：

```javascript
// 批量生成桌号
for (let i = 1; i <= 10; i++) {
  const result = await zhuohao.generateQRCode(`桌${i}`)
  console.log(`桌${i} 生成结果:`, result)
}
```

#### 8.2 下载小程序码

1. 在 uniCloud 控制台进入"云存储"
2. 进入 `qrcodes/` 目录
3. 下载生成的小程序码图片
4. 打印或展示在餐桌上

### 第九步：测试扫码流程

1. 使用微信扫描生成的小程序码
2. 小程序应该自动打开并显示桌号弹窗
3. 选择就餐人数
4. 点击确认
5. 验证桌号和人数是否正确保存

## 验证清单

- [ ] 数据库 Schema 已上传
- [ ] 数据库索引已创建
- [ ] 云对象已部署
- [ ] 微信凭证已配置
- [ ] 云存储可用
- [ ] 生成小程序码成功
- [ ] 扫码识别正常
- [ ] 桌号弹窗显示正常
- [ ] 人数选择功能正常
- [ ] 数据保存到本地存储

## 常见问题

### 1. 生成小程序码失败

**可能原因：**
- AppID 或 AppSecret 配置错误
- 微信 API 配额不足
- 网络连接问题

**解决方案：**
- 检查 uniCloud 控制台的微信凭证配置
- 查看云函数日志
- 确认微信小程序已发布或在开发者列表中

### 2. 扫码无法识别桌号

**可能原因：**
- scene 参数超过 32 字符
- 小程序页面路径错误
- 小程序未发布

**解决方案：**
- 确保桌号长度不超过 32 字符
- 检查 `pages/index/index` 路径是否正确
- 在微信开发者工具中测试

### 3. 数据库写入失败

**可能原因：**
- 桌号重复
- Schema 验证失败
- 权限不足

**解决方案：**
- 检查桌号是否已存在
- 验证数据格式是否符合 Schema
- 检查数据库权限配置

## 监控和维护

### 日志查看

在 uniCloud 控制台：
1. 进入"云函数/云对象"
2. 选择 `zhuohao` 云对象
3. 点击"日志"查看运行日志

### 性能监控

关注以下指标：
- 小程序码生成成功率
- 平均生成时间
- 数据库查询响应时间
- 错误率

### 定期维护

- 定期清理过期的小程序码图片
- 备份数据库数据
- 更新微信 AppSecret
- 检查云存储空间使用情况

## 下一步

部署完成后，你可以：

1. 创建管理端界面（任务 9）
   - 批量生成桌号
   - 查看桌号列表
   - 下载小程序码

2. 添加权限控制（任务 8）
   - 管理员权限验证
   - 操作日志记录

3. 性能优化（任务 12）
   - 添加缓存
   - 优化图片大小
   - 添加监控

## 技术支持

如遇到问题，请查看：
- [uniCloud 官方文档](https://uniapp.dcloud.net.cn/uniCloud/)
- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- 云函数日志
- 浏览器控制台
