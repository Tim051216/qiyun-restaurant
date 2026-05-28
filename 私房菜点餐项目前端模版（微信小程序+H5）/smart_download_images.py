#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
智能菜品图片下载工具
先搜索并预览图片，确认质量后再下载
"""

import requests
import json
import os
from urllib.parse import quote

# Pixabay API配置
PIXABAY_API_KEY = "YOUR_API_KEY_HERE"  # 需要从 https://pixabay.com/api/docs/ 获取免费API Key
PIXABAY_API_URL = "https://pixabay.com/api/"

# 菜品列表（文件名 -> 中文名 -> 英文搜索词）
DISHES = [
    # 川味麻辣风
    {'file': 'gbyd.jpg', 'name': '干煸芸豆', 'search': 'dry fried green beans chinese', 'alt': 'sichuan green beans'},
    {'file': 'mpdf.jpg', 'name': '麻婆豆腐', 'search': 'mapo tofu', 'alt': 'spicy tofu chinese'},
    {'file': 'yxrs.jpg', 'name': '鱼香肉丝', 'search': 'yuxiang shredded pork', 'alt': 'fish flavored pork chinese'},
    {'file': 'szyu.jpg', 'name': '水煮鱼', 'search': 'sichuan boiled fish spicy', 'alt': 'chinese spicy fish soup'},
    {'file': 'lzj.jpg', 'name': '辣子鸡', 'search': 'chongqing spicy chicken', 'alt': 'sichuan chicken dried chili'},
    {'file': 'mxw.jpg', 'name': '毛血旺', 'search': 'mao xue wang spicy hot pot', 'alt': 'sichuan blood curd hot pot'},
    {'file': 'ksj.jpg', 'name': '口水鸡', 'search': 'saliva chicken sichuan', 'alt': 'chinese cold chicken spicy'},
    {'file': 'ffpp.jpg', 'name': '夫妻肺片', 'search': 'fuqi feipian sliced beef', 'alt': 'sichuan beef slices spicy'},
    
    # 家常小炒
    {'file': 'tclj.jpg', 'name': '糖醋里脊', 'search': 'sweet sour pork chinese', 'alt': 'crispy pork sweet sour'},
    {'file': 'jjrs.jpg', 'name': '京酱肉丝', 'search': 'beijing shredded pork sauce', 'alt': 'chinese pork hoisin sauce'},
    {'file': 'hgr.jpg', 'name': '回锅肉', 'search': 'twice cooked pork sichuan', 'alt': 'chinese pork belly pepper'},
    {'file': 'gbjd.jpg', 'name': '宫保鸡丁', 'search': 'kung pao chicken', 'alt': 'chinese chicken peanuts'},
    {'file': 'qjrs.jpg', 'name': '青椒肉丝', 'search': 'pork green pepper stir fry', 'alt': 'chinese pork bell pepper'},
    {'file': 'mxr.jpg', 'name': '木须肉', 'search': 'moo shu pork chinese', 'alt': 'pork egg mushroom chinese'},
    {'file': 'hsr.jpg', 'name': '红烧肉', 'search': 'braised pork belly chinese', 'alt': 'red braised pork'},
    {'file': 'xcr.jpg', 'name': '小炒肉', 'search': 'hunan stir fried pork', 'alt': 'chinese pork pepper stir fry'},
    {'file': 'myss.jpg', 'name': '蚂蚁上树', 'search': 'ants climbing tree noodles', 'alt': 'glass noodles minced pork'},
    
    # 时蔬素菜
    {'file': 'sltds.jpg', 'name': '酸辣土豆丝', 'search': 'sour spicy potato shreds', 'alt': 'chinese potato stir fry'},
    {'file': 'xhscjd.jpg', 'name': '西红柿炒鸡蛋', 'search': 'tomato scrambled eggs chinese', 'alt': 'stir fried tomato egg'},
    {'file': 'hsqz.jpg', 'name': '红烧茄子', 'search': 'braised eggplant chinese', 'alt': 'chinese eggplant garlic sauce'},
    {'file': 'dsx.jpg', 'name': '地三鲜', 'search': 'di san xian eggplant potato', 'alt': 'chinese three treasures vegetables'},
    {'file': 'qcss.jpg', 'name': '清炒时蔬', 'search': 'stir fried green vegetables', 'alt': 'chinese bok choy garlic'},
    {'file': 'gbdj.jpg', 'name': '干煸豆角', 'search': 'dry fried string beans', 'alt': 'sichuan green beans'},
    {'file': 'ssbc.jpg', 'name': '手撕包菜', 'search': 'hand torn cabbage chinese', 'alt': 'stir fried cabbage spicy'},
    {'file': 'srxlh.jpg', 'name': '蒜蓉西兰花', 'search': 'garlic broccoli chinese', 'alt': 'steamed broccoli garlic'},
    {'file': 'ymc.jpg', 'name': '油麦菜', 'search': 'chinese lettuce stir fried', 'alt': 'garlic lettuce asian'},
    
    # 汤品主食
    {'file': 'mf.jpg', 'name': '米饭', 'search': 'white rice bowl chinese', 'alt': 'steamed rice asian'},
    {'file': 'mt.jpg', 'name': '馒头', 'search': 'chinese steamed bun mantou', 'alt': 'white steamed buns'},
    {'file': 'slt.jpg', 'name': '酸辣汤', 'search': 'hot sour soup chinese', 'alt': 'spicy sour soup tofu'},
    {'file': 'gdt.jpg', 'name': '疙瘩汤', 'search': 'chinese dough drop soup', 'alt': 'noodle soup chinese'},
    {'file': 'zcdht.jpg', 'name': '紫菜蛋花汤', 'search': 'seaweed egg drop soup', 'alt': 'chinese seaweed soup'},
    {'file': 'xhsjdt.jpg', 'name': '西红柿鸡蛋汤', 'search': 'tomato egg soup chinese', 'alt': 'chinese tomato egg drop soup'},
    {'file': 'cf.jpg', 'name': '炒饭', 'search': 'chinese fried rice', 'alt': 'egg fried rice asian'},
    {'file': 'cm.jpg', 'name': '炒面', 'search': 'chinese fried noodles', 'alt': 'stir fried noodles vegetables'},
    {'file': 'lyt.jpg', 'name': '老鸭汤', 'search': 'chinese duck soup', 'alt': 'braised duck soup asian'},
    {'file': 'pgt.jpg', 'name': '排骨汤', 'search': 'pork rib soup chinese', 'alt': 'chinese spare rib soup'},
    
    # 夜宵烧烤
    {'file': 'kyrc.jpg', 'name': '烤羊肉串', 'search': 'lamb skewers grilled', 'alt': 'chinese lamb kebab'},
    {'file': 'kjc.jpg', 'name': '烤鸡翅', 'search': 'grilled chicken wings', 'alt': 'bbq chicken wings asian'},
    {'file': 'kyy.jpg', 'name': '烤鱿鱼', 'search': 'grilled squid chinese', 'alt': 'bbq squid skewers'},
    {'file': 'kjc2.jpg', 'name': '烤韭菜', 'search': 'grilled chinese chives', 'alt': 'bbq leeks asian'},
    {'file': 'kqz.jpg', 'name': '烤茄子', 'search': 'grilled eggplant chinese', 'alt': 'bbq eggplant garlic'},
    {'file': 'kym.jpg', 'name': '烤玉米', 'search': 'grilled corn', 'alt': 'bbq corn cob'},
    {'file': 'ksh.jpg', 'name': '烤生蚝', 'search': 'grilled oysters garlic', 'alt': 'bbq oysters chinese'},
    {'file': 'knrc.jpg', 'name': '烤牛肉串', 'search': 'beef skewers grilled', 'alt': 'chinese beef kebab'},
    {'file': 'kjx.jpg', 'name': '烤鸡心', 'search': 'grilled chicken hearts', 'alt': 'bbq chicken hearts skewers'},
    {'file': 'kjzg.jpg', 'name': '烤金针菇', 'search': 'grilled enoki mushrooms', 'alt': 'bbq mushrooms foil'},
    {'file': 'ktdp.jpg', 'name': '烤土豆片', 'search': 'grilled potato slices', 'alt': 'bbq potato wedges'},
    {'file': 'kmtp.jpg', 'name': '烤馒头片', 'search': 'grilled bread slices', 'alt': 'toasted bread chinese'},
    
    # 特色龙虾
    {'file': 'mlxlx.jpg', 'name': '麻辣小龙虾', 'search': 'spicy crayfish chinese', 'alt': 'sichuan crawfish spicy'},
    {'file': 'srxlx.jpg', 'name': '蒜蓉小龙虾', 'search': 'garlic crayfish chinese', 'alt': 'crawfish garlic sauce'},
    {'file': 'ssxlx.jpg', 'name': '十三香龙虾', 'search': 'spiced crayfish chinese', 'alt': 'crawfish thirteen spices'},
    {'file': 'ymdx.jpg', 'name': '油焖大虾', 'search': 'braised prawns chinese', 'alt': 'chinese shrimp tomato sauce'},
    {'file': 'qzlx.jpg', 'name': '清蒸龙虾', 'search': 'steamed lobster', 'alt': 'chinese steamed lobster'},
    {'file': 'zsjlx.jpg', 'name': '芝士焗龙虾', 'search': 'cheese baked lobster', 'alt': 'lobster cheese gratin'},
    {'file': 'bzxlx.jpg', 'name': '冰镇小龙虾', 'search': 'chilled crayfish', 'alt': 'cold crawfish ice'},
    {'file': 'jylx.jpg', 'name': '椒盐龙虾', 'search': 'salt pepper lobster', 'alt': 'fried lobster salt pepper'},
    {'file': 'bftlx.jpg', 'name': '避风塘龙虾', 'search': 'typhoon shelter lobster', 'alt': 'lobster fried garlic breadcrumbs'},
    
    # 饮品酒水
    {'file': 'kkl.jpg', 'name': '可口可乐', 'search': 'coca cola bottle', 'alt': 'coke glass ice'},
    {'file': 'xb.jpg', 'name': '雪碧', 'search': 'sprite bottle', 'alt': 'sprite glass lemon'},
    {'file': 'cz.jpg', 'name': '橙汁', 'search': 'orange juice glass', 'alt': 'fresh orange juice'},
    {'file': 'xgz.jpg', 'name': '西瓜汁', 'search': 'watermelon juice', 'alt': 'fresh watermelon drink'},
    {'file': 'nms.jpg', 'name': '柠檬水', 'search': 'lemon water glass', 'alt': 'lemonade ice'},
    {'file': 'smt.jpg', 'name': '酸梅汤', 'search': 'chinese plum juice', 'alt': 'sour plum drink'},
    {'file': 'nc.jpg', 'name': '奶茶', 'search': 'bubble tea', 'alt': 'milk tea boba'},
    {'file': 'dj.jpg', 'name': '豆浆', 'search': 'soy milk chinese', 'alt': 'soybean milk glass'},
    {'file': 'pj.jpg', 'name': '啤酒', 'search': 'beer glass', 'alt': 'cold beer bottle'},
    {'file': 'hj.jpg', 'name': '红酒', 'search': 'red wine glass', 'alt': 'wine bottle glass'},
    {'file': 'bj.jpg', 'name': '白酒', 'search': 'chinese liquor baijiu', 'alt': 'chinese white spirit bottle'},
    {'file': 'kqs.jpg', 'name': '矿泉水', 'search': 'mineral water bottle', 'alt': 'bottled water'},
]

def search_pixabay(query, per_page=5):
    """搜索Pixabay图片"""
    params = {
        'key': PIXABAY_API_KEY,
        'q': query,
        'image_type': 'photo',
        'per_page': per_page,
        'min_width': 800,
        'min_height': 600,
        'safesearch': 'true',
        'order': 'popular'
    }
    
    try:
        response = requests.get(PIXABAY_API_URL, params=params, timeout=10)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        print(f"   ❌ 搜索失败: {e}")
        return None

def generate_preview_html():
    """生成预览HTML页面"""
    html_content = """<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>菜品图片预览 - 请选择合适的图片</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
            background: #f5f5f5;
            padding: 20px;
        }
        .header {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .header h1 { color: #333; margin-bottom: 10px; }
        .header p { color: #666; line-height: 1.6; }
        .dish-section {
            background: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .dish-title {
            font-size: 20px;
            font-weight: bold;
            color: #333;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #4CAF50;
        }
        .dish-title .filename {
            color: #666;
            font-size: 14px;
            font-weight: normal;
            margin-left: 10px;
        }
        .images-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }
        .image-card {
            border: 2px solid #ddd;
            border-radius: 8px;
            overflow: hidden;
            cursor: pointer;
            transition: all 0.3s;
        }
        .image-card:hover {
            border-color: #4CAF50;
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        .image-card.selected {
            border-color: #4CAF50;
            border-width: 3px;
        }
        .image-card img {
            width: 100%;
            height: 200px;
            object-fit: cover;
            display: block;
        }
        .image-info {
            padding: 10px;
            background: #f9f9f9;
        }
        .image-info .size {
            font-size: 12px;
            color: #666;
        }
        .image-info .select-btn {
            margin-top: 8px;
            width: 100%;
            padding: 6px;
            background: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        .image-info .select-btn:hover {
            background: #45a049;
        }
        .no-results {
            padding: 20px;
            text-align: center;
            color: #999;
        }
        .search-info {
            color: #666;
            font-size: 14px;
            margin-bottom: 10px;
        }
        .download-section {
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.2);
            max-width: 300px;
        }
        .download-btn {
            width: 100%;
            padding: 12px;
            background: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            font-weight: bold;
        }
        .download-btn:hover {
            background: #45a049;
        }
        .selected-count {
            margin-bottom: 10px;
            color: #333;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>🍜 菜品图片预览与选择</h1>
        <p>
            请为每道菜选择最合适的图片。点击图片或"选择"按钮进行选择。<br>
            选择完成后，点击右下角的"生成下载脚本"按钮。
        </p>
    </div>
    
    <div id="dishes-container"></div>
    
    <div class="download-section">
        <div class="selected-count">已选择: <span id="count">0</span> / """ + str(len(DISHES)) + """</div>
        <button class="download-btn" onclick="generateDownloadScript()">生成下载脚本</button>
    </div>
    
    <script>
        const selections = {};
        
        function selectImage(dishFile, imageUrl, imageSize) {
            selections[dishFile] = { url: imageUrl, size: imageSize };
            
            // 更新UI
            document.querySelectorAll(`[data-dish="${dishFile}"]`).forEach(card => {
                card.classList.remove('selected');
            });
            document.querySelector(`[data-dish="${dishFile}"][data-url="${imageUrl}"]`).classList.add('selected');
            
            // 更新计数
            document.getElementById('count').textContent = Object.keys(selections).length;
        }
        
        function generateDownloadScript() {
            if (Object.keys(selections).length === 0) {
                alert('请至少选择一张图片！');
                return;
            }
            
            const script = `#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# 自动生成的下载脚本

import requests
import os

DOWNLOADS = ${JSON.stringify(selections, null, 4)}

def download_images():
    output_dir = 'static/menu/menulist'
    os.makedirs(output_dir, exist_ok=True)
    
    for filename, info in DOWNLOADS.items():
        print(f"下载: {filename}...")
        try:
            response = requests.get(info['url'], timeout=30)
            response.raise_for_status()
            
            filepath = os.path.join(output_dir, filename)
            with open(filepath, 'wb') as f:
                f.write(response.content)
            print(f"  ✅ 成功 ({info['size']})")
        except Exception as e:
            print(f"  ❌ 失败: {e}")

if __name__ == '__main__':
    download_images()
    print("\\n下载完成！")
`;
            
            // 下载脚本文件
            const blob = new Blob([script], { type: 'text/plain' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'download_selected_images.py';
            a.click();
            URL.revokeObjectURL(url);
            
            alert('下载脚本已生成！请运行 download_selected_images.py 下载图片。');
        }
    </script>
</body>
</html>"""
    
    return html_content

def main():
    """主函数"""
    print("=" * 60)
    print("智能菜品图片搜索工具")
    print("=" * 60)
    print()
    
    # 检查API Key
    if PIXABAY_API_KEY == "YOUR_API_KEY_HERE":
        print("⚠️  请先设置Pixabay API Key")
        print()
        print("获取步骤：")
        print("1. 访问 https://pixabay.com/")
        print("2. 注册/登录账号")
        print("3. 访问 https://pixabay.com/api/docs/")
        print("4. 复制你的API Key")
        print("5. 在本脚本中替换 PIXABAY_API_KEY 的值")
        print()
        print("或者使用无API方式：")
        print("直接打开生成的 preview.html 文件，手动搜索并选择图片")
        return
    
    # 搜索所有菜品
    results = {}
    print("🔍 开始搜索图片...")
    print()
    
    for i, dish in enumerate(DISHES, 1):
        print(f"[{i}/{len(DISHES)}] {dish['name']} ({dish['file']})")
        print(f"   搜索: {dish['search']}")
        
        # 主搜索
        data = search_pixabay(dish['search'])
        if data and data.get('hits'):
            results[dish['file']] = {
                'name': dish['name'],
                'search': dish['search'],
                'images': data['hits'][:5]
            }
            print(f"   ✅ 找到 {len(data['hits'])} 张图片")
        else:
            # 备用搜索
            print(f"   尝试备用搜索: {dish['alt']}")
            data = search_pixabay(dish['alt'])
            if data and data.get('hits'):
                results[dish['file']] = {
                    'name': dish['name'],
                    'search': dish['alt'],
                    'images': data['hits'][:5]
                }
                print(f"   ✅ 找到 {len(data['hits'])} 张图片")
            else:
                print(f"   ❌ 未找到合适图片")
        print()
    
    # 生成预览HTML
    print("📄 生成预览页面...")
    html = generate_preview_html()
    
    # 注入搜索结果
    dishes_html = ""
    for dish_file, dish_data in results.items():
        dishes_html += f"""
        <div class="dish-section">
            <div class="dish-title">
                {dish_data['name']}
                <span class="filename">{dish_file}</span>
            </div>
            <div class="search-info">搜索词: {dish_data['search']}</div>
            <div class="images-grid">
        """
        
        for img in dish_data['images']:
            size = f"{img['imageWidth']}×{img['imageHeight']}"
            dishes_html += f"""
                <div class="image-card" data-dish="{dish_file}" data-url="{img['largeImageURL']}"
                     onclick="selectImage('{dish_file}', '{img['largeImageURL']}', '{size}')">
                    <img src="{img['webformatURL']}" alt="{dish_data['name']}">
                    <div class="image-info">
                        <div class="size">{size}</div>
                        <button class="select-btn">选择此图</button>
                    </div>
                </div>
            """
        
        dishes_html += """
            </div>
        </div>
        """
    
    html = html.replace('<div id="dishes-container"></div>', 
                       f'<div id="dishes-container">{dishes_html}</div>')
    
    # 保存HTML
    with open('preview_images.html', 'w', encoding='utf-8') as f:
        f.write(html)
    
    print("✅ 预览页面已生成: preview_images.html")
    print()
    print("下一步：")
    print("1. 在浏览器中打开 preview_images.html")
    print("2. 为每道菜选择最合适的图片")
    print("3. 点击'生成下载脚本'按钮")
    print("4. 运行生成的 download_selected_images.py")

if __name__ == '__main__':
    main()
