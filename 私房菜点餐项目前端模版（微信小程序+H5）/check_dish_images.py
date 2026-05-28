#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
菜品图片匹配检查工具
检查下载的图片是否与菜品名称匹配
"""

import os
import json

# 菜品映射表（文件名 -> 菜品名称）
DISH_MAPPING = {
    # 川味麻辣风
    'gbyd.jpg': '干煸芸豆',
    'mpdf.jpg': '麻婆豆腐',
    'yxrs.jpg': '鱼香肉丝',
    'szyu.jpg': '水煮鱼',
    'lzj.jpg': '辣子鸡',
    'mxw.jpg': '毛血旺',
    'ksj.jpg': '口水鸡',
    'ffpp.jpg': '夫妻肺片',
    
    # 家常小炒
    'tclj.jpg': '糖醋里脊',
    'jjrs.jpg': '京酱肉丝',
    'hgr.jpg': '回锅肉',
    'gbjd.jpg': '宫保鸡丁',
    'qjrs.jpg': '青椒肉丝',
    'mxr.jpg': '木须肉',
    'hsr.jpg': '红烧肉',
    'xcr.jpg': '小炒肉',
    'myss.jpg': '蚂蚁上树',
    
    # 时蔬素菜
    'sltds.jpg': '酸辣土豆丝',
    'xhscjd.jpg': '西红柿炒鸡蛋',
    'hsqz.jpg': '红烧茄子',
    'dsx.jpg': '地三鲜',
    'qcss.jpg': '清炒时蔬',
    'gbdj.jpg': '干煸豆角',
    'ssbc.jpg': '手撕包菜',
    'srxlh.jpg': '蒜蓉西兰花',
    'ymc.jpg': '油麦菜',
    
    # 汤品主食
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
    
    # 夜宵烧烤
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
    
    # 特色龙虾
    'mlxlx.jpg': '麻辣小龙虾',
    'srxlx.jpg': '蒜蓉小龙虾',
    'ssxlx.jpg': '十三香龙虾',
    'ymdx.jpg': '油焖大虾',
    'qzlx.jpg': '清蒸龙虾',
    'zsjlx.jpg': '芝士焗龙虾',
    'bzxlx.jpg': '冰镇小龙虾',
    'jylx.jpg': '椒盐龙虾',
    'bftlx.jpg': '避风塘龙虾',
    
    # 饮品酒水
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

def check_images():
    """检查图片文件"""
    image_dir = 'static/menu/menulist'
    
    if not os.path.exists(image_dir):
        print(f"❌ 图片目录不存在: {image_dir}")
        return
    
    print("=" * 60)
    print("菜品图片匹配检查报告")
    print("=" * 60)
    print()
    
    # 获取所有图片文件
    image_files = [f for f in os.listdir(image_dir) if f.endswith('.jpg')]
    
    print(f"📊 统计信息:")
    print(f"   - 应有图片数量: {len(DISH_MAPPING)}")
    print(f"   - 实际图片数量: {len(image_files)}")
    print()
    
    # 检查缺失的图片
    missing_images = []
    for filename in DISH_MAPPING.keys():
        if filename not in image_files:
            missing_images.append((filename, DISH_MAPPING[filename]))
    
    if missing_images:
        print("❌ 缺失的图片:")
        for filename, dish_name in missing_images:
            print(f"   - {filename} ({dish_name})")
        print()
    else:
        print("✅ 所有图片都已下载")
        print()
    
    # 检查多余的图片（.png 文件）
    extra_images = [f for f in os.listdir(image_dir) if f.endswith('.png')]
    if extra_images:
        print("⚠️  发现旧版本图片（.png格式）:")
        for filename in extra_images:
            print(f"   - {filename}")
        print()
    
    # 生成需要人工检查的列表
    print("=" * 60)
    print("🔍 需要人工检查的菜品图片")
    print("=" * 60)
    print()
    print("请打开以下图片，检查是否与菜品名称匹配：")
    print()
    
    categories = {
        '川味麻辣风': ['gbyd.jpg', 'mpdf.jpg', 'yxrs.jpg', 'szyu.jpg', 'lzj.jpg', 'mxw.jpg', 'ksj.jpg', 'ffpp.jpg'],
        '家常小炒': ['tclj.jpg', 'jjrs.jpg', 'hgr.jpg', 'gbjd.jpg', 'qjrs.jpg', 'mxr.jpg', 'hsr.jpg', 'xcr.jpg', 'myss.jpg'],
        '时蔬素菜': ['sltds.jpg', 'xhscjd.jpg', 'hsqz.jpg', 'dsx.jpg', 'qcss.jpg', 'gbdj.jpg', 'ssbc.jpg', 'srxlh.jpg', 'ymc.jpg'],
        '汤品主食': ['mf.jpg', 'mt.jpg', 'slt.jpg', 'gdt.jpg', 'zcdht.jpg', 'xhsjdt.jpg', 'cf.jpg', 'cm.jpg', 'lyt.jpg', 'pgt.jpg'],
        '夜宵烧烤': ['kyrc.jpg', 'kjc.jpg', 'kyy.jpg', 'kjc2.jpg', 'kqz.jpg', 'kym.jpg', 'ksh.jpg', 'knrc.jpg', 'kjx.jpg', 'kjzg.jpg', 'ktdp.jpg', 'kmtp.jpg'],
        '特色龙虾': ['mlxlx.jpg', 'srxlx.jpg', 'ssxlx.jpg', 'ymdx.jpg', 'qzlx.jpg', 'zsjlx.jpg', 'bzxlx.jpg', 'jylx.jpg', 'bftlx.jpg'],
        '饮品酒水': ['kkl.jpg', 'xb.jpg', 'cz.jpg', 'xgz.jpg', 'nms.jpg', 'smt.jpg', 'nc.jpg', 'dj.jpg', 'pj.jpg', 'hj.jpg', 'bj.jpg', 'kqs.jpg'],
    }
    
    for category, files in categories.items():
        print(f"\n【{category}】")
        for filename in files:
            dish_name = DISH_MAPPING.get(filename, '未知')
            file_path = os.path.join(image_dir, filename)
            exists = "✅" if os.path.exists(file_path) else "❌"
            print(f"  {exists} {filename:15s} -> {dish_name}")
    
    print()
    print("=" * 60)
    print("💡 检查建议:")
    print("=" * 60)
    print("1. 在文件管理器中打开: static/menu/menulist/")
    print("2. 逐个查看图片，确认是否与菜品名称匹配")
    print("3. 如果发现不匹配的图片，记录下来")
    print("4. 告诉我哪些图片不匹配，我会帮你重新下载")
    print()
    print("常见问题:")
    print("- 烤韭菜(kjc2.jpg) 可能被误认为其他蔬菜")
    print("- 蚂蚁上树(myss.jpg) 应该是粉丝肉末，不是真的蚂蚁")
    print("- 地三鲜(dsx.jpg) 应该有茄子、土豆、青椒")
    print("- 避风塘龙虾(bftlx.jpg) 应该有面包糠")
    print()

if __name__ == '__main__':
    check_images()
