#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
菜品图片批量下载脚本 - 完全免费版
使用 Foodish API 和备用图片源，无需 API Key
"""

import requests
import os
import time
import random

# 图片保存目录
OUTPUT_DIR = "./static/menu/menulist"

# 备用图片源（完全免费，无需 API Key）
BACKUP_SOURCES = [
    "https://foodish-api.com/api/",  # 随机美食图片
    "https://source.unsplash.com/800x800/?food,{keyword}",  # Unsplash 备用
    "https://loremflickr.com/800/800/food,{keyword}",  # LoremFlickr
]

# 菜品列表（使用通用关键词）
DISHES = [
    # 川味麻辣风
    {"name": "干煸芸豆", "keywords": ["beans", "vegetables", "chinese"], "filename": "gbyd.jpg"},
    {"name": "麻婆豆腐", "keywords": ["tofu", "spicy", "chinese"], "filename": "mpdf.jpg"},
    {"name": "鱼香肉丝", "keywords": ["pork", "stirfry", "chinese"], "filename": "yxrs.jpg"},
    {"name": "水煮鱼", "keywords": ["fish", "soup", "spicy"], "filename": "szyu.jpg"},
    {"name": "辣子鸡", "keywords": ["chicken", "spicy", "chinese"], "filename": "lzj.jpg"},
    {"name": "毛血旺", "keywords": ["hotpot", "spicy", "chinese"], "filename": "mxw.jpg"},
    {"name": "口水鸡", "keywords": ["chicken", "cold", "chinese"], "filename": "ksj.jpg"},
    {"name": "夫妻肺片", "keywords": ["beef", "sliced", "chinese"], "filename": "ffpp.jpg"},
    
    # 家常小炒
    {"name": "糖醋里脊", "keywords": ["pork", "sweet", "chinese"], "filename": "tclj.jpg"},
    {"name": "京酱肉丝", "keywords": ["pork", "shredded", "chinese"], "filename": "jjrs.jpg"},
    {"name": "回锅肉", "keywords": ["pork", "belly", "chinese"], "filename": "hgr.jpg"},
    {"name": "宫保鸡丁", "keywords": ["chicken", "kungpao", "chinese"], "filename": "gbjd.jpg"},
    {"name": "青椒肉丝", "keywords": ["pork", "pepper", "stirfry"], "filename": "qjrs.jpg"},
    {"name": "木须肉", "keywords": ["pork", "egg", "chinese"], "filename": "mxr.jpg"},
    {"name": "红烧肉", "keywords": ["pork", "braised", "chinese"], "filename": "hsr.jpg"},
    {"name": "小炒肉", "keywords": ["pork", "stirfry", "chinese"], "filename": "xcr.jpg"},
    {"name": "蚂蚁上树", "keywords": ["noodles", "glass", "chinese"], "filename": "myss.jpg"},
    
    # 时蔬素菜
    {"name": "酸辣土豆丝", "keywords": ["potato", "shredded", "spicy"], "filename": "sltds.jpg"},
    {"name": "西红柿炒鸡蛋", "keywords": ["tomato", "egg", "scrambled"], "filename": "xhscjd.jpg"},
    {"name": "红烧茄子", "keywords": ["eggplant", "braised", "chinese"], "filename": "hsqz.jpg"},
    {"name": "地三鲜", "keywords": ["eggplant", "potato", "pepper"], "filename": "dsx.jpg"},
    {"name": "清炒时蔬", "keywords": ["vegetables", "stirfry", "green"], "filename": "qcss.jpg"},
    {"name": "干煸豆角", "keywords": ["beans", "green", "stirfry"], "filename": "gbdj.jpg"},
    {"name": "手撕包菜", "keywords": ["cabbage", "stirfry", "chinese"], "filename": "ssbc.jpg"},
    {"name": "蒜蓉西兰花", "keywords": ["broccoli", "garlic", "green"], "filename": "srxlh.jpg"},
    {"name": "油麦菜", "keywords": ["lettuce", "green", "vegetables"], "filename": "ymc.jpg"},
    
    # 汤品主食
    {"name": "米饭", "keywords": ["rice", "white", "bowl"], "filename": "mf.jpg"},
    {"name": "馒头", "keywords": ["bun", "steamed", "bread"], "filename": "mt.jpg"},
    {"name": "酸辣汤", "keywords": ["soup", "spicy", "sour"], "filename": "slt.jpg"},
    {"name": "疙瘩汤", "keywords": ["soup", "noodle", "chinese"], "filename": "gdt.jpg"},
    {"name": "紫菜蛋花汤", "keywords": ["soup", "seaweed", "egg"], "filename": "zcdht.jpg"},
    {"name": "西红柿鸡蛋汤", "keywords": ["soup", "tomato", "egg"], "filename": "xhsjdt.jpg"},
    {"name": "炒饭", "keywords": ["rice", "fried", "chinese"], "filename": "cf.jpg"},
    {"name": "炒面", "keywords": ["noodles", "fried", "chinese"], "filename": "cm.jpg"},
    {"name": "老鸭汤", "keywords": ["soup", "duck", "chinese"], "filename": "lyt.jpg"},
    {"name": "排骨汤", "keywords": ["soup", "ribs", "pork"], "filename": "pgt.jpg"},
    
    # 夜宵烧烤
    {"name": "烤羊肉串", "keywords": ["lamb", "skewers", "grilled"], "filename": "kyrc.jpg"},
    {"name": "烤鸡翅", "keywords": ["chicken", "wings", "grilled"], "filename": "kjc.jpg"},
    {"name": "烤鱿鱼", "keywords": ["squid", "grilled", "seafood"], "filename": "kyy.jpg"},
    {"name": "烤韭菜", "keywords": ["leeks", "grilled", "vegetables"], "filename": "kjc2.jpg"},
    {"name": "烤茄子", "keywords": ["eggplant", "grilled", "vegetables"], "filename": "kqz.jpg"},
    {"name": "烤玉米", "keywords": ["corn", "grilled", "vegetables"], "filename": "kym.jpg"},
    {"name": "烤生蚝", "keywords": ["oysters", "grilled", "seafood"], "filename": "ksh.jpg"},
    {"name": "烤牛肉串", "keywords": ["beef", "skewers", "grilled"], "filename": "knrc.jpg"},
    {"name": "烤鸡心", "keywords": ["chicken", "grilled", "meat"], "filename": "kjx.jpg"},
    {"name": "烤金针菇", "keywords": ["mushrooms", "grilled", "vegetables"], "filename": "kjzg.jpg"},
    {"name": "烤土豆片", "keywords": ["potato", "grilled", "vegetables"], "filename": "ktdp.jpg"},
    {"name": "烤馒头片", "keywords": ["bread", "grilled", "toast"], "filename": "kmtp.jpg"},
    
    # 特色龙虾
    {"name": "麻辣小龙虾", "keywords": ["crayfish", "spicy", "seafood"], "filename": "mlxlx.jpg"},
    {"name": "蒜蓉小龙虾", "keywords": ["crayfish", "garlic", "seafood"], "filename": "srxlx.jpg"},
    {"name": "十三香龙虾", "keywords": ["crayfish", "spiced", "seafood"], "filename": "ssxlx.jpg"},
    {"name": "油焖大虾", "keywords": ["prawns", "braised", "seafood"], "filename": "ymdx.jpg"},
    {"name": "清蒸龙虾", "keywords": ["lobster", "steamed", "seafood"], "filename": "qzlx.jpg"},
    {"name": "芝士焗龙虾", "keywords": ["lobster", "cheese", "baked"], "filename": "zsjlx.jpg"},
    {"name": "冰镇小龙虾", "keywords": ["crayfish", "chilled", "seafood"], "filename": "bzxlx.jpg"},
    {"name": "椒盐龙虾", "keywords": ["lobster", "salt", "pepper"], "filename": "jylx.jpg"},
    {"name": "避风塘龙虾", "keywords": ["lobster", "fried", "seafood"], "filename": "bftlx.jpg"},
    
    # 饮品酒水
    {"name": "可口可乐", "keywords": ["cola", "drink", "soda"], "filename": "kkl.jpg"},
    {"name": "雪碧", "keywords": ["sprite", "drink", "soda"], "filename": "xb.jpg"},
    {"name": "橙汁", "keywords": ["orange", "juice", "drink"], "filename": "cz.jpg"},
    {"name": "西瓜汁", "keywords": ["watermelon", "juice", "drink"], "filename": "xgz.jpg"},
    {"name": "柠檬水", "keywords": ["lemon", "water", "drink"], "filename": "nms.jpg"},
    {"name": "酸梅汤", "keywords": ["plum", "drink", "chinese"], "filename": "smt.jpg"},
    {"name": "奶茶", "keywords": ["tea", "milk", "bubble"], "filename": "nc.jpg"},
    {"name": "豆浆", "keywords": ["soy", "milk", "drink"], "filename": "dj.jpg"},
    {"name": "啤酒", "keywords": ["beer", "drink", "alcohol"], "filename": "pj.jpg"},
    {"name": "红酒", "keywords": ["wine", "red", "drink"], "filename": "hj.jpg"},
    {"name": "白酒", "keywords": ["liquor", "chinese", "alcohol"], "filename": "bj.jpg"},
    {"name": "矿泉水", "keywords": ["water", "bottle", "drink"], "filename": "kqs.jpg"},
]


def download_from_foodish():
    """从 Foodish API 获取随机美食图片"""
    try:
        response = requests.get("https://foodish-api.com/api/", timeout=10)
        if response.status_code == 200:
            data = response.json()
            return data.get('image')
    except:
        pass
    return None


def download_from_loremflickr(keywords):
    """从 LoremFlickr 下载图片"""
    keyword = ",".join(keywords[:2])  # 使用前两个关键词
    url = f"https://loremflickr.com/800/800/{keyword}"
    return url


def download_image(url, filepath, max_retries=3):
    """下载图片到指定路径"""
    for attempt in range(max_retries):
        try:
            response = requests.get(url, timeout=30, allow_redirects=True)
            if response.status_code == 200:
                with open(filepath, 'wb') as f:
                    f.write(response.content)
                
                file_size = os.path.getsize(filepath) / 1024
                
                # 检查文件是否有效（大于 10KB）
                if file_size > 10:
                    return True, file_size
                else:
                    os.remove(filepath)
                    
        except Exception as e:
            if attempt == max_retries - 1:
                print(f"  ❌ 下载失败: {e}")
    
    return False, 0


def download_all_images():
    """批量下载所有菜品图片"""
    
    # 创建输出目录
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    print(f"🚀 开始下载 {len(DISHES)} 道菜品图片...")
    print(f"📁 保存目录: {OUTPUT_DIR}")
    print(f"🌐 图片来源: LoremFlickr (完全免费，无需 API Key)\n")
    
    success_count = 0
    failed_dishes = []
    
    for i, dish in enumerate(DISHES, 1):
        name = dish['name']
        keywords = dish['keywords']
        filename = dish['filename']
        filepath = os.path.join(OUTPUT_DIR, filename)
        
        print(f"[{i}/{len(DISHES)}] {name}")
        print(f"  🔍 关键词: {', '.join(keywords)}")
        
        # 检查文件是否已存在
        if os.path.exists(filepath):
            file_size = os.path.getsize(filepath) / 1024
            print(f"  ⏭️  文件已存在 ({file_size:.1f} KB)，跳过\n")
            success_count += 1
            continue
        
        # 尝试从 LoremFlickr 下载
        print(f"  📥 正在下载...")
        image_url = download_from_loremflickr(keywords)
        
        success, file_size = download_image(image_url, filepath)
        
        if success:
            print(f"  ✅ 下载成功 ({file_size:.1f} KB)\n")
            success_count += 1
        else:
            # 如果失败，尝试使用 Foodish API
            print(f"  🔄 尝试备用源...")
            foodish_url = download_from_foodish()
            if foodish_url:
                success, file_size = download_image(foodish_url, filepath)
                if success:
                    print(f"  ✅ 下载成功 ({file_size:.1f} KB)\n")
                    success_count += 1
                else:
                    failed_dishes.append(dish)
                    print(f"  ❌ 下载失败\n")
            else:
                failed_dishes.append(dish)
                print(f"  ❌ 下载失败\n")
        
        # 避免请求过快
        time.sleep(1)
    
    # 输出统计信息
    print("\n" + "="*60)
    print(f"✨ 下载完成！")
    print(f"✅ 成功: {success_count}/{len(DISHES)}")
    print(f"❌ 失败: {len(failed_dishes)}/{len(DISHES)}")
    
    if failed_dishes:
        print("\n⚠️  以下菜品下载失败，请手动下载：")
        for dish in failed_dishes:
            print(f"   - {dish['name']}")
    
    print("\n💡 后续步骤：")
    print("   1. 检查下载的图片质量")
    print("   2. 运行压缩脚本: python compress_images.py")
    print("   3. 手动替换不合适的图片")
    print("\n📝 注意：")
    print("   - LoremFlickr 提供的是随机图片")
    print("   - 图片可能不完全匹配菜品")
    print("   - 建议手动替换重要菜品的图片")


if __name__ == "__main__":
    try:
        download_all_images()
    except KeyboardInterrupt:
        print("\n\n⚠️  下载已中断")
    except Exception as e:
        print(f"\n\n❌ 发生错误: {e}")
        import traceback
        traceback.print_exc()
