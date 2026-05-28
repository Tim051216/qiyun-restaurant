#!/usr/bin/env node

/**
 * 浏览器调试 MCP 服务器
 * 功能: 捕获浏览器控制台日志、错误、网络请求
 */

const { Server } = require('@modelcontextprotocol/sdk/server/index.js');
const { StdioServerTransport } = require('@modelcontextprotocol/sdk/server/stdio.js');
const {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} = require('@modelcontextprotocol/sdk/types.js');
const { chromium, firefox, webkit } = require('playwright');

let browser = null;
let page = null;
let consoleLogs = [];
let errors = [];
let networkLogs = [];

const server = new Server(
  {
    name: 'browser-debug',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// 工具列表
server.setRequestHandler(ListToolsRequestSchema, async () => ({
  tools: [
    {
      name: 'start_browser_debug',
      description: '启动浏览器并开始监听控制台日志、错误和网络请求',
      inputSchema: {
        type: 'object',
        properties: {
          url: {
            type: 'string',
            description: '要访问的URL(如 http://localhost:8090)',
          },
          browser: {
            type: 'string',
            description: '浏览器类型: chromium(Chrome), msedge(Edge), firefox, webkit(Safari)',
            enum: ['chromium', 'msedge', 'firefox', 'webkit'],
            default: 'msedge',
          },
          headless: {
            type: 'boolean',
            description: '是否无头模式(默认true)',
            default: true,
          },
        },
        required: ['url'],
      },
    },
    {
      name: 'get_console_logs',
      description: '获取浏览器控制台的所有日志(包括log、warn、error)',
      inputSchema: {
        type: 'object',
        properties: {
          type: {
            type: 'string',
            description: '日志类型: all(全部), error(仅错误), warn(仅警告)',
            enum: ['all', 'error', 'warn'],
            default: 'all',
          },
        },
      },
    },
    {
      name: 'get_network_logs',
      description: '获取网络请求日志(包括失败的请求)',
      inputSchema: {
        type: 'object',
        properties: {
          failed_only: {
            type: 'boolean',
            description: '是否只显示失败的请求',
            default: false,
          },
        },
      },
    },
    {
      name: 'execute_in_browser',
      description: '在浏览器中执行JavaScript代码并返回结果',
      inputSchema: {
        type: 'object',
        properties: {
          code: {
            type: 'string',
            description: 'JavaScript代码',
          },
        },
        required: ['code'],
      },
    },
    {
      name: 'take_screenshot',
      description: '截取当前页面截图',
      inputSchema: {
        type: 'object',
        properties: {
          path: {
            type: 'string',
            description: '保存路径(默认: screenshot.png)',
            default: 'screenshot.png',
          },
        },
      },
    },
    {
      name: 'stop_browser_debug',
      description: '停止浏览器调试并关闭浏览器',
      inputSchema: {
        type: 'object',
        properties: {},
      },
    },
  ],
}));

// 工具调用处理
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    switch (name) {
      case 'start_browser_debug': {
        // 清空之前的日志
        consoleLogs = [];
        errors = [];
        networkLogs = [];

        // 选择浏览器类型
        let browserType;
        const browserName = args.browser || 'msedge';
        
        if (browserName === 'msedge') {
          browserType = chromium;
          browser = await browserType.launch({
            headless: args.headless !== false,
            channel: 'msedge',
          });
        } else if (browserName === 'firefox') {
          browserType = firefox;
          browser = await browserType.launch({
            headless: args.headless !== false,
          });
        } else if (browserName === 'webkit') {
          browserType = webkit;
          browser = await browserType.launch({
            headless: args.headless !== false,
          });
        } else {
          browserType = chromium;
          browser = await browserType.launch({
            headless: args.headless !== false,
          });
        }
        
        page = await browser.newPage();

        // 监听控制台日志
        page.on('console', (msg) => {
          const log = {
            type: msg.type(),
            text: msg.text(),
            timestamp: new Date().toISOString(),
          };
          consoleLogs.push(log);
          
          if (msg.type() === 'error') {
            errors.push(log);
          }
        });

        // 监听页面错误
        page.on('pageerror', (error) => {
          const errorLog = {
            type: 'pageerror',
            text: error.message,
            stack: error.stack,
            timestamp: new Date().toISOString(),
          };
          errors.push(errorLog);
          consoleLogs.push(errorLog);
        });

        // 监听网络请求
        page.on('response', (response) => {
          const log = {
            url: response.url(),
            status: response.status(),
            method: response.request().method(),
            timestamp: new Date().toISOString(),
            failed: !response.ok(),
          };
          networkLogs.push(log);
        });

        // 访问页面
        await page.goto(args.url, { waitUntil: 'networkidle' });

        return {
          content: [
            {
              type: 'text',
              text: `浏览器已启动并访问 ${args.url}\n正在监听控制台日志、错误和网络请求...`,
            },
          ],
        };
      }

      case 'get_console_logs': {
        if (!page) {
          throw new Error('浏览器未启动,请先调用 start_browser_debug');
        }

        let logs = consoleLogs;
        if (args.type === 'error') {
          logs = errors;
        } else if (args.type === 'warn') {
          logs = consoleLogs.filter((log) => log.type === 'warning');
        }

        const formatted = logs.map((log) => 
          `[${log.timestamp}] [${log.type}] ${log.text}${log.stack ? '\n' + log.stack : ''}`
        ).join('\n\n');

        return {
          content: [
            {
              type: 'text',
              text: formatted || '暂无日志',
            },
          ],
        };
      }

      case 'get_network_logs': {
        if (!page) {
          throw new Error('浏览器未启动,请先调用 start_browser_debug');
        }

        let logs = networkLogs;
        if (args.failed_only) {
          logs = logs.filter((log) => log.failed);
        }

        const formatted = logs.map((log) => 
          `[${log.timestamp}] ${log.method} ${log.url} - ${log.status}${log.failed ? ' ❌' : ' ✓'}`
        ).join('\n');

        return {
          content: [
            {
              type: 'text',
              text: formatted || '暂无网络请求',
            },
          ],
        };
      }

      case 'execute_in_browser': {
        if (!page) {
          throw new Error('浏览器未启动,请先调用 start_browser_debug');
        }

        const result = await page.evaluate(args.code);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(result, null, 2),
            },
          ],
        };
      }

      case 'take_screenshot': {
        if (!page) {
          throw new Error('浏览器未启动,请先调用 start_browser_debug');
        }

        await page.screenshot({ path: args.path || 'screenshot.png' });
        return {
          content: [
            {
              type: 'text',
              text: `截图已保存到 ${args.path || 'screenshot.png'}`,
            },
          ],
        };
      }

      case 'stop_browser_debug': {
        if (browser) {
          await browser.close();
          browser = null;
          page = null;
        }

        const summary = {
          totalLogs: consoleLogs.length,
          errors: errors.length,
          networkRequests: networkLogs.length,
          failedRequests: networkLogs.filter((log) => log.failed).length,
        };

        return {
          content: [
            {
              type: 'text',
              text: `浏览器已关闭\n\n调试摘要:\n${JSON.stringify(summary, null, 2)}`,
            },
          ],
        };
      }

      default:
        throw new Error(`未知工具: ${name}`);
    }
  } catch (error) {
    return {
      content: [
        {
          type: 'text',
          text: `错误: ${error.message}`,
        },
      ],
      isError: true,
    };
  }
});

// 启动服务器
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('浏览器调试 MCP 服务器已启动');
}

main().catch((error) => {
  console.error('服务器启动失败:', error);
  process.exit(1);
});
