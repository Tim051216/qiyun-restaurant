#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
菜品图片预览和下载工具
使用 Unsplash Source API（无需注册）
"""

import webbrowser
import os

# 菜品列表
DISHES = [
    # 川味麻辣风
    {'file': 'gbyd.jpg', 'name': '干煸芸豆', 'search': 'dry-fried-green-beans,sichuan-beans'},
    {'file': 'mpdf.jpg', 'name': '麻婆豆腐', 'search': 'mapo-tofu,spicy-tofu'},
    {'file': 'yxrs.jpg', 'name': '鱼香肉丝', 'search': 'yuxiang-pork,fish-flavored-pork'},
    {'file': 'szyu.jpg', 'name': '水煮鱼', 'search': 'sichuan-boiled-fish,spicy-fish'},
    {'file': 'lzj.jpg', 'name': '辣子鸡', 'search': 'chongqing-chicken,spicy-chicken-chili'},
    {'file': 'mxw.jpg', 'name': '毛血旺', 'search': 'sichuan-hot-pot,spicy-blood-curd'},
    {'file': 'ksj.jpg', 'name': '口水鸡', 'search': 'saliva-chicken,cold-chicken-spicy'},
    {'file': 'ffpp.jpg', 'name': '夫妻肺片', 'search': 'sichuan-beef-slices,spicy-beef'},
    
    # 家常小炒
    {'file': 'tclj.jpg', 'name': '糖醋里脊', 'search': 'sweet-sour-pork,crispy-pork'},
    {'file': 'jjrs.jpg', 'name': '京酱肉丝', 'search': 'beijing-pork-sauce,hoisin-pork'},
    {'file': 'hgr.jpg', 'name': '回锅肉', 'search': 'twice-cooked-pork,pork-belly-pepper'},
    {'file': 'gbjd.jpg', 'name': '宫保鸡丁', 'search': 'kung-pao-chicken,chicken-peanuts'},
    {'file': 'qjrs.jpg', 'name': '青椒肉丝', 'search': 'pork-green-pepper,stir-fry-pork'},
    {'file': 'mxr.jpg', 'name': '木须肉', 'search': 'moo-shu-pork,pork-egg-mushroom'},
    {'file': 'hsr.jpg', 'name': '红烧肉', 'search': 'braised-pork-belly,red-braised-pork'},
    {'file': 'xcr.jpg', 'name': '小炒肉', 'search': 'hunan-pork,stir-fried-pork-pepper'},
    {'file': 'myss.jpg', 'name': '蚂蚁上树', 'search': 'glass-noodles-pork,vermicelli-minced-pork'},
    
    # 时蔬素菜
    {'file': 'sltds.jpg', 'name': '酸辣土豆丝', 'search': 'sour-spicy-potato,potato-shreds'},
    {'file': 'xhscjd.jpg', 'name': '西红柿炒鸡蛋', 'search': 'tomato-scrambled-eggs,stir-fried-tomato-egg'},
    {'file': 'hsqz.jpg', 'name': '红烧茄子', 'search': 'braised-eggplant,chinese-eggplant'},
    {'file': 'dsx.jpg', 'name': '地三鲜', 'search': 'eggplant-potato-pepper,three-treasures'},
    {'file': 'qcss.jpg', 'name': '清炒时蔬', 'search': 'stir-fried-vegetables,bok-choy'},
    {'file': 'gbdj.jpg', 'name': '干煸豆角', 'search': 'dry-fried-beans,sichuan-string-beans'},
    {'file': 'ssbc.jpg', 'name': '手撕包菜', 'search': 'hand-torn-cabbage,stir-fried-cabbage'},
    {'file': 'srxlh.jpg', 'name': '蒜蓉西兰花', 'search': 'garlic-broccoli,steamed-broccoli'},
    {'file': 'ymc.jpg', 'name': '油麦菜', 'search': 'chinese-lettuce,garlic-lettuce'},
    
    # 汤品主食
    {'file': 'mf.jpg', 'name': '米饭', 'search': 'white-rice-bowl,steamed-rice'},
    {'file': 'mt.jpg', 'name': '馒头', 'search': 'chinese-steamed-bun,mantou'},
    {'file': 'slt.jpg', 'name': '酸辣汤', 'search': 'hot-sour-soup,spicy-sour-soup'},
    {'file': 'gdt.jpg', 'name': '疙瘩汤', 'search': 'dough-drop-soup,chinese-noodle-soup'},
    {'file': 'zcdht.jpg', 'name': '紫菜蛋花汤', 'search': 'seaweed-egg-soup,chinese-seaweed'},
    {'file': 'xhsjdt.jpg', 'name': '西红柿鸡蛋汤', 'search': 'tomato-egg-soup,chinese-tomato-soup'},
    {'file': 'cf.jpg', 'name': '炒饭', 'search': 'chinese-fried-rice,egg-fried-rice'},
    {'file': 'cm.jpg', 'name': '炒面', 'search': 'chinese-fried-noodles,stir-fried-noodles'},
    {'file': 'lyt.jpg', 'name': '老鸭汤', 'search': 'chinese-duck-soup,braised-duck'},
    {'file': 'pgt.jpg', 'name': '排骨汤', 'search': 'pork-rib-soup,spare-rib-soup'},
    
    # 夜宵烧烤
    {'file': 'kyrc.jpg', 'name': '烤羊肉串', 'search': 'lamb-skewers,grilled-lamb-kebab'},
    {'file': 'kjc.jpg', 'name': '烤鸡翅', 'search': 'grilled-chicken-wings,bbq-wings'},
    {'file': 'kyy.jpg', 'name': '烤鱿鱼', 'search': 'grilled-squid,bbq-squid'},
    {'file': 'kjc2.jpg', 'name': '烤韭菜', 'search': 'grilled-chives,bbq-leeks'},
    {'file': 'kqz.jpg', 'name': '烤茄子', 'search': 'grilled-eggplant,bbq-eggplant'},
    {'file': 'kym.jpg', 'name': '烤玉米', 'search': 'grilled-corn,bbq-corn'},
    {'file': 'ksh.jpg', 'name': '烤生蚝', 'search': 'grilled-oysters,bbq-oysters'},
    {'file': 'knrc.jpg', 'name': '烤牛肉串', 'search': 'beef-skewers,grilled-beef-kebab'},
    {'file': 'kjx.jpg', 'name': '烤鸡心', 'search': 'grilled-chicken-hearts,bbq-hearts'},
    {'file': 'kjzg.jpg', 'name': '烤金针菇', 'search': 'grilled-enoki-mushrooms,bbq-mushrooms'},
    {'file': 'ktdp.jpg', 'name': '烤土豆片', 'search': 'grilled-potato,bbq-potato'},
    {'file': 'kmtp.jpg', 'name': '烤馒头片', 'search': 'grilled-bread,toasted-bread'},
    
    # 特色龙虾
    {'file': 'mlxlx.jpg', 'name': '麻辣小龙虾', 'search': 'spicy-crayfish,sichuan-crawfish'},
    {'file': 'srxlx.jpg', 'name': '蒜蓉小龙虾', 'search': 'garlic-crayfish,crawfish-garlic'},
    {'file': 'ssxlx.jpg', 'name': '十三香龙虾', 'search': 'spiced-crayfish,chinese-crawfish'},
    {'file': 'ymdx.jpg', 'name': '油焖大虾', 'search': 'braised-prawns,chinese-shrimp'},
    {'file': 'qzlx.jpg', 'name': '清蒸龙虾', 'search': 'steamed-lobster,chinese-lobster'},
    {'file': 'zsjlx.jpg', 'name': '芝士焗龙虾', 'search': 'cheese-baked-lobster,lobster-gratin'},
    {'file': 'bzxlx.jpg', 'name': '冰镇小龙虾', 'search': 'chilled-crayfish,cold-crawfish'},
    {'file': 'jylx.jpg', 'name': '椒盐龙虾', 'search': 'salt-pepper-lobster,fried-lobster'},
    {'file': 'bftlx.jpg', 'name': '避风塘龙虾', 'search': 'typhoon-shelter-lobster,fried-garlic-lobster'},
    
    # 饮品酒水
    {'file': 'kkl.jpg', 'name': '可口可乐', 'search': 'coca-cola,coke-glass'},
    {'file': 'xb.jpg', 'name': '雪碧', 'search': 'sprite,lemon-soda'},
    {'file': 'cz.jpg', 'name': '橙汁', 'search': 'orange-juice,fresh-orange'},
    {'file': 'xgz.jpg', 'name': '西瓜汁', 'search': 'watermelon-juice,fresh-watermelon'},
    {'file': 'nms.jpg', 'name': '柠檬水', 'search': 'lemon-water,lemonade'},
    {'file': 'smt.jpg', 'name': '酸梅汤', 'search': 'plum-juice,chinese-plum-drink'},
    {'file': 'nc.jpg', 'name': '奶茶', 'search': 'bubble-tea,milk-tea-boba'},
    {'file': 'dj.jpg', 'name': '豆浆', 'search': 'soy-milk,soybean-milk'},
    {'file': 'pj.jpg', 'name': '啤酒', 'search': 'beer-glass,cold-beer'},
    {'file': 'hj.jpg', 'name': '红酒', 'search': 'red-wine,wine-glass'},
    {'file': 'bj.jpg', 'name': '白酒', 'search': 'chinese-liquor,baijiu'},
    {'file': 'kqs.jpg', 'name': '矿泉水', 'search': 'mineral-water,bottled-water'},
]

def generate_preview_html():
    """生成交互式预览页面"""
    html = """<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>菜品图片搜索与预览</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
        }
        .header {
            background: white;
            padding: 30px;
            border-radius: 12px;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }
        .header h1 { 
            color: #333; 
            margin-bottom: 15px;
            font-size: 32px;
        }
        .header p { 
            color: #666; 
            line-height: 1.8;
            font-size: 16px;
        }
        .instructions {
            background: #fff3cd;
            border-left: 4px solid #ffc107;
            padding: 15px;
            margin-top: 15px;
            border-radius: 4px;
        }
        .instructions strong { color: #856404; }
        .dish-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
            gap: 20px;
        }
        .dish-card {
            background: white;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            transition: transform 0.3s;
        }
        .dish-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 20px rgba(0,0,0,0.25);
        }
        .dish-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
        }
        .dish-name {
            font-size: 20px;
            font-weight: bold;
            margin-bottom: 5px;
        }
        .dish-file {
            font-size: 13px;
            opacity: 0.9;
        }
        .search-section {
            padding: 20px;
        }
        .search-links {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .search-btn {
            flex: 1;
            min-width: 140px;
            padding: 10px 15px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            transition: all 0.3s;
            text-decoration: none;
            text-align: center;
            display: inline-block;
        }
        .btn-unsplash {
            background: #000;
            color: white;
        }
        .btn-unsplash:hover {
            background: #333;
        }
        .btn-pexels {
            background: #05A081;
            color: white;
        }
        .btn-pexels:hover {
            background: #048a6e;
        }
        .btn-pixabay {
            background: #2ecc71;
            color: white;
        }
        .btn-pixabay:hover {
            background: #27ae60;
        }
        .url-input-section {
            margin-top: 15px;
            padding-top: 15px;
            border-top: 1px solid #eee;
        }
        .url-input {
            width: 100%;
            padding: 10px;
            border: 2px solid #ddd;
            border-radius: 6px;
            font-size: 14px;
            margin-bottom: 10px;
        }
        .url-input:focus {
            outline: none;
            border-color: #667eea;
        }
        .save-btn {
            width: 100%;
            padding: 10px;
            background: #4CAF50;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
        }
        .save-btn:hover {
            background: #45a049;
        }
        .save-btn:disabled {
            background: #ccc;
            cursor: not-allowed;
        }
        .status {
            margin-top: 10px;
            padding: 8px;
            border-radius: 4px;
            font-size: 13px;
            text-align: center;
        }
        .status.success {
            background: #d4edda;
            color: #155724;
        }
        .download-panel {
            position: fixed;
            bottom: 30px;
            right: 30px;
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.3);
            min-width: 280px;
            z-index: 1000;
        }
        .download-title {
            font-size: 18px;
            font-weight: bold;
            margin-bottom: 15px;
            color: #333;
        }
        .progress {
            font-size: 24px;
            font-weight: bold;
            color: #667eea;
            margin-bottom: 15px;
        }
        .generate-btn {
            width: 100%;
            padding: 15px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 16px;
            font-weight: bold;
            transition: transform 0.2s;
        }
        .generate-btn:hover {
            transform: scale(1.05);
        }
        .generate-btn:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🍜 菜品图片搜索与下载工具</h1>
            <p>
                为每道菜搜索合适的图片，找到满意的图片后，复制图片URL并保存。
                完成所有菜品后，点击右下角生成下载脚本。
            </p>
            <div class="instructions">
                <strong>使用说明：</strong><br>
                1. 点击搜索按钮在图片网站查找合适的图片<br>
                2. 在图片网站上右键点击图片 → "复制图片地址"<br>
                3. 粘贴到下方输入框并点击"保存URL"<br>
                4. 完成所有菜品后点击右下角"生成下载脚本"
            </div>
        </div>
        
        <div class="dish-grid" id="dishGrid"></div>
        
        <div class="download-panel">
            <div class="download-title">下载进度</div>
            <div class="progress"><span id="progress">0</span> / """ + str(len(DISHES)) + """</div>
            <button class="generate-btn" id="generateBtn" onclick="generateScript()" disabled>
                生成下载脚本
            </button>
        </div>
    </div>
    
    <script>
        const dishes = """ + str(DISHES).replace("'", '"') + """;
        const savedUrls = {};
        
        function createDishCard(dish) {
            const searches = dish.search.split(',');
            const card = document.createElement('div');
            card.className = 'dish-card';
            card.innerHTML = `
                <div class="dish-header">
                    <div class="dish-name">${dish.name}</div>
                    <div class="dish-file">${dish.file}</div>
                </div>
                <div class="search-section">
                    <div class="search-links">
                        <a href="https://unsplash.com/s/photos/${searches[0]}" 
                           target="_blank" class="search-btn btn-unsplash">
                            🔍 Unsplash
                        </a>
                        <a href="https://www.pexels.com/search/${searches[0]}/" 
                           target="_blank" class="search-btn btn-pexels">
                            🔍 Pexels
                        </a>
                        <a href="https://pixabay.com/images/search/${searches[0]}/" 
                           target="_blank" class="search-btn btn-pixabay">
                            🔍 Pixabay
                        </a>
                    </div>
                    <div class="url-input-section">
                        <input type="text" 
                               class="url-input" 
                               id="url_${dish.file}"
                               placeholder="粘贴图片URL（右键图片→复制图片地址）">
                        <button class="save-btn" onclick="saveUrl('${dish.file}')">
                            保存 URL
                        </button>
                        <div class="status" id="status_${dish.file}" style="display:none;"></div>
                    </div>
                </div>
            `;
            return card;
        }
        
        function saveUrl(filename) {
            const input = document.getElementById('url_' + filename);
            const status = document.getElementById('status_' + filename);
            const url = input.value.trim();
            
            if (!url) {
                status.textContent = '请输入URL';
                status.className = 'status';
                status.style.display = 'block';
                return;
            }
            
            if (!url.startsWith('http')) {
                status.textContent = 'URL格式不正确';
                status.className = 'status';
                status.style.display = 'block';
                return;
            }
            
            savedUrls[filename] = url;
            status.textContent = '✅ 已保存';
            status.className = 'status success';
            status.style.display = 'block';
            
            updateProgress();
        }
        
        function updateProgress() {
            const count = Object.keys(savedUrls).length;
            document.getElementById('progress').textContent = count;
            document.getElementById('generateBtn').disabled = count === 0;
        }
        
        function generateScript() {
            const script = `#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# 自动生成的图片下载脚本

import requests
import os
from urllib.parse import urlparse

# 图片URL映射
IMAGE_URLS = ${JSON.stringify(savedUrls, null, 4)}

def download_image(filename, url):
    # 下载单张图片
    try:
        print(f"下载: {filename}...")
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        response = requests.get(url, headers=headers, timeout=30)
        response.raise_for_status()
        
        # 保存图片
        output_dir = 'static/menu/menulist'
        os.makedirs(output_dir, exist_ok=True)
        filepath = os.path.join(output_dir, filename)
        
        with open(filepath, 'wb') as f:
            f.write(response.content)
        
        size_kb = len(response.content) / 1024
        print(f"  ✅ 成功 ({size_kb:.1f} KB)")
        return True
    except Exception as e:
        print(f"  ❌ 失败: {e}")
        return False

def main():
    print("=" * 60)
    print("开始下载菜品图片")
    print("=" * 60)
    print()
    
    success = 0
    failed = 0
    
    for filename, url in IMAGE_URLS.items():
        if download_image(filename, url):
            success += 1
        else:
            failed += 1
    
    print()
    print("=" * 60)
    print(f"下载完成！成功: {success}, 失败: {failed}")
    print("=" * 60)

if __name__ == '__main__':
    main()
`;
            
            // 下载脚本
            const blob = new Blob([script], { type: 'text/plain;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'download_dish_images_final.py';
            a.click();
            URL.revokeObjectURL(url);
            
            alert('✅ 下载脚本已生成！\\n\\n请运行：python download_dish_images_final.py');
        }
        
        // 初始化
        const grid = document.getElementById('dishGrid');
        dishes.forEach(dish => {
            grid.appendChild(createDishCard(dish));
        });
    </script>
</body>
</html>""";
    
    return html

def main():
    """主函数"""
    print("=" * 60)
    print("菜品图片搜索与下载工具")
    print("=" * 60)
    print()
    
    # 生成HTML
    html = generate_preview_html()
    filename = 'dish_image_search.html'
    
    with open(filename, 'w', encoding='utf-8') as f:
        f.write(html)
    
    print(f"✅ 已生成搜索页面: {filename}")
    print()
    print("使用步骤：")
    print("1. 页面将自动在浏览器中打开")
    print("2. 点击搜索按钮在图片网站查找合适的图片")
    print("3. 找到满意的图片后，右键→复制图片地址")
    print("4. 粘贴URL并点击'保存URL'")
    print("5. 完成所有菜品后，点击'生成下载脚本'")
    print("6. 运行生成的 download_dish_images_final.py")
    print()
    
    # 在浏览器中打开
    filepath = os.path.abspath(filename)
    webbrowser.open('file://' + filepath)
    print("✅ 已在浏览器中打开搜索页面")

if __name__ == '__main__':
    main()
