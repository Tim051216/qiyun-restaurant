# 七云菜馆商家后台管理系统 - 启动指南

## 📋 项目信息

- **技术栈**: Vue 3 + Vite + Element Plus
- **端口**: 8090（已修改，避免与Grafana的3000端口冲突）
- **后端API**: http://localhost:8080（Gateway网关）

## 🚀 快速启动

### 首次启动（需要安装依赖）

```powershell
cd F:\HBuilderProjects\restaurant-admin
npm install
npm run dev
```

### 后续启动

```powershell
cd F:\HBuilderProjects\restaurant-admin
npm run dev
```

启动成功后，浏览器会自动打开：**http://localhost:8090**

## 📦 可用命令

```powershell
# 开发模式（热重载）
npm run dev

# 生产构建
npm run build

# 预览生产构建
npm run preview
```

## 🔧 配置说明

### 端口配置

端口已修改为 **8090**，配置文件：`vite.config.js`

```javascript
server: {
  port: 8090,  // 前端端口
  open: true,  // 自动打开浏览器
  proxy: {     // API代理配置
    '/api': {
      target: 'http://localhost:8080',  // 后端Gateway地址
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

### API配置

后端API地址配置在 `src/utils/request.js`：

```javascript
const request = axios.create({
  baseURL: 'http://localhost:8080',  // Gateway地址
  timeout: 5000
})
```

## ⚠️ 注意事项

### 1. 确保后端服务已启动

前端需要连接后端API，请先启动：
- Gateway (8080)
- Dish Service (8082)
- Order Service (8081)
- Member Service (8083)
- Admin Service (8084)

启动后端服务：
```powershell
cd F:\HBuilderProjects
.\start-all-services.ps1
```

### 2. 端口冲突

如果8090端口被占用，可以修改 `vite.config.js` 中的端口号。

### 3. Node.js版本

建议使用 Node.js 16+ 版本。

检查版本：
```powershell
node -v
npm -v
```

## 🌐 访问地址

- **前端管理系统**: http://localhost:8090
  - **登录账号**: admin
  - **登录密码**: 123456
- **后端Gateway**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos

## 📝 开发说明

### 项目结构

```
restaurant-admin/
├── src/
│   ├── api/           # API接口
│   ├── layout/        # 布局组件
│   ├── router/        # 路由配置
│   ├── styles/        # 样式文件
│   ├── utils/         # 工具函数
│   └── views/         # 页面组件
│       ├── dashboard/ # 仪表盘
│       ├── login/     # 登录页
│       ├── menu/      # 菜品管理
│       ├── order/     # 订单管理
│       ├── member/    # 会员管理
│       ├── marketing/ # 营销管理
│       ├── statistics/# 统计分析
│       ├── system/    # 系统设置
│       └── table/     # 餐桌管理
├── index.html
├── package.json
└── vite.config.js
```

### 主要功能模块

1. **仪表盘** - 数据概览和统计图表
2. **菜品管理** - 菜品列表、分类管理
3. **订单管理** - 订单列表、实时订单
4. **会员管理** - 会员列表、等级管理
5. **营销管理** - 活动、优惠券、秒杀
6. **统计分析** - 销售统计、菜品统计
7. **系统设置** - 系统配置、员工管理
8. **餐桌管理** - 餐桌列表

## 🐛 常见问题

### Q1: npm install 失败？

**A**: 尝试清除缓存：
```powershell
npm cache clean --force
npm install
```

或使用淘宝镜像：
```powershell
npm install --registry=https://registry.npmmirror.com
```

### Q2: 启动后页面空白？

**A**: 
1. 检查浏览器控制台是否有错误
2. 确认后端服务是否正常运行
3. 检查API地址配置是否正确

### Q3: API请求失败？

**A**: 
1. 确认Gateway服务正常运行（http://localhost:8080）
2. 检查网络请求的URL是否正确
3. 查看浏览器Network面板的请求详情

### Q4: 热重载不工作？

**A**: 
1. 重启开发服务器
2. 清除浏览器缓存
3. 检查文件是否保存

## 📞 技术支持

如遇问题，请检查：
1. Node.js和npm版本是否符合要求
2. 后端服务是否正常运行
3. 端口是否被占用
4. 浏览器控制台的错误信息

---

**祝你开发愉快！** 🎉
