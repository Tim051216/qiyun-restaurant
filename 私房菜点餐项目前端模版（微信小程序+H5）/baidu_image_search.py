#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
百度图片搜索工具 - 完全免费方案
生成搜索链接，手动选择并下载图片
"""

import webbrowser
import os
from urllib.parse import quote

# 菜品列表
DISHES = [
    # 川味麻辣风
    {'file': 'gbyd.jpg', 'name': '干煸芸豆', 'category': '川味麻辣风'},
    {'file': 'mpdf.jpg', 'name': '麻婆豆腐', 'category': '川味麻辣风'},
    {'file': 'yxrs.jpg', 'name': '鱼香肉丝', 'category': '川味麻辣风'},
    {'file': 'szyu.jpg', 'name': '水煮鱼', 'category': '川味麻辣风'},
    {'file': 'lzj.jpg', 'name': '辣子鸡', 'category': '川味麻辣风'},
    {'file': 'mxw.jpg', 'name': '毛血旺', 'category': '川味麻辣风'},
    {'file': 'ksj.jpg', 'name': '口水鸡', 'category': '川味麻辣风'},
    {'file': 'ffpp.jpg', 'name': '夫妻肺片', 'category': '川味麻辣风'},
    
    # 家常小炒
    {'file': 'tclj.jpg', 'name': '糖醋里脊', 'category': '家常小炒'},
    {'file': 'jjrs.jpg', 'name': '京酱肉丝', 'category': '家常小炒'},
    {'file': 'hgr.jpg', 'name': '回锅肉', 'category': '家常小炒'},
    {'file': 'gbjd.jpg', 'name': '宫保鸡丁', 'category': '家常小炒'},
    {'file': 'qjrs.jpg', 'name': '青椒肉丝', 'category': '家常小炒'},
    {'file': 'mxr.jpg', 'name': '木须肉', 'category': '家常小炒'},
    {'file': 'hsr.jpg', 'name': '红烧肉', 'category': '家常小炒'},
    {'file': 'xcr.jpg', 'name': '小炒肉', 'category': '家常小炒'},
    {'file': 'myss.jpg', 'name': '蚂蚁上树', 'category': '家常小炒'},
    
    # 时蔬素菜
    {'file': 'sltds.jpg', 'name': '酸辣土豆丝', 'category': '时蔬素菜'},
    {'file': 'xhscjd.jpg', 'name': '西红柿炒鸡蛋', 'category': '时蔬素菜'},
    {'file': 'hsqz.jpg', 'name': '红烧茄子', 'category': '时蔬素菜'},
    {'file': 'dsx.jpg', 'name': '地三鲜', 'category': '时蔬素菜'},
    {'file': 'qcss.jpg', 'name': '清炒时蔬', 'category': '时蔬素菜'},
    {'file': 'gbdj.jpg', 'name': '干煸豆角', 'category': '时蔬素菜'},
    {'file': 'ssbc.jpg', 'name': '手撕包菜', 'category': '时蔬素菜'},
    {'file': 'srxlh.jpg', 'name': '蒜蓉西兰花', 'category': '时蔬素菜'},
    {'file': 'ymc.jpg', 'name': '油麦菜', 'category': '时蔬素菜'},
    
    # 汤品主食
    {'file': 'mf.jpg', 'name': '米饭', 'category': '汤品主食'},
    {'file': 'mt.jpg', 'name': '馒头', 'category': '汤品主食'},
    {'file': 'slt.jpg', 'name': '酸辣汤', 'category': '汤品主食'},
    {'file': 'gdt.jpg', 'name': '疙瘩汤', 'category': '汤品主食'},
    {'file': 'zcdht.jpg', 'name': '紫菜蛋花汤', 'category': '汤品主食'},
    {'file': 'xhsjdt.jpg', 'name': '西红柿鸡蛋汤', 'category': '汤品主食'},
    {'file': 'cf.jpg', 'name': '炒饭', 'category': '汤品主食'},
    {'file': 'cm.jpg', 'name': '炒面', 'category': '汤品主食'},
    {'file': 'lyt.jpg', 'name': '老鸭汤', 'category': '汤品主食'},
    {'file': 'pgt.jpg', 'name': '排骨汤', 'category': '汤品主食'},
    
    # 夜宵烧烤
    {'file': 'kyrc.jpg', 'name': '烤羊肉串', 'category': '夜宵烧烤'},
    {'file': 'kjc.jpg', 'name': '烤鸡翅', 'category': '夜宵烧烤'},
    {'file': 'kyy.jpg', 'name': '烤鱿鱼', 'category': '夜宵烧烤'},
    {'file': 'kjc2.jpg', 'name': '烤韭菜', 'category': '夜宵烧烤'},
    {'file': 'kqz.jpg', 'name': '烤茄子', 'category': '夜宵烧烤'},
    {'file': 'kym.jpg', 'name': '烤玉米', 'category': '夜宵烧烤'},
    {'file': 'ksh.jpg', 'name': '烤生蚝', 'category': '夜宵烧烤'},
    {'file': 'knrc.jpg', 'name': '烤牛肉串', 'category': '夜宵烧烤'},
    {'file': 'kjx.jpg', 'name': '烤鸡心', 'category': '夜宵烧烤'},
    {'file': 'kjzg.jpg', 'name': '烤金针菇', 'category': '夜宵烧烤'},
    {'file': 'ktdp.jpg', 'name': '烤土豆片', 'category': '夜宵烧烤'},
    {'file': 'kmtp.jpg', 'name': '烤馒头片', 'category': '夜宵烧烤'},
    
    # 特色龙虾
    {'file': 'mlxlx.jpg', 'name': '麻辣小龙虾', 'category': '特色龙虾'},
    {'file': 'srxlx.jpg', 'name': '蒜蓉小龙虾', 'category': '特色龙虾'},
    {'file': 'ssxlx.jpg', 'name': '十三香龙虾', 'category': '特色龙虾'},
    {'file': 'ymdx.jpg', 'name': '油焖大虾', 'category': '特色龙虾'},
    {'file': 'qzlx.jpg', 'name': '清蒸龙虾', 'category': '特色龙虾'},
    {'file': 'zsjlx.jpg', 'name': '芝士焗龙虾', 'category': '特色龙虾'},
    {'file': 'bzxlx.jpg', 'name': '冰镇小龙虾', 'category': '特色龙虾'},
    {'file': 'jylx.jpg', 'name': '椒盐龙虾', 'category': '特色龙虾'},
    {'file': 'bftlx.jpg', 'name': '避风塘龙虾', 'category': '特色龙虾'},
    
    # 饮品酒水
    {'file': 'kkl.jpg', 'name': '可口可乐', 'category': '饮品酒水'},
    {'file': 'xb.jpg', 'name': '雪碧', 'category': '饮品酒水'},
    {'file': 'cz.jpg', 'name': '橙汁', 'category': '饮品酒水'},
    {'file': 'xgz.jpg', 'name': '西瓜汁', 'category': '饮品酒水'},
    {'file': 'nms.jpg', 'name': '柠檬水', 'category': '饮品酒水'},
    {'file': 'smt.jpg', 'name': '酸梅汤', 'category': '饮品酒水'},
    {'file': 'nc.jpg', 'name': '奶茶', 'category': '饮品酒水'},
    {'file': 'dj.jpg', 'name': '豆浆', 'category': '饮品酒水'},
    {'file': 'pj.jpg', 'name': '啤酒', 'category': '饮品酒水'},
    {'file': 'hj.jpg', 'name': '红酒', 'category': '饮品酒水'},
    {'file': 'bj.jpg', 'name': '白酒', 'category': '饮品酒水'},
    {'file': 'kqs.jpg', 'name': '矿泉水', 'category': '饮品酒水'},
]

def generate_html():
    """生成简化的搜索页面"""
    html = """<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>菜品图片下载工具 - 百度图片版</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: "Microsoft YaHei", Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container { max-width: 1200px; margin: 0 auto; }
        .header {
            background: white;
            padding: 30px;
            border-radius: 12px;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }
        .header h1 { color: #333; margin-bottom: 15px; font-size: 28px; }
        .header p { color: #666; line-height: 1.8; margin-bottom: 10px; }
        .steps {
            background: #e3f2fd;
            border-left: 4px solid #2196F3;
            padding: 15px;
            margin-top: 15px;
            border-radius: 4px;
        }
        .steps h3 { color: #1976D2; margin-bottom: 10px; }
        .steps ol { margin-left: 20px; }
        .steps li { margin: 8px 0; color: #555; }
        .category-section {
            background: white;
            padding: 25px;
            border-radius: 12px;
            margin-bottom: 25px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }
        .category-title {
            font-size: 22px;
            font-weight: bold;
            color: #333;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 3px solid #667eea;
        }
        .dish-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 15px;
        }
        .dish-item {
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            padding: 15px;
            background: #fafafa;
            transition: all 0.3s;
        }
        .dish-item:hover {
            border-color: #667eea;
            background: white;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .dish-name {
            font-size: 16px;
            font-weight: bold;
            color: #333;
            margin-bottom: 8px;
        }
        .dish-file {
            font-size: 12px;
            color: #999;
            margin-bottom: 12px;
        }
        .search-btn {
            display: block;
            width: 100%;
            padding: 10px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            text-align: center;
            border-radius: 6px;
            font-weight: bold;
            transition: transform 0.2s;
        }
        .search-btn:hover {
            transform: scale(1.05);
        }
        .tip {
            background: #fff3cd;
            border: 1px solid #ffc107;
            padding: 15px;
            border-radius: 8px;
            margin-top: 20px;
        }
        .tip strong { color: #856404; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🍜 菜品图片下载工具（百度图片版）</h1>
            <p>
                这是一个完全免费的解决方案！使用百度图片搜索，手动下载高质量菜品图片。
            </p>
            <div class="steps">
                <h3>📝 使用步骤：</h3>
                <ol>
                    <li>点击下方的"搜索图片"按钮，会在新标签页打开百度图片搜索</li>
                    <li>在搜索结果中找到合适的图片</li>
                    <li>右键点击图片 → "图片另存为" → 保存到 <code>static/menu/menulist/</code> 目录</li>
                    <li>文件名必须与下方显示的文件名一致（如 gbyd.jpg）</li>
                    <li>重复以上步骤完成所有菜品</li>
                </ol>
            </div>
            <div class="tip">
                <strong>💡 选图建议：</strong>
                选择清晰、有食欲感、俯拍或45度角的图片。避免带水印的图片。
            </div>
        </div>
"""
    
    # 按分类组织菜品
    categories = {}
    for dish in DISHES:
        cat = dish['category']
        if cat not in categories:
            categories[cat] = []
        categories[cat].append(dish)
    
    # 生成每个分类的HTML
    for category, dishes in categories.items():
        html += f"""
        <div class="category-section">
            <div class="category-title">{category}</div>
            <div class="dish-grid">
"""
        for dish in dishes:
            search_url = f"https://image.baidu.com/search/index?tn=baiduimage&word={quote(dish['name'])}"
            html += f"""
                <div class="dish-item">
                    <div class="dish-name">{dish['name']}</div>
                    <div class="dish-file">文件名: {dish['file']}</div>
                    <a href="{search_url}" target="_blank" class="search-btn">
                        🔍 搜索图片
                    </a>
                </div>
"""
        html += """
            </div>
        </div>
"""
    
    html += """
    </div>
</body>
</html>"""
    
    return html

def main():
    """主函数"""
    print("=" * 60)
    print("菜品图片下载工具 - 百度图片版")
    print("=" * 60)
    print()
    
    # 生成HTML
    html = generate_html()
    filename = 'baidu_image_download.html'
    
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(html)
    
    print(f"✅ 已生成搜索页面: {filename}")
    print()
    print("使用步骤：")
    print("1. 页面将在浏览器中打开")
    print("2. 点击'搜索图片'按钮")
    print("3. 在百度图片中找到合适的图片")
    print("4. 右键 → 图片另存为")
    print("5. 保存到: static/menu/menulist/")
    print("6. 文件名必须与页面显示的一致")
    print()
    print("⚠️  注意：")
    print("- 图片仅用于学习和开发")
    print("- 商业使用需要获得授权")
    print("- 建议选择无水印的图片")
    print()
    
    # 在浏览器中打开
    filepath = os.path.abspath(filename)
    webbrowser.open('file://' + filepath)
    print("✅ 已在浏览器中打开搜索页面")

if __name__ == '__main__':
    main()
