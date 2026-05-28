#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
菜品图片批量下载脚本（简化版）
无需 API Key，直接从 Unsplash 下载
"""

import requests
import os
import time
from pathlib import Path

# 图片保存目录
OUTPUT_DIR = "./static/menu/menulist"

# 菜品列表
DISHES = [
    # 川味麻辣风
    {"name": "干煸芸豆", "search": "green-beans-stir-fry", "filename": "gbyd.jpg"},
    {"name": "麻婆豆腐", "search": "mapo-tofu", "filename": "mpdf.jpg"},
    {"name": "鱼香肉丝", "search": "chinese-pork-dish", "filename": "yxrs.jpg"},
    {"name": "水煮鱼", "search": "spicy-fish-chinese", "filename": "szyu.jpg"},
    {"name": "辣子鸡", "search": "spicy-chicken", "filename": "lzj.jpg"},
    {"name": "毛血旺", "search": "hot-pot-chinese", "filename": "mxw.jpg"},
    {"name": "口水鸡", "search": "chinese-chicken", "filename": "ksj.jpg"},
    {"name": "夫妻肺片", "search": "beef-slices", "filename": "ffpp.jpg"},
    
    # 家常小炒
    {"name": "糖醋里脊", "search": "sweet-sour-pork", "filename": "tclj.jpg"},
    {"name": "京酱肉丝", "search": "shredded-pork", "filename": "jjrs.jpg"},
    {"name": "回锅肉", "search": "twice-cooked-pork", "filename": "hgr.jpg"},
    {"name": "宫保鸡丁", "search": "kung-pao-chicken", "filename": "gbjd.jpg"},
    {"name": "青椒肉丝", "search": "pork-pepper", "filename": "qjrs.jpg"},
    {"name": "木须肉", "search": "moo-shu-pork", "filename": "mxr.jpg"},
    {"name": "红烧肉", "search": "braised-pork-belly", "filename": "hsr.jpg"},
    {"name": "小炒肉", "search": "stir-fried-pork", "filename": "xcr.jpg"},
    {"name": "蚂蚁上树", "search": "glass-noodles", "filename": "myss.jpg"},
    
    # 时蔬素菜
    {"name": "酸辣土豆丝", "search": "potato-strips", "filename": "sltds.jpg"},
    {"name": "西红柿炒鸡蛋", "search": "tomato-eggs", "filename": "xhscjd.jpg"},
    {"name": "红烧茄子", "search": "braised-eggplant", "filename": "hsqz.jpg"},
    {"name": "地三鲜", "search": "eggplant-potato", "filename": "dsx.jpg"},
    {"name": "清炒时蔬", "search": "stir-fried-vegetables", "filename": "qcss.jpg"},
    {"name": "干煸豆角", "search": "green-beans", "filename": "gbdj.jpg"},
    {"name": "手撕包菜", "search": "cabbage-stir-fry", "filename": "ssbc.jpg"},
    {"name": "蒜蓉西兰花", "search": "garlic-broccoli", "filename": "srxlh.jpg"},
    {"name": "油麦菜", "search": "chinese-lettuce", "filename": "ymc.jpg"},
    
    # 汤品主食
    {"name": "米饭", "search": "white-rice", "filename": "mf.jpg"},
    {"name": "馒头", "search": "steamed-buns", "filename": "mt.jpg"},
    {"name": "酸辣汤", "search": "hot-sour-soup", "filename": "slt.jpg"},
    {"name": "疙瘩汤", "search": "noodle-soup", "filename": "gdt.jpg"},
    {"name": "紫菜蛋花汤", "search": "seaweed-soup", "filename": "zcdht.jpg"},
    {"name": "西红柿鸡蛋汤", "search": "tomato-egg-soup", "filename": "xhsjdt.jpg"},
    {"name": "炒饭", "search": "fried-rice", "filename": "cf.jpg"},
    {"name": "炒面", "search": "fried-noodles", "filename": "cm.jpg"},
    {"name": "老鸭汤", "search": "duck-soup", "filename": "lyt.jpg"},
    {"name": "排骨汤", "search": "pork-rib-soup", "filename": "pgt.jpg"},
    
    # 夜宵烧烤
    {"name": "烤羊肉串", "search": "lamb-skewers", "filename": "kyrc.jpg"},
    {"name": "烤鸡翅", "search": "grilled-chicken-wings", "filename": "kjc.jpg"},
    {"name": "烤鱿鱼", "search": "grilled-squid", "filename": "kyy.jpg"},
    {"name": "烤韭菜", "search": "grilled-leeks", "filename": "kjc2.jpg"},
    {"name": "烤茄子", "search": "grilled-eggplant", "filename": "kqz.jpg"},
    {"name": "烤玉米", "search": "grilled-corn", "filename": "kym.jpg"},
    {"name": "烤生蚝", "search": "grilled-oysters", "filename": "ksh.jpg"},
    {"name": "烤牛肉串", "search": "beef-skewers", "filename": "knrc.jpg"},
    {"name": "烤鸡心", "search": "grilled-chicken", "filename": "kjx.jpg"},
    {"name": "烤金针菇", "search": "grilled-mushrooms", "filename": "kjzg.jpg"},
    {"name": "烤土豆片", "search": "grilled-potatoes", "filename": "ktdp.jpg"},
    {"name": "烤馒头片", "search": "grilled-bread", "filename": "kmtp.jpg"},
    
    # 特色龙虾
    {"name": "麻辣小龙虾", "search": "spicy-crayfish", "filename": "mlxlx.jpg"},
    {"name": "蒜蓉小龙虾", "search": "garlic-crayfish", "filename": "srxlx.jpg"},
    {"name": "十三香龙虾", "search": "crayfish", "filename": "ssxlx.jpg"},
    {"name": "油焖大虾", "search": "braised-prawns", "filename": "ymdx.jpg"},
    {"name": "清蒸龙虾", "search": "steamed-lobster", "filename": "qzlx.jpg"},
    {"name": "芝士焗龙虾", "search": "cheese-lobster", "filename": "zsjlx.jpg"},
    {"name": "冰镇小龙虾", "search": "chilled-seafood", "filename": "bzxlx.jpg"},
    {"name": "椒盐龙虾", "search": "salt-pepper-lobster", "filename": "jylx.jpg"},
    {"name": "避风塘龙虾", "search": "lobster-dish", "filename": "bftlx.jpg"},
    
    # 饮品酒水
    {"name": "可口可乐", "search": "coca-cola", "filename": "kkl.jpg"},
    {"name": "雪碧", "search": "sprite", "filename": "xb.jpg"},
    {"name": "橙汁", "search": "orange-juice", "filename": "cz.jpg"},
    {"name": "西瓜汁", "search": "watermelon-juice", "filename": "xgz.jpg"},
    {"name": "柠檬水", "search": "lemon-water", "filename": "nms.jpg"},
    {"name": "酸梅汤", "search": "plum-juice", "filename": "smt.jpg"},
    {"name": "奶茶", "search": "bubble-tea", "filename": "nc.jpg"},
    {"name": "豆浆", "search": "soy-milk", "filename": "dj.jpg"},
    {"name": "啤酒", "search": "beer", "filename": "pj.jpg"},
    {"name": "红酒", "search": "red-wine", "filename": "hj.jpg"},
    {"name": "白酒", "search": "chinese-liquor", "filename": "bj.jpg"},
    {"name": "矿泉水", "search": "water-bottle", "filename": "kqs.jpg"},
]


def download_from_unsplash(search_term, filepath, width=800, height=800):
    """从 Unsplash 下载图片（使用 Source API）"""
    # Unsplash Source API - 无需 API Key
    url = f"https://source.unsplash.com/{width}x{height}/?{search_term}"
    
    try:
        print(f"  📥 正在下载...")
        response = requests.get(url, timeout=30, allow_redirects=True)
        response.raise_for_status()
        
        with open(filepath, 'wb') as f:
            f.write(response.content)
        
        # 检查文件大小
        file_size = os.path.getsize(filepath) / 1024  # KB
        print(f"  ✅ 下载成功: {filepath} ({file_size:.1f} KB)")
        return True
        
    except Exception as e:
        print(f"  ❌ 下载失败: {e}")
        if os.path.exists(filepath):
            os.remove(filepath)
        return False


def download_all_images():
    """批量下载所有菜品图片"""
    
    # 创建输出目录
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    print(f"🚀 开始下载 {len(DISHES)} 道菜品图片...")
    print(f"📁 保存目录: {OUTPUT_DIR}")
    print(f"🌐 图片来源: Unsplash (免费高质量图片)\n")
    
    success_count = 0
    failed_dishes = []
    
    for i, dish in enumerate(DISHES, 1):
        name = dish['name']
        search_term = dish['search']
        filename = dish['filename']
        filepath = os.path.join(OUTPUT_DIR, filename)
        
        print(f"[{i}/{len(DISHES)}] {name}")
        print(f"  🔍 搜索: {search_term}")
        
        # 检查文件是否已存在
        if os.path.exists(filepath):
            file_size = os.path.getsize(filepath) / 1024
            print(f"  ⏭️  文件已存在 ({file_size:.1f} KB)，跳过")
            success_count += 1
            print()
            continue
        
        # 下载图片
        if download_from_unsplash(search_term, filepath):
            success_count += 1
        else:
            failed_dishes.append(dish)
        
        print()
        
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
            print(f"   - {dish['name']} (搜索: {dish['search']})")
    
    print("\n💡 后续步骤：")
    print("   1. 检查下载的图片质量")
    print("   2. 使用 TinyPNG (https://tinypng.com/) 压缩图片")
    print("   3. 必要时手动替换不合适的图片")
    print("   4. 更新 menu.js 中的图片路径")


if __name__ == "__main__":
    try:
        download_all_images()
    except KeyboardInterrupt:
        print("\n\n⚠️  下载已中断")
    except Exception as e:
        print(f"\n\n❌ 发生错误: {e}")
