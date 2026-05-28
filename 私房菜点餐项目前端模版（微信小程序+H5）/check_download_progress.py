#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
检查图片下载进度
"""

import os
from pathlib import Path

OUTPUT_DIR = "./static/menu/menulist"
TOTAL_DISHES = 69

def check_progress():
    """检查下载进度"""
    
    if not os.path.exists(OUTPUT_DIR):
        print(f"❌ 目录不存在: {OUTPUT_DIR}")
        return
    
    # 获取所有图片文件
    image_files = [f for f in os.listdir(OUTPUT_DIR) 
                   if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
    
    downloaded_count = len(image_files)
    progress = (downloaded_count / TOTAL_DISHES) * 100
    
    print("=" * 60)
    print("📊 图片下载进度")
    print("=" * 60)
    print(f"✅ 已下载: {downloaded_count}/{TOTAL_DISHES} ({progress:.1f}%)")
    print(f"⏳ 剩余: {TOTAL_DISHES - downloaded_count}")
    
    if downloaded_count > 0:
        # 计算总大小
        total_size = sum(os.path.getsize(os.path.join(OUTPUT_DIR, f)) 
                        for f in image_files) / 1024 / 1024
        avg_size = (total_size * 1024) / downloaded_count
        
        print(f"📦 总大小: {total_size:.2f} MB")
        print(f"📏 平均大小: {avg_size:.1f} KB/张")
    
    print("\n最近下载的图片:")
    # 按修改时间排序，显示最新的 5 张
    files_with_time = [(f, os.path.getmtime(os.path.join(OUTPUT_DIR, f))) 
                       for f in image_files]
    files_with_time.sort(key=lambda x: x[1], reverse=True)
    
    for i, (filename, _) in enumerate(files_with_time[:5], 1):
        file_size = os.path.getsize(os.path.join(OUTPUT_DIR, filename)) / 1024
        print(f"  {i}. {filename} ({file_size:.1f} KB)")
    
    print("\n" + "=" * 60)
    
    if downloaded_count == TOTAL_DISHES:
        print("🎉 所有图片下载完成！")
        print("\n下一步:")
        print("  1. 运行压缩脚本: python compress_images.py")
        print("  2. 检查图片质量")
    else:
        print(f"⏳ 下载进行中... 请等待")

if __name__ == "__main__":
    check_progress()
