# 🖼️ 菜品图片批量下载工具

## 📦 工具包含

本工具包提供了完整的菜品图片下载和处理方案：

### 核心脚本
1. **download_images_simple.py** - 简化版下载脚本（推荐）
   - ✅ 无需 API Key
   - ✅ 使用 Unsplash 免费图片
   - ✅ 一键下载 76 道菜品图片

2. **download_dish_images.py** - 高级版下载脚本
   - 使用 Pixabay API
   - 搜索结果更精准
   - 需要免费注册获取 API Key

3. **compress_images.py** - 图片压缩脚本
   - 批量压缩图片
   - 调整尺寸
   - 优化文件大小

4. **一键下载并压缩图片.bat** - Windows 批处理脚本
   - 一键运行
   - 图形化菜单
   - 自动安装依赖

### 文档
- **图片下载脚本使用说明.md** - 详细使用教程
- **菜品图片资源指南.md** - 图片资源和搜索关键词

## 🚀 快速开始（3 步完成）

### Windows 用户（最简单）

1. **双击运行**
   ```
   一键下载并压缩图片.bat
   ```

2. **选择操作**
   - 选项 1：仅下载图片
   - 选项 2：仅压缩图片
   - 选项 3：下载并压缩（推荐）

3. **等待完成**
   - 脚本会自动完成所有操作
   - 图片保存在 `static/menu/menulist/`

### Mac/Linux 用户

1. **安装依赖**
   ```bash
   pip3 install requests pillow
   ```

2. **下载图片**
   ```bash
   python3 download_images_simple.py
   ```

3. **压缩图片**
   ```bash
   python3 compress_images.py
   ```

## 📋 下载清单

脚本会自动下载以下 76 道菜品的图片：

- 川味麻辣风：8 道
- 家常小炒：9 道
- 时蔬素菜：9 道
- 汤品主食：10 道
- 夜宵烧烤：12 道
- 特色龙虾：9 道
- 饮品酒水：12 道

详细清单请查看 `菜品图片资源指南.md`

## ⚙️ 配置选项

### 下载配置（download_images_simple.py）

```python
OUTPUT_DIR = "./static/menu/menulist"  # 图片保存目录
```

### 压缩配置（compress_images.py）

```python
QUALITY = 85          # JPEG 质量 (1-100)
MAX_WIDTH = 800       # 最大宽度
MAX_HEIGHT = 800      # 最大高度
CONVERT_TO_JPG = True # 转换为 JPG
```

## 📊 预期效果

### 下载
- 时间：约 5-10 分钟
- 数量：76 张图片
- 格式：JPG
- 尺寸：800x800 像素

### 压缩
- 压缩率：约 50-70%
- 单张大小：< 200KB
- 总大小：< 15MB

## 🔧 常见问题

### Q1: 提示 "No module named 'requests'"
```bash
pip install requests
```

### Q2: 某些图片下载失败
- 检查网络连接
- 重新运行脚本（会跳过已下载的）
- 手动下载失败的图片

### Q3: 图片质量不满意
- 调整 `QUALITY` 参数（提高到 90-95）
- 或手动替换不合适的图片

### Q4: 图片太大
- 运行压缩脚本
- 或使用在线工具：https://tinypng.com/

### Q5: Python 版本问题
```bash
# 使用 python3
python3 download_images_simple.py
python3 compress_images.py
```

## 📝 手动下载指南

如果自动下载失败，可以手动下载：

1. **访问图片网站**
   - Pixabay: https://pixabay.com/images/search/chinese%20food/
   - Unsplash: https://unsplash.com/s/photos/chinese-food
   - Pexels: https://www.pexels.com/search/chinese%20food/

2. **搜索菜品**
   - 参考 `菜品图片资源指南.md` 中的搜索关键词
   - 例如：搜索 "mapo tofu" 下载麻婆豆腐图片

3. **下载并重命名**
   - 下载图片
   - 重命名为对应的文件名（如 mpdf.jpg）
   - 放到 `static/menu/menulist/` 目录

## 🎯 最佳实践

1. **先测试少量**
   - 修改脚本只下载前 5 道菜
   - 检查图片质量
   - 满意后再下载全部

2. **保留备份**
   - 压缩前备份原图
   - 以防需要重新处理

3. **统一风格**
   - 选择相似风格的图片
   - 保持色调一致
   - 建议俯拍或 45 度角

4. **定期更新**
   - 根据季节更换图片
   - 使用真实菜品照片更佳

## 📂 文件结构

```
私房菜点餐项目前端模版（微信小程序+H5）/
├── download_images_simple.py      # 简化版下载脚本
├── download_dish_images.py        # 高级版下载脚本
├── compress_images.py             # 图片压缩脚本
├── 一键下载并压缩图片.bat         # Windows 批处理
├── 图片下载脚本使用说明.md        # 详细教程
├── 菜品图片资源指南.md            # 资源指南
├── 图片下载工具README.md          # 本文件
└── static/
    └── menu/
        └── menulist/              # 图片保存目录
            ├── gbyd.jpg          # 干煸芸豆
            ├── mpdf.jpg          # 麻婆豆腐
            └── ...               # 其他图片
```

## 🌟 功能特点

- ✅ 全自动下载 76 道菜品图片
- ✅ 无需 API Key（简化版）
- ✅ 自动压缩优化
- ✅ 智能跳过已下载
- ✅ 详细进度显示
- ✅ 失败自动重试
- ✅ 一键批处理
- ✅ 跨平台支持

## 📞 需要帮助？

1. 查看 `图片下载脚本使用说明.md`
2. 查看 `菜品图片资源指南.md`
3. 检查 Python 和依赖安装
4. 尝试手动下载测试

## 📄 许可说明

- 脚本代码：MIT License
- 下载的图片：遵循 Unsplash/Pixabay 许可
  - Unsplash: 免费商用
  - Pixabay: 免费商用
- 请遵守图片网站的使用条款

## 🔄 更新日志

### v1.0.0 (2025-02-19)
- ✨ 初始版本
- ✅ 支持 76 道菜品图片下载
- ✅ 自动压缩功能
- ✅ Windows 批处理脚本
- ✅ 完整文档

---

**祝你使用愉快！** 🎉

如有问题或建议，欢迎反馈。
