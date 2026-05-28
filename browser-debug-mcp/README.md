# 浏览器调试 MCP 服务器

自动捕获浏览器控制台日志、错误和网络请求,无需手动复制粘贴。

## 功能特性

- ✅ 自动捕获控制台日志(log、warn、error)
- ✅ 捕获页面错误和堆栈信息
- ✅ 监控网络请求(包括失败的请求)
- ✅ 在浏览器中执行 JavaScript 代码
- ✅ 截图保存
- ✅ 支持多浏览器(Edge、Chrome、Firefox、Safari)
- ✅ 支持有头/无头模式
- ✅ 默认使用 Edge 浏览器

## 安装步骤

### 1. 安装依赖
```bash
cd browser-debug-mcp
npm install
```

### 2. 安装 Playwright 浏览器
```bash
# Edge 浏览器(推荐,已默认配置)
# 如果您已安装 Edge,无需额外操作

# 或安装其他浏览器
npx playwright install chromium  # Chrome
npx playwright install firefox   # Firefox
```

### 3. 配置 MCP
将以下配置添加到 `~/.kiro/settings/mcp.json`:

```json
{
  "mcpServers": {
    "browser-debug": {
      "command": "node",
      "args": ["C:/Users/Administrator/七云菜馆/browser-debug-mcp/server.js"],
      "disabled": false,
      "autoApprove": ["get_console_logs", "get_network_logs"]
    }
  }
}
```

## 使用方法

### 场景: 调试餐厅管理后台

**1. 启动浏览器监听(默认使用 Edge)**
```
"启动浏览器调试 http://localhost:8090"
```

AI 会调用 `start_browser_debug` 工具,Edge 浏览器会自动打开并开始监听。

**1.1 指定其他浏览器**
```
"用 Chrome 启动浏览器调试 http://localhost:8090"
"用 Firefox 启动浏览器调试 http://localhost:8090"
```

**2. 自动获取控制台错误**
```
"查看控制台有什么错误"
```

AI 会调用 `get_console_logs` 工具,直接返回所有错误信息,无需您手动复制!

**3. 查看网络请求**
```
"查看失败的网络请求"
```

AI 会调用 `get_network_logs` 工具,显示所有 404、500 等失败请求。

**4. 执行调试代码**
```
"在浏览器中执行 localStorage.getItem('token')"
```

AI 会在浏览器控制台执行代码并返回结果。

**5. 截图保存**
```
"截图保存当前页面"
```

**6. 停止调试**
```
"停止浏览器调试"
```

## 可用工具

| 工具名 | 功能 | 参数 |
|--------|------|------|
| `start_browser_debug` | 启动浏览器并监听 | url, browser(msedge/chromium/firefox/webkit), headless |
| `get_console_logs` | 获取控制台日志 | type(all/error/warn) |
| `get_network_logs` | 获取网络请求 | failed_only |
| `execute_in_browser` | 执行JS代码 | code |
| `take_screenshot` | 截图 | path |
| `stop_browser_debug` | 停止调试 | - |

## 实际应用示例

### 调试登录问题
```
您: "启动浏览器调试 http://localhost:8090,然后点击登录"
AI: 启动浏览器,自动点击登录按钮
您: "查看控制台错误"
AI: 返回 "Uncaught TypeError: Cannot read property 'token' of undefined"
您: "查看失败的网络请求"
AI: 返回 "POST /api/login - 401 ❌"
```

### 调试 Vue 组件
```
您: "在浏览器中执行 window.__VUE_DEVTOOLS_GLOBAL_HOOK__"
AI: 返回 Vue DevTools 信息
```

## 优势

**之前**: 
1. 您打开浏览器
2. 手动操作触发错误
3. 复制控制台错误
4. 粘贴给 AI
5. AI 分析

**现在**:
1. 您说 "启动浏览器调试"
2. AI 自动获取所有错误
3. 直接分析并修复

节省时间 80%!

## 注意事项

- 浏览器会在后台运行(无头模式)或前台显示(有头模式)
- 所有日志会实时捕获,即使页面刷新也会保留
- 建议在开发环境使用,生产环境请谨慎
- 截图会保存在当前工作目录

## 故障排查

**问题: 浏览器启动失败**
```bash
# Edge 浏览器
npx playwright install msedge --force

# 或使用 Chrome
npx playwright install chromium --force
```

**问题: MCP 服务器连接失败**
- 检查 mcp.json 中的路径是否正确
- 确保 Node.js 版本 >= 18
- 查看 Kiro 的 MCP 日志

**问题: 无法捕获日志**
- 确保先调用 `start_browser_debug`
- 检查页面是否真的有错误输出
