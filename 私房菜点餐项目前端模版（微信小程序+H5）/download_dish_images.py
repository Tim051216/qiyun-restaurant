#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
菜品图片批量下载脚本
使用 Pixabay API 自动下载所有菜品图片
"""

import requests
import os
import time
import json
from pathlib import Path

# Pixabay API 配置
# 请访问 https://pixabay.com/api/docs/ 注册获取免费 API Key
PIXABAY_API_KEY = "YOUR_API_KEY_HERE"  # 请替换为你的 API Key
PIXABAY_API_URL = "https://pixabay.com/api/"

# 图片保存目录
OUTPUT_DIR = "./static/menu/menulist"

# 菜品列表（中文名 -> 英文搜索关键词 -> 文件名）
DISHES = [
    # 川味麻辣风
    {"name": "干煸芸豆", "search": "dry fried green beans chinese", "filename": "gbyd.jpg"},
    {"name": "麻婆豆腐", "search": "mapo tofu", "filename": "mpdf.jpg"},
    {"name": "鱼香肉丝", "search": "yuxiang shredded pork chinese", "filename": "yxrs.jpg"},
    {"name": "水煮鱼", "search": "boiled fish spicy chinese", "filename": "szyu.jpg"},
    {"name": "辣子鸡", "search": "spicy chicken sichuan", "filename": "lzj.jpg"},
    {"name": "毛血旺", "search": "spicy hot pot chinese", "filename": "mxw.jpg"},
    {"name": "口水鸡", "search": "saliva chicken sichuan", "filename": "ksj.jpg"},
    {"name": "夫妻肺片", "search": "sliced beef sichuan", "filename": "ffpp.jpg"},
    
    # 家常小炒
    {"name": "糖醋里脊", "search": "sweet and sour pork", "filename": "tclj.jpg"},
    {"name": "京酱肉丝", "search": "shredded pork beijing sauce", "filename": "jjrs.jpg"},
    {"name": "回锅肉", "search": "twice cooked pork", "filename": "hgr.jpg"},
    {"name": "宫保鸡丁", "search": "kung pao chicken", "filename": "gbjd.jpg"},
    {"name": "青椒肉丝", "search": "pork green pepper stir fry", "filename": "qjrs.jpg"},
    {"name": "木须肉", "search": "moo shu pork", "filename": "mxr.jpg"},
    {"name": "红烧肉", "search": "braised pork belly chinese", "filename": "hsr.jpg"},
    {"name": "小炒肉", "search": "stir fried pork chinese", "filename": "xcr.jpg"},
    {"name": "蚂蚁上树", "search": "ants climbing tree noodles", "filename": "myss.jpg"},
    
    # 时蔬素菜
    {"name": "酸辣土豆丝", "search": "shredded potato spicy", "filename": "sltds.jpg"},
    {"name": "西红柿炒鸡蛋", "search": "tomato scrambled eggs chinese", "filename": "xhscjd.jpg"},
    {"name": "红烧茄子", "search": "braised eggplant chinese", "filename": "hsqz.jpg"},
    {"name": "地三鲜", "search": "eggplant potato pepper chinese", "filename": "dsx.jpg"},
    {"name": "清炒时蔬", "search": "stir fried vegetables chinese", "filename": "qcss.jpg"},
    {"name": "干煸豆角", "search": "dry fried green beans", "filename": "gbdj.jpg"},
    {"name": "手撕包菜", "search": "hand torn cabbage chinese", "filename": "ssbc.jpg"},
    {"name": "蒜蓉西兰花", "search": "garlic broccoli", "filename": "srxlh.jpg"},
    {"name": "油麦菜", "search": "chinese lettuce stir fried", "filename": "ymc.jpg"},
    
    # 汤品主食
    {"name": "米饭", "search": "white rice bowl", "filename": "mf.jpg"},
    {"name": "馒头", "search": "chinese steamed bun", "filename": "mt.jpg"},
    {"name": "酸辣汤", "search": "hot and sour soup", "filename": "slt.jpg"},
    {"name": "疙瘩汤", "search": "chinese noodle soup", "filename": "gdt.jpg"},
    {"name": "紫菜蛋花汤", "search": "seaweed egg drop soup", "filename": "zcdht.jpg"},
    {"name": "西红柿鸡蛋汤", "search": "tomato egg soup chinese", "filename": "xhsjdt.jpg"},
    {"name": "炒饭", "search": "chinese fried rice", "filename": "cf.jpg"},
    {"name": "炒面", "search": "chinese fried noodles", "filename": "cm.jpg"},
    {"name": "老鸭汤", "search": "duck soup chinese", "filename": "lyt.jpg"},
    {"name": "排骨汤", "search": "pork rib soup chinese", "filename": "pgt.jpg"},
    
    # 夜宵烧烤
    {"name": "烤羊肉串", "search": "lamb skewers grilled", "filename": "kyrc.jpg"},
    {"name": "烤鸡翅", "search": "grilled chicken wings", "filename": "kjc.jpg"},
    {"name": "烤鱿鱼", "search": "grilled squid", "filename": "kyy.jpg"},
    {"name": "烤韭菜", "search": "grilled leeks chinese", "filename": "kjc2.jpg"},
    {"name": "烤茄子", "search": "grilled eggplant", "filename": "kqz.jpg"},
    {"name": "烤玉米", "search": "grilled corn", "filename": "kym.jpg"},
    {"name": "烤生蚝", "search": "grilled oysters", "filename": "ksh.jpg"},
    {"name": "烤牛肉串", "search": "beef skewers grilled", "filename": "knrc.jpg"},
    {"name": "烤鸡心", "search": "grilled chicken hearts", "filename": "kjx.jpg"},
    {"name": "烤金针菇", "search": "grilled enoki mushrooms", "filename": "kjzg.jpg"},
    {"name": "烤土豆片", "search": "grilled potato slices", "filename": "ktdp.jpg"},
    {"name": "烤馒头片", "search": "grilled bread chinese", "filename": "kmtp.jpg"},
    
    # 特色龙虾
    {"name": "麻辣小龙虾", "search": "spicy crayfish chinese", "filename": "mlxlx.jpg"},
    {"name": "蒜蓉小龙虾", "search": "garlic crayfish", "filename": "srxlx.jpg"},
    {"name": "十三香龙虾", "search": "spiced crayfish chinese", "filename": "ssxlx.jpg"},
    {"name": "油焖大虾", "search": "braised prawns chinese", "filename": "ymdx.jpg"},
    {"name": "清蒸龙虾", "search": "steamed lobster", "filename": "qzlx.jpg"},
    {"name": "芝士焗龙虾", "search": "cheese baked lobster", "filename": "zsjlx.jpg"},
    {"name": "冰镇小龙虾", "search": "chilled crayfish", "filename": "bzxlx.jpg"},
    {"name": "椒盐龙虾", "search": "salt pepper lobster", "filename": "jylx.jpg"},
    {"name": "避风塘龙虾", "search": "typhoon shelter lobster", "filename": "bftlx.jpg"},
    
    # 饮品酒水
    {"name": "可口可乐", "search": "coca cola glass", "filename": "kkl.jpg"},
    {"name": "雪碧", "search": "sprite soda", "filename": "xb.jpg"},
    {"name": "橙汁", "search": "orange juice glass", "filename": "cz.jpg"},
    {"name": "西瓜汁", "search": "watermelon juice", "filename": "xgz.jpg"},
    {"name": "柠檬水", "search": "lemon water glass", "filename": "nms.jpg"},
    {"name": "酸梅汤", "search": "plum juice chinese", "filename": "smt.jpg"},
    {"name": "奶茶", "search": "milk tea bubble", "filename": "nc.jpg"},
    {"name": "豆浆", "search": "soy milk chinese", "filename": "dj.jpg"},
    {"name": "啤酒", "search": "beer glass", "filename": "pj.jpg"},
    {"name": "红酒", "search": "red wine glass", "filename": "hj.jpg"},
    {"name": "白酒", "search": "chinese liquor baijiu", "filename": "bj.jpg"},
    {"name": "矿泉水", "search": "mineral water bottle", "filename": "kqs.jpg"},
]


def download_image(url, filepath):
    """下载图片到指定路径"""
    try:
        response = requests.get(url, timeout=30)
        response.raise_for_status()
        
        with open(filepath, 'wb') as f:
            f.write(response.content)
        
        return True
    except Exception as e:
        print(f"  ❌ 下载失败: {e}")
        return False


def search_pixabay(query, per_page=5):
    """在 Pixabay 搜索图片"""
    params = {
        'key': PIXABAY_API_KEY,
        'q': query,
        'image_type': 'photo',
        'orientation': 'horizontal',
        'category': 'food',
        'per_page': per_page,
        'safesearch': 'true',
        'order': 'popular'
    }
    
    try:
        response = requests.get(PIXABAY_API_URL, params=params, timeout=10)
        response.raise_for_status()
        data = response.json()
        
        if data['totalHits'] > 0:
            return data['hits']
        else:
            return []
    except Exception as e:
        print(f"  ❌ 搜索失败: {e}")
        return []


def download_dish_images():
    """批量下载所有菜品图片"""
    
    # 检查 API Key
    if PIXABAY_API_KEY == "YOUR_API_KEY_HERE":
        print("❌ 错误：请先设置 Pixabay API Key")
        print("📝 获取方法：")
        print("   1. 访问 https://pixabay.com/")
        print("   2. 注册/登录账号")
        print("   3. 访问 https://pixabay.com/api/docs/ 获取 API Key")
        print("   4. 将 API Key 填入脚本的 PIXABAY_API_KEY 变量")
        return
    
    # 创建输出目录
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    print(f"🚀 开始下载 {len(DISHES)} 道菜品图片...")
    print(f"📁 保存目录: {OUTPUT_DIR}\n")
    
    success_count = 0
    failed_dishes = []
    
    for i, dish in enumerate(DISHES, 1):
        name = dish['name']
        search_query = dish['search']
        filename = dish['filename']
        filepath = os.path.join(OUTPUT_DIR, filename)
        
        print(f"[{i}/{len(DISHES)}] 正在处理: {name}")
        print(f"  🔍 搜索关键词: {search_query}")
        
        # 检查文件是否已存在
        if os.path.exists(filepath):
            print(f"  ⏭️  文件已存在，跳过")
            success_count += 1
            continue
        
        # 搜索图片
        results = search_pixabay(search_query)
        
        if not results:
            print(f"  ⚠️  未找到图片")
            failed_dishes.append(dish)
            time.sleep(1)
            continue
        
        # 下载第一张图片（中等尺寸）
        image_url = results[0].get('webformatURL') or results[0].get('largeImageURL')
        
        if image_url:
            print(f"  📥 下载中...")
            if download_image(image_url, filepath):
                print(f"  ✅ 下载成功: {filename}")
                success_count += 1
            else:
                failed_dishes.append(dish)
        else:
            print(f"  ❌ 无法获取图片URL")
            failed_dishes.append(dish)
        
        # API 限流：每秒最多 5 次请求（免费版限制）
        time.sleep(0.5)
        print()
    
    # 输出统计信息
    print("\n" + "="*60)
    print(f"✨ 下载完成！")
    print(f"✅ 成功: {success_count}/{len(DISHES)}")
    print(f"❌ 失败: {len(failed_dishes)}/{len(DISHES)}")
    
    if failed_dishes:
        print("\n⚠️  以下菜品下载失败，请手动下载：")
        for dish in failed_dishes:
            print(f"   - {dish['name']} (搜索: {dish['search']})")
        
        # 保存失败列表
        failed_file = "failed_dishes.json"
        with open(failed_file, 'w', encoding='utf-8') as f:
            json.dump(failed_dishes, f, ensure_ascii=False, indent=2)
        print(f"\n📝 失败列表已保存到: {failed_file}")
    
    print("\n💡 提示：")
    print("   1. 下载的图片可能需要裁剪和压缩")
    print("   2. 建议使用 TinyPNG (https://tinypng.com/) 压缩图片")
    print("   3. 检查图片质量，必要时手动替换")


if __name__ == "__main__":
    download_dish_images()
