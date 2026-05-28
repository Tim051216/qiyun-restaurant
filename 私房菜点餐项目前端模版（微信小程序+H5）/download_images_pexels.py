#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
菜品图片批量下载脚本 - Pexels 版本
使用 Pexels API 下载免费高质量图片
"""

import requests
import os
import time
import json

# Pexels API 配置
# 免费 API Key，每小时 200 次请求
PEXELS_API_KEY = "563492ad6f91700001000001c7e2b2c8e8f04e2c9f3e8c5e5e5e5e5e"  # 示例 Key，请替换
PEXELS_API_URL = "https://api.pexels.com/v1/search"

# 图片保存目录
OUTPUT_DIR = "./static/menu/menulist"

# 菜品列表（简化版 - 使用通用关键词）
DISHES = [
    # 川味麻辣风
    {"name": "干煸芸豆", "search": "green beans dish", "filename": "gbyd.jpg"},
    {"name": "麻婆豆腐", "search": "tofu dish spicy", "filename": "mpdf.jpg"},
    {"name": "鱼香肉丝", "search": "chinese pork stir fry", "filename": "yxrs.jpg"},
    {"name": "水煮鱼", "search": "fish soup spicy", "filename": "szyu.jpg"},
    {"name": "辣子鸡", "search": "spicy chicken chinese", "filename": "lzj.jpg"},
    {"name": "毛血旺", "search": "hot pot chinese", "filename": "mxw.jpg"},
    {"name": "口水鸡", "search": "chicken dish chinese", "filename": "ksj.jpg"},
    {"name": "夫妻肺片", "search": "beef slices chinese", "filename": "ffpp.jpg"},
    
    # 家常小炒
    {"name": "糖醋里脊", "search": "sweet sour pork", "filename": "tclj.jpg"},
    {"name": "京酱肉丝", "search": "shredded pork", "filename": "jjrs.jpg"},
    {"name": "回锅肉", "search": "pork belly dish", "filename": "hgr.jpg"},
    {"name": "宫保鸡丁", "search": "kung pao chicken", "filename": "gbjd.jpg"},
    {"name": "青椒肉丝", "search": "pork pepper stir fry", "filename": "qjrs.jpg"},
    {"name": "木须肉", "search": "pork egg dish", "filename": "mxr.jpg"},
    {"name": "红烧肉", "search": "braised pork", "filename": "hsr.jpg"},
    {"name": "小炒肉", "search": "stir fried pork", "filename": "xcr.jpg"},
    {"name": "蚂蚁上树", "search": "glass noodles", "filename": "myss.jpg"},
    
    # 时蔬素菜
    {"name": "酸辣土豆丝", "search": "potato dish", "filename": "sltds.jpg"},
    {"name": "西红柿炒鸡蛋", "search": "tomato scrambled eggs", "filename": "xhscjd.jpg"},
    {"name": "红烧茄子", "search": "eggplant dish", "filename": "hsqz.jpg"},
    {"name": "地三鲜", "search": "eggplant potato", "filename": "dsx.jpg"},
    {"name": "清炒时蔬", "search": "stir fried vegetables", "filename": "qcss.jpg"},
    {"name": "干煸豆角", "search": "green beans", "filename": "gbdj.jpg"},
    {"name": "手撕包菜", "search": "cabbage dish", "filename": "ssbc.jpg"},
    {"name": "蒜蓉西兰花", "search": "broccoli garlic", "filename": "srxlh.jpg"},
    {"name": "油麦菜", "search": "lettuce dish", "filename": "ymc.jpg"},
    
    # 汤品主食
    {"name": "米饭", "search": "white rice bowl", "filename": "mf.jpg"},
    {"name": "馒头", "search": "steamed bun", "filename": "mt.jpg"},
    {"name": "酸辣汤", "search": "soup bowl", "filename": "slt.jpg"},
    {"name": "疙瘩汤", "search": "noodle soup", "filename": "gdt.jpg"},
    {"name": "紫菜蛋花汤", "search": "seaweed soup", "filename": "zcdht.jpg"},
    {"name": "西红柿鸡蛋汤", "search": "tomato soup", "filename": "xhsjdt.jpg"},
    {"name": "炒饭", "search": "fried rice", "filename": "cf.jpg"},
    {"name": "炒面", "search": "fried noodles", "filename": "cm.jpg"},
    {"name": "老鸭汤", "search": "duck soup", "filename": "lyt.jpg"},
    {"name": "排骨汤", "search": "pork rib soup", "filename": "pgt.jpg"},
    
    # 夜宵烧烤
    {"name": "烤羊肉串", "search": "lamb skewers", "filename": "kyrc.jpg"},
    {"name": "烤鸡翅", "search": "chicken wings grilled", "filename": "kjc.jpg"},
    {"name": "烤鱿鱼", "search": "grilled squid", "filename": "kyy.jpg"},
    {"name": "烤韭菜", "search": "grilled vegetables", "filename": "kjc2.jpg"},
    {"name": "烤茄子", "search": "grilled eggplant", "filename": "kqz.jpg"},
    {"name": "烤玉米", "search": "grilled corn", "filename": "kym.jpg"},
    {"name": "烤生蚝", "search": "oysters grilled", "filename": "ksh.jpg"},
    {"name": "烤牛肉串", "search": "beef skewers", "filename": "knrc.jpg"},
    {"name": "烤鸡心", "search": "grilled meat", "filename": "kjx.jpg"},
    {"name": "烤金针菇", "search": "grilled mushrooms", "filename": "kjzg.jpg"},
    {"name": "烤土豆片", "search": "grilled potatoes", "filename": "ktdp.jpg"},
    {"name": "烤馒头片", "search": "grilled bread", "filename": "kmtp.jpg"},
    
    # 特色龙虾
    {"name": "麻辣小龙虾", "search": "crayfish spicy", "filename": "mlxlx.jpg"},
    {"name": "蒜蓉小龙虾", "search": "crayfish garlic", "filename": "srxlx.jpg"},
    {"name": "十三香龙虾", "search": "crayfish dish", "filename": "ssxlx.jpg"},
    {"name": "油焖大虾", "search": "prawns dish", "filename": "ymdx.jpg"},
    {"name": "清蒸龙虾", "search": "lobster steamed", "filename": "qzlx.jpg"},
    {"name": "芝士焗龙虾", "search": "lobster cheese", "filename": "zsjlx.jpg"},
    {"name": "冰镇小龙虾", "search": "seafood platter", "filename": "bzxlx.jpg"},
    {"name": "椒盐龙虾", "search": "lobster fried", "filename": "jylx.jpg"},
    {"name": "避风塘龙虾", "search": "lobster dish", "filename": "bftlx.jpg"},
    
    # 饮品酒水
    {"name": "可口可乐", "search": "coca cola glass", "filename": "kkl.jpg"},
    {"name": "雪碧", "search": "sprite soda", "filename": "xb.jpg"},
    {"name": "橙汁", "search": "orange juice", "filename": "cz.jpg"},
    {"name": "西瓜汁", "search": "watermelon juice", "filename": "xgz.jpg"},
    {"name": "柠檬水", "search": "lemon water", "filename": "nms.jpg"},
    {"name": "酸梅汤", "search": "plum drink", "filename": "smt.jpg"},
    {"name": "奶茶", "search": "bubble tea", "filename": "nc.jpg"},
    {"name": "豆浆", "search": "soy milk", "filename": "dj.jpg"},
    {"name": "啤酒", "search": "beer glass", "filename": "pj.jpg"},
    {"name": "红酒", "search": "red wine", "filename": "hj.jpg"},
    {"name": "白酒", "search": "liquor bottle", "filename": "bj.jpg"},
    {"name": "矿泉水", "search": "water bottle", "filename": "kqs.jpg"},
]


def search_pexels(query, per_page=5):
    """在 Pexels 搜索图片"""
    headers = {
        'Authorization': PEXELS_API_KEY
    }
    
    params = {
        'query': query,
        'per_page': per_page,
        'orientation': 'square'
    }
    
    try:
        response = requests.get(PEXELS_API_URL, headers=headers, params=params, timeout=10)
        response.raise_for_status()
        data = response.json()
        
        if data.get('photos'):
            return data['photos']
        else:
            return []
    except Exception as e:
        print(f"  ❌ 搜索失败: {e}")
        return []


def download_image(url, filepath):
    """下载图片"""
    try:
        response = requests.get(url, timeout=30)
        response.raise_for_status()
        
        with open(filepath, 'wb') as f:
            f.write(response.content)
        
        file_size = os.path.getsize(filepath) / 1024
        return True, file_size
    except Exception as e:
        print(f"  ❌ 下载失败: {e}")
        return False, 0


def download_all():
    """批量下载"""
    
    print("⚠️  注意：此脚本需要 Pexels API Key")
    print("📝 获取方法：")
    print("   1. 访问 https://www.pexels.com/api/")
    print("   2. 注册账号并获取免费 API Key")
    print("   3. 将 API Key 填入脚本的 PEXELS_API_KEY 变量\n")
    
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
        
        print(f"[{i}/{len(DISHES)}] {name}")
        print(f"  🔍 搜索: {search_query}")
        
        # 检查文件是否已存在
        if os.path.exists(filepath):
            file_size = os.path.getsize(filepath) / 1024
            print(f"  ⏭️  文件已存在 ({file_size:.1f} KB)，跳过\n")
            success_count += 1
            continue
        
        # 搜索图片
        results = search_pexels(search_query)
        
        if not results:
            print(f"  ⚠️  未找到图片\n")
            failed_dishes.append(dish)
            time.sleep(1)
            continue
        
        # 下载第一张图片（中等尺寸）
        image_url = results[0]['src']['medium']
        
        print(f"  📥 下载中...")
        success, file_size = download_image(image_url, filepath)
        
        if success:
            print(f"  ✅ 下载成功 ({file_size:.1f} KB)\n")
            success_count += 1
        else:
            failed_dishes.append(dish)
            print()
        
        # API 限流
        time.sleep(0.5)
    
    # 输出统计
    print("=" * 60)
    print(f"✨ 下载完成！")
    print(f"✅ 成功: {success_count}/{len(DISHES)}")
    print(f"❌ 失败: {len(failed_dishes)}/{len(DISHES)}")
    
    if failed_dishes:
        print("\n⚠️  以下菜品下载失败：")
        for dish in failed_dishes:
            print(f"   - {dish['name']}")


if __name__ == "__main__":
    download_all()
