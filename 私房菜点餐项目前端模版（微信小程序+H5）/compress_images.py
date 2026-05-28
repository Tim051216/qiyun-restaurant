#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
图片压缩脚本
批量压缩菜品图片，减小文件大小
"""

from PIL import Image
import os
from pathlib import Path

# 配置
INPUT_DIR = "./static/menu/menulist"
OUTPUT_DIR = "./static/menu/menulist"  # 可以设置为不同目录保留原图
QUALITY = 85  # JPEG 质量 (1-100)
MAX_WIDTH = 800  # 最大宽度
MAX_HEIGHT = 800  # 最大高度
CONVERT_TO_JPG = True  # 是否将 PNG 转换为 JPG


def get_file_size_kb(filepath):
    """获取文件大小（KB）"""
    return os.path.getsize(filepath) / 1024


def compress_image(input_path, output_path, quality=85, max_width=800, max_height=800):
    """
    压缩单张图片
    
    Args:
        input_path: 输入图片路径
        output_path: 输出图片路径
        quality: JPEG 质量 (1-100)
        max_width: 最大宽度
        max_height: 最大高度
    
    Returns:
        tuple: (是否成功, 原始大小KB, 压缩后大小KB)
    """
    try:
        # 获取原始文件大小
        original_size = get_file_size_kb(input_path)
        
        # 打开图片
        img = Image.open(input_path)
        
        # 保存原始尺寸
        original_width, original_height = img.size
        
        # 调整尺寸（保持宽高比）
        if img.width > max_width or img.height > max_height:
            img.thumbnail((max_width, max_height), Image.Resampling.LANCZOS)
            resized = True
        else:
            resized = False
        
        # 转换颜色模式
        if img.mode in ('RGBA', 'LA', 'P'):
            # 创建白色背景
            background = Image.new('RGB', img.size, (255, 255, 255))
            
            # 如果是调色板模式，先转换为 RGBA
            if img.mode == 'P':
                img = img.convert('RGBA')
            
            # 粘贴图片到背景（保留透明度）
            if img.mode in ('RGBA', 'LA'):
                background.paste(img, mask=img.split()[-1])
            else:
                background.paste(img)
            
            img = background
        elif img.mode != 'RGB':
            img = img.convert('RGB')
        
        # 确保输出目录存在
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        
        # 保存压缩后的图片
        if CONVERT_TO_JPG and not output_path.lower().endswith('.jpg'):
            output_path = os.path.splitext(output_path)[0] + '.jpg'
        
        img.save(output_path, 'JPEG', quality=quality, optimize=True)
        
        # 获取压缩后文件大小
        compressed_size = get_file_size_kb(output_path)
        
        return True, original_size, compressed_size, resized, original_width, original_height
        
    except Exception as e:
        return False, 0, 0, False, 0, 0


def compress_all_images():
    """批量压缩所有图片"""
    
    if not os.path.exists(INPUT_DIR):
        print(f"❌ 错误：目录不存在 {INPUT_DIR}")
        return
    
    # 获取所有图片文件
    image_extensions = ('.jpg', '.jpeg', '.png', '.webp', '.bmp')
    image_files = [f for f in os.listdir(INPUT_DIR) 
                   if f.lower().endswith(image_extensions)]
    
    if not image_files:
        print(f"⚠️  未找到图片文件在 {INPUT_DIR}")
        return
    
    print(f"🚀 开始压缩 {len(image_files)} 张图片...")
    print(f"📁 输入目录: {INPUT_DIR}")
    print(f"📁 输出目录: {OUTPUT_DIR}")
    print(f"⚙️  设置: 质量={QUALITY}, 最大尺寸={MAX_WIDTH}x{MAX_HEIGHT}\n")
    
    success_count = 0
    failed_count = 0
    total_original_size = 0
    total_compressed_size = 0
    
    for i, filename in enumerate(image_files, 1):
        input_path = os.path.join(INPUT_DIR, filename)
        output_filename = filename
        
        # 如果转换为 JPG，修改扩展名
        if CONVERT_TO_JPG and not filename.lower().endswith('.jpg'):
            output_filename = os.path.splitext(filename)[0] + '.jpg'
        
        output_path = os.path.join(OUTPUT_DIR, output_filename)
        
        print(f"[{i}/{len(image_files)}] {filename}")
        
        success, original_size, compressed_size, resized, orig_w, orig_h = compress_image(
            input_path, output_path, QUALITY, MAX_WIDTH, MAX_HEIGHT
        )
        
        if success:
            reduction = ((original_size - compressed_size) / original_size * 100) if original_size > 0 else 0
            
            print(f"  ✅ 压缩成功")
            print(f"     原始: {original_size:.1f} KB ({orig_w}x{orig_h})")
            print(f"     压缩: {compressed_size:.1f} KB")
            print(f"     减少: {reduction:.1f}%")
            
            if resized:
                print(f"     已调整尺寸")
            
            success_count += 1
            total_original_size += original_size
            total_compressed_size += compressed_size
        else:
            print(f"  ❌ 压缩失败")
            failed_count += 1
        
        print()
    
    # 输出统计信息
    print("=" * 60)
    print(f"✨ 压缩完成！")
    print(f"✅ 成功: {success_count}/{len(image_files)}")
    print(f"❌ 失败: {failed_count}/{len(image_files)}")
    
    if success_count > 0:
        total_reduction = ((total_original_size - total_compressed_size) / total_original_size * 100)
        print(f"\n📊 总体统计:")
        print(f"   原始总大小: {total_original_size:.1f} KB ({total_original_size/1024:.1f} MB)")
        print(f"   压缩后总大小: {total_compressed_size:.1f} KB ({total_compressed_size/1024:.1f} MB)")
        print(f"   节省空间: {total_original_size - total_compressed_size:.1f} KB ({total_reduction:.1f}%)")
    
    print("\n💡 提示:")
    print("   1. 检查压缩后的图片质量")
    print("   2. 如果质量不满意，可以调整 QUALITY 参数（当前: {})".format(QUALITY))
    print("   3. 建议在小程序中使用 < 200KB 的图片")


def batch_rename_images():
    """批量重命名图片（可选功能）"""
    print("🔄 批量重命名功能")
    print("此功能可以将图片统一命名格式")
    print("当前未启用，如需使用请修改脚本")


if __name__ == "__main__":
    try:
        # 检查 PIL 是否安装
        try:
            from PIL import Image
        except ImportError:
            print("❌ 错误：未安装 Pillow 库")
            print("📝 请运行: pip install pillow")
            exit(1)
        
        compress_all_images()
        
    except KeyboardInterrupt:
        print("\n\n⚠️  压缩已中断")
    except Exception as e:
        print(f"\n\n❌ 发生错误: {e}")
        import traceback
        traceback.print_exc()
