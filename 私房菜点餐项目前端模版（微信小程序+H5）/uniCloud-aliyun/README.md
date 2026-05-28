# 桌号管理功能 - 技术文档

## 项目概述

这是一个基于 **vue2 + uniapp + uniCloud** 的扫码点餐微信小程序的桌号管理功能模块。

### 核心功能

1. ✅ **生成桌号小程序码** - 为每张餐桌生成唯一的微信小程序码
2. ✅ **扫码识别** - 顾客扫码自动识别桌号并打开小程序
3. ✅ **桌号弹窗** - 显示当前桌号并选择就餐人数
4. ✅ **数据管理** - 查询、列表、删除桌号记录
5. ✅ **错误处理** - 完整的输入验证和错误处理机制
6. ✅ **失败回滚** - 操作失败自动回滚，保证数据一致性

## 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    微信小程序前端                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  扫码入口     │  │  主页面       │  │  桌号弹窗     │  │
│  │  (QR Code)   │→ │  (index.vue) │→ │  (Popup)     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   uniCloud 云端                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  云对象 (zhuohao)                                  │  │
│  │  - generateQRCode()  生成小程序码                  │  │
│  │  - getTableInfo()    查询桌号信息                  │  │
│  │  - getTableList()    获取桌号列表                  │  │
│  │  - deleteTable()     删除桌号                      │  │
│  └──────────────────────────────────────────────────┘  │
│                            ↓                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  微信 API     │  │  云数据库     │  │  云存储       │  │
│  │  (小程序码)   │  │  (zhuohao)   │  │  (qrcodes/)  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 目录结构

```
uniCloud-aliyun/
├── cloudfunctions/
│   └── zhuohao/                    # 桌号管理云对象
│       ├── index.obj.js            # 云对象主文件
│       ├── package.json            # 依赖配置
│       ├── config.md               # 配置说明
│       └── test.js                 # 测试脚本
├── database/
│   └── zhuohao.schema.json         # 数据库 Schema
├── DEPLOYMENT_GUIDE.md             # 部署指南
└── README.md                       # 本文件

pages/
└── index/
    └── index.vue                   # 主页面（含扫码识别和弹窗）
```

## 数据库设计

### zhuohao 表

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| _id | string | 是 | 记录 ID（自动生成） |
| create_time | timestamp | 是 | 创建时间（自动生成） |
| table_number | string | 是 | 桌号（1-32字符，唯一） |
| qrcode_image | string | 是 | 小程序码图片 URL |

**索引：**
- `table_number` - 唯一索引

## API 文档

### 1. generateQRCode(tableNumber)

生成桌号小程序码

**参数：**
- `tableNumber` (string) - 桌号，1-32字符，仅支持中文、字母、数字

**返回：**
```javascript
{
  success: true,
  data: {
    id: "记录ID",
    qrcodeUrl: "小程序码URL",
    tableNumber: "桌号"
  }
}
```

**错误码：**
- `1002` - 桌号不能为空
- `1003` - 桌号格式不正确
- `1004` - 桌号已存在
- `2001` - 微信 API 调用失败
- `2002` - 云存储上传失败
- `5000` - 系统错误

### 2. getTableInfo(tableNumber)

查询桌号信息

**参数：**
- `tableNumber` (string) - 桌号

**返回：**
```javascript
{
  success: true,
  data: {
    _id: "记录ID",
    table_number: "桌号",
    qrcode_image: "小程序码URL",
    create_time: "创建时间"
  }
}
```

### 3. getTableList(pageSize, pageNum)

获取桌号列表

**参数：**
- `pageSize` (number) - 每页数量，默认 20
- `pageNum` (number) - 页码，默认 1

**返回：**
```javascript
{
  success: true,
  data: {
    list: [...],      // 桌号列表
    total: 100,       // 总数
    pageSize: 20,     // 每页数量
    pageNum: 1        // 当前页码
  }
}
```

### 4. deleteTable(tableNumber)

删除桌号

**参数：**
- `tableNumber` (string) - 桌号

**返回：**
```javascript
{
  success: true,
  data: {
    deleted: 1,
    tableNumber: "桌号"
  }
}
```

## 使用示例

### 前端调用

```javascript
// 导入云对象
const zhuohao = uniCloud.importObject('zhuohao')

// 生成桌号小程序码
const result = await zhuohao.generateQRCode('桌1')
if (result.success) {
  console.log('小程序码URL:', result.data.qrcodeUrl)
}

// 查询桌号信息
const info = await zhuohao.getTableInfo('桌1')
console.log('桌号信息:', info.data)

// 获取桌号列表
const list = await zhuohao.getTableList(20, 1)
console.log('桌号列表:', list.data.list)

// 删除桌号
const deleteResult = await zhuohao.deleteTable('桌1')
console.log('删除结果:', deleteResult)
```

### 扫码流程

1. 用户扫描桌号小程序码
2. 小程序启动，`onLoad` 接收 `scene` 参数
3. 解析 `scene` 获取桌号
4. 显示桌号弹窗
5. 用户选择就餐人数
6. 保存桌号和人数到本地存储
7. 用户开始点餐

```javascript
// pages/index/index.vue
onLoad(options) {
  if (options.scene) {
    const tableNumber = decodeURIComponent(options.scene)
    this.tableNumber = tableNumber
    this.showTableDialog = true
  }
}
```

## 配置信息

### 微信小程序

- **AppID**: `wxc63d199886077877`
- **AppSecret**: 请在 uniCloud 控制台配置（不要提交到代码仓库）

### 小程序码参数

- **scene**: 桌号（最多 32 字符）
- **page**: `pages/index/index`
- **width**: 280px
- **isHyaline**: false

## 部署流程

详细部署步骤请查看 [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md)

**快速部署：**

1. 配置微信凭证（uniCloud 控制台）
2. 上传数据库 Schema
3. 创建数据库索引
4. 上传云对象
5. 测试功能
6. 发布小程序

## 测试

### 运行测试

```bash
# 在 HBuilderX 中
1. 右键点击 zhuohao 云对象
2. 选择"运行-本地云函数"
3. 复制 test.js 中的测试代码
4. 运行测试
```

### 测试用例

- ✅ 生成单个桌号小程序码
- ✅ 验证空桌号
- ✅ 验证特殊字符
- ✅ 验证超长桌号
- ✅ 测试重复桌号
- ✅ 查询桌号信息
- ✅ 获取桌号列表
- ✅ 删除桌号
- ✅ 批量生成桌号

## 安全性

### 输入验证

- ✅ 桌号非空验证
- ✅ 桌号长度验证（1-32字符）
- ✅ 桌号格式验证（仅中文、字母、数字）
- ✅ 桌号唯一性验证
- ✅ 人数范围验证（1-20）

### 错误处理

- ✅ 统一错误响应格式
- ✅ 详细错误日志记录
- ✅ 用户友好的错误提示
- ✅ 失败自动回滚机制

### 数据安全

- ✅ AppSecret 不在代码中硬编码
- ✅ 使用 uniCloud 配置中心管理凭证
- ✅ 数据库权限控制
- ✅ 输入清理和转义

## 性能优化

- ✅ 数据库索引优化
- ✅ 数据库操作重试机制
- ✅ 分页查询支持
- ✅ 错误日志记录

## 监控指标

建议监控以下指标：

- 小程序码生成成功率
- 平均生成时间
- 扫码识别成功率
- 数据库查询响应时间
- 错误率

## 常见问题

### Q1: 生成小程序码失败？

**A**: 检查以下几点：
1. AppID 和 AppSecret 是否正确配置
2. 微信 API 配额是否充足
3. 网络连接是否正常
4. 查看云函数日志

### Q2: 扫码无法识别桌号？

**A**: 检查以下几点：
1. 桌号长度是否超过 32 字符
2. 小程序页面路径是否正确
3. 小程序是否已发布或在开发者列表中

### Q3: 数据库写入失败？

**A**: 检查以下几点：
1. 桌号是否重复
2. 数据格式是否符合 Schema
3. 数据库权限是否正确配置

## 后续开发

可选功能（参考 tasks.md）：

- [ ] 创建独立的桌号弹窗组件
- [ ] 添加权限控制
- [ ] 创建管理端界面
- [ ] 批量生成和导出功能
- [ ] 小程序码打印功能
- [ ] 数据统计和分析

## 技术支持

- [uniCloud 官方文档](https://uniapp.dcloud.net.cn/uniCloud/)
- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- [uni-app 官方文档](https://uniapp.dcloud.net.cn/)

## 版本历史

### v1.0.0 (2024)

- ✅ 完成数据库 Schema 设计
- ✅ 实现云对象核心功能
- ✅ 实现前端扫码识别
- ✅ 实现桌号弹窗
- ✅ 完整的错误处理机制
- ✅ 部署文档和测试脚本

## 许可证

本项目仅供学习和参考使用。
