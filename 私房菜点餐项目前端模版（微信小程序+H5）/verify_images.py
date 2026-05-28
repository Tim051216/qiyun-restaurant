#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
验证菜品图片是否都存在
"""

import os
import json

# 图片目录
IMAGE_DIR = "./static/menu/menulist"

# 从 menu.js 提取的所有图片文件名
REQUIRED_IMAGES = [
    # 川味麻辣风
    'gbyd.jpg', 'mpdf.jpg', 'yxrs.jpg', 'szyu.jpg', 
    'lzj.jpg', 'mxw.jpg', 'ksj.jpg', 'ffpp.jpg',
    
    # 家常小炒
    'tclj.jpg', 'jjrs.jpg', 'hgr.jpg', 'gbjd.jpg',
    'qjrs.jpg', 'mxr.jpg', 'hsr.jpg', 'xcr.jpg', 'myss.jpg',
    
    # 时蔬素菜
    'sltds.jpg', 'xhscjd.jpg', 'hsqz.jpg', 'dsx.jpg',
    'qcss.jpg', 'gbdj.jpg', 'ssbc.jpg', 'srxlh.jpg', 'ymc.jpg',
    
    # 汤品主食
    'mf.jpg', 'mt.jpg', 'slt.jpg', 'gdt.jpg',
    'zcdht.jpg', 'xhsjdt.jpg', 'cf.jpg', 'cm.jpg',
    'lyt.jpg', 'pgt.jpg',
    
    # 夜宵烧烤
    'kyrc.jpg', 'kjc.jpg', 'kyy.jpg', 'kjc2.jpg',
    'kqz.jpg', 'kym.jpg', 'ksh.jpg', 'knrc.jpg',
    'kjx.jpg', 'kjzg.jpg', 'ktdp.jpg', 'kmtp.jpg',
    
    # 特色龙虾
    'mlxlx.jpg', 'srxlx.jpg', 'ssxlx.jpg', 'ymdx.jpg',
    'qzlx.jpg', 'zsjlx.jpg', 'bzxlx.jpg', 'jylx.jpg', 'bftlx.jpg',
    
    # 饮品酒水
    'kkl.jpg', 'xb.jpg', 'cz.jpg', 'xgz.jpg',
    'nms.jpg', 'smt.jpg', 'nc.jpg', 'dj.jpg',
    'pj.jpg', 'hj.jpg', 'bj.jpg', 'kqs.jpg',
]

# 菜品名称映射
DISH_NAMES = {
    'gbyd.jpg': '干煸芸豆',
    'mpdf.jpg': '麻婆豆腐',
    'yxrs.jpg': '鱼香肉丝',
    'szyu.jpg': '水煮鱼',
    'lzj.jpg': '辣子鸡',
    'mxw.jpg': '毛血旺',
    'ksj.jpg': '口水鸡',
    'ffpp.jpg': '夫妻肺片',
    'tclj.jpg': '糖醋里脊',
    'jjrs.jpg': '京酱肉丝',
    'hgr.jpg': '回锅肉',
    'gbjd.jpg': '宫保鸡丁',
    'qjrs.jpg': '青椒肉丝',
    'mxr.jpg': '木须肉',
    'hsr.jpg': '红烧肉',
    'xcr.jpg': '小炒肉',
    'myss.jpg': '蚂蚁上树',
    'sltds.jpg': '酸辣土豆丝',
    'xhscjd.jpg': '西红柿炒鸡蛋',
    'hsqz.jpg': '红烧茄子',
    'dsx.jpg': '地三鲜',
    'qcss.jpg': '清炒时蔬',
    'gbdj.jpg': '干煸豆角',
    'ssbc.jpg': '手撕包菜',
    'srxlh.jpg': '蒜蓉西兰花',
    'ymc.jpg': '油麦菜',
    'mf.jpg': '米饭',
    'mt.jpg': '馒头',
    'slt.jpg': '酸辣汤',
    'gdt.jpg': '疙瘩汤',
    'zcdht.jpg': '紫菜蛋花汤',
    'xhsjdt.jpg': '西红柿鸡蛋汤',
    'cf.jpg': '炒饭',
    'cm.jpg': '炒面',
    'lyt.jpg': '老鸭汤',
    'pgt.jpg': '排骨汤',
    'kyrc.jpg': '烤羊肉串',
    'kjc.jpg': '烤鸡翅',
    'kyy.jpg': '烤鱿鱼',
    'kjc2.jpg': '烤韭菜',
    'kqz.jpg': '烤茄子',
    'kym.jpg': '烤玉米',
    'ksh.jpg': '烤生蚝',
    'knrc.jpg': '烤牛肉串',
    'kjx.jpg': '烤鸡心',
    'kjzg.jpg': '烤金针菇',
    'ktdp.jpg': '烤土豆片',
    'kmtp.jpg': '烤馒头片',
    'mlxlx.jpg': '麻辣小龙虾',
    'srxlx.jpg': '蒜蓉小龙虾',
    'ssxlx.jpg': '十三香龙虾',
    'ymdx.jpg': '油焖大虾',
    'qzlx.jpg': '清蒸龙虾',
    'zsjlx.jpg': '芝士焗龙虾',
    'bzxlx.jpg': '冰镇小龙虾',
    'jylx.jpg': '椒盐龙虾',
    'bftlx.jpg': '避风塘龙虾',
    'kkl.jpg': '可口可乐',
    'xb.jpg': '雪碧',
    'cz.jpg': '橙汁',
    'xgz.jpg': '西瓜汁',
    'nms.jpg': '柠檬水',
    'smt.jpg': '酸梅汤',
    'nc.jpg': '奶茶',
    'dj.jpg': '豆浆',
    'pj.jpg': '啤酒',
    'hj.jpg': '红酒',
    'bj.jpg': '白酒',
    'kqs.jpg': '矿泉水',
}


def verify_images():
    """验证所有图片是否存在"""
    
    print("=" * 60)
    print("🔍 验证菜品图片")
    print("=" * 60)
    print(f"📁 图片目录: {IMAGE_DIR}")
    print(f"📋 需要验证: {len(REQUIRED_IMAGES)} 张图片\n")
    
    if not os.path.exists(IMAGE_DIR):
        print(f"❌ 错误：目录不存在 {IMAGE_DIR}")
        return
    
    # 检查每张图片
    missing_images = []
    existing_images = []
    
    for image_file in REQUIRED_IMAGES:
        image_path = os.path.join(IMAGE_DIR, image_file)
        dish_name = DISH_NAMES.get(image_file, '未知')
        
        if os.path.exists(image_path):
            file_size = os.path.getsize(image_path) / 1024
            existing_images.append((image_file, dish_name, file_size))
            print(f"✅ {dish_name:12s} ({image_file:15s}) - {file_size:.1f} KB")
        else:
            missing_images.append((image_file, dish_name))
            print(f"❌ {dish_name:12s} ({image_file:15s}) - 缺失")
    
    # 输出统计
    print("\n" + "=" * 60)
    print("📊 验证结果")
    print("=" * 60)
    print(f"✅ 存在: {len(existing_images)}/{len(REQUIRED_IMAGES)}")
    print(f"❌ 缺失: {len(missing_images)}/{len(REQUIRED_IMAGES)}")
    
    if existing_images:
        total_size = sum(size for _, _, size in existing_images) / 1024
        avg_size = (total_size * 1024) / len(existing_images)
        print(f"📦 总大小: {total_size:.2f} MB")
        print(f"📏 平均大小: {avg_size:.1f} KB/张")
    
    if missing_images:
        print("\n⚠️  缺失的图片:")
        for image_file, dish_name in missing_images:
            print(f"   - {dish_name} ({image_file})")
    else:
        print("\n🎉 所有图片都已就绪！")
        print("\n✨ 可以在微信开发者工具中查看效果了")
    
    print("\n" + "=" * 60)


if __name__ == "__main__":
    verify_images()
