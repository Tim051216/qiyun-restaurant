#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
自动重命名图片工具
把下载的图片放到 downloads 文件夹，脚本会自动识别并重命名
"""

import os
import shutil
from PIL import Image
import imagehash

# 菜品列表（按顺序）
DISHES = [
    # 川味麻辣风
    {'file': 'gbyd.jpg', 'name': '干煸芸豆', 'keywords': ['芸豆', '豆角', '干煸']},
    {'file': 'mpdf.jpg', 'name': '麻婆豆腐', 'keywords': ['麻婆', '豆腐', '红油']},
    {'file': 'yxrs.jpg', 'name': '鱼香肉丝', 'keywords': ['鱼香', '肉丝', '木耳']},
    {'file': 'szyu.jpg', 'name': '水煮鱼', 'keywords': ['水煮', '鱼', '辣椒']},
    {'file': 'lzj.jpg', 'name': '辣子鸡', 'keywords': ['辣子', '鸡', '干辣椒']},
    {'file': 'mxw.jpg', 'name': '毛血旺', 'keywords': ['毛血旺', '血', '毛肚']},
    {'file': 'ksj.jpg', 'name': '口水鸡', 'keywords': ['口水', '鸡', '花生']},
    {'file': 'ffpp.jpg', 'name': '夫妻肺片', 'keywords': ['夫妻', '肺片', '牛肉']},
    
    # 家常小炒
    {'file': 'tclj.jpg', 'name': '糖醋里脊', 'keywords': ['糖醋', '里脊', '红色']},
    {'file': 'jjrs.jpg', 'name': '京酱肉丝', 'keywords': ['京酱', '肉丝', '豆腐皮']},
    {'file': 'hgr.jpg', 'name': '回锅肉', 'keywords': ['回锅', '五花肉', '青椒']},
    {'file': 'gbjd.jpg', 'name': '宫保鸡丁', 'keywords': ['宫保', '鸡丁', '花生']},
    {'file': 'qjrs.jpg', 'name': '青椒肉丝', 'keywords': ['青椒', '肉丝']},
    {'file': 'mxr.jpg', 'name': '木须肉', 'keywords': ['木须', '鸡蛋', '木耳']},
    {'file': 'hsr.jpg', 'name': '红烧肉', 'keywords': ['红烧肉', '五花肉', '红亮']},
    {'file': 'xcr.jpg', 'name': '小炒肉', 'keywords': ['小炒', '肉', '青椒']},
    {'file': 'myss.jpg', 'name': '蚂蚁上树', 'keywords': ['蚂蚁', '粉丝', '肉末']},
    
    # 时蔬素菜
    {'file': 'sltds.jpg', 'name': '酸辣土豆丝', 'keywords': ['土豆丝', '酸辣']},
    {'file': 'xhscjd.jpg', 'name': '西红柿炒鸡蛋', 'keywords': ['西红柿', '鸡蛋', '番茄']},
    {'file': 'hsqz.jpg', 'name': '红烧茄子', 'keywords': ['茄子', '红烧']},
    {'file': 'dsx.jpg', 'name': '地三鲜', 'keywords': ['地三鲜', '茄子', '土豆']},
    {'file': 'qcss.jpg', 'name': '清炒时蔬', 'keywords': ['青菜', '蔬菜', '绿色']},
    {'file': 'gbdj.jpg', 'name': '干煸豆角', 'keywords': ['豆角', '干煸', '长豆']},
    {'file': 'ssbc.jpg', 'name': '手撕包菜', 'keywords': ['包菜', '手撕', '卷心菜']},
    {'file': 'srxlh.jpg', 'name': '蒜蓉西兰花', 'keywords': ['西兰花', '蒜蓉', '绿色']},
    {'file': 'ymc.jpg', 'name': '油麦菜', 'keywords': ['油麦菜', '生菜', '绿叶']},
    
    # 汤品主食
    {'file': 'mf.jpg', 'name': '米饭', 'keywords': ['米饭', '白饭', '大米']},
    {'file': 'mt.jpg', 'name': '馒头', 'keywords': ['馒头', '白馒头', '蒸']},
    {'file': 'slt.jpg', 'name': '酸辣汤', 'keywords': ['酸辣汤', '汤', '豆腐']},
    {'file': 'gdt.jpg', 'name': '疙瘩汤', 'keywords': ['疙瘩汤', '面疙瘩']},
    {'file': 'zcdht.jpg', 'name': '紫菜蛋花汤', 'keywords': ['紫菜', '蛋花', '汤']},
    {'file': 'xhsjdt.jpg', 'name': '西红柿鸡蛋汤', 'keywords': ['西红柿', '鸡蛋', '汤']},
    {'file': 'cf.jpg', 'name': '炒饭', 'keywords': ['炒饭', '蛋炒饭', '米饭']},
    {'file': 'cm.jpg', 'name': '炒面', 'keywords': ['炒面', '面条']},
    {'file': 'lyt.jpg', 'name': '老鸭汤', 'keywords': ['鸭汤', '鸭', '汤']},
    {'file': 'pgt.jpg', 'name': '排骨汤', 'keywords': ['排骨', '汤', '玉米']},
    
    # 夜宵烧烤
    {'file': 'kyrc.jpg', 'name': '烤羊肉串', 'keywords': ['羊肉串', '烤串', '羊肉']},
    {'file': 'kjc.jpg', 'name': '烤鸡翅', 'keywords': ['鸡翅', '烤', '翅膀']},
    {'file': 'kyy.jpg', 'name': '烤鱿鱼', 'keywords': ['鱿鱼', '烤', '海鲜']},
    {'file': 'kjc2.jpg', 'name': '烤韭菜', 'keywords': ['韭菜', '烤', '绿色']},
    {'file': 'kqz.jpg', 'name': '烤茄子', 'keywords': ['茄子', '烤', '蒜蓉']},
    {'file': 'kym.jpg', 'name': '烤玉米', 'keywords': ['玉米', '烤', '黄色']},
    {'file': 'ksh.jpg', 'name': '烤生蚝', 'keywords': ['生蚝', '烤', '蒜蓉']},
    {'file': 'knrc.jpg', 'name': '烤牛肉串', 'keywords': ['牛肉串', '烤串', '牛肉']},
    {'file': 'kjx.jpg', 'name': '烤鸡心', 'keywords': ['鸡心', '烤', '串']},
    {'file': 'kjzg.jpg', 'name': '烤金针菇', 'keywords': ['金针菇', '烤', '蘑菇']},
    {'file': 'ktdp.jpg', 'name': '烤土豆片', 'keywords': ['土豆', '烤', '片']},
    {'file': 'kmtp.jpg', 'name': '烤馒头片', 'keywords': ['馒头', '烤', '片']},
    
    # 特色龙虾
    {'file': 'mlxlx.jpg', 'name': '麻辣小龙虾', 'keywords': ['龙虾', '麻辣', '红色']},
    {'file': 'srxlx.jpg', 'name': '蒜蓉小龙虾', 'keywords': ['龙虾', '蒜蓉']},
    {'file': 'ssxlx.jpg', 'name': '十三香龙虾', 'keywords': ['龙虾', '十三香']},
    {'file': 'ymdx.jpg', 'name': '油焖大虾', 'keywords': ['大虾', '油焖', '红色']},
    {'file': 'qzlx.jpg', 'name': '清蒸龙虾', 'keywords': ['龙虾', '清蒸', '波士顿']},
    {'file': 'zsjlx.jpg', 'name': '芝士焗龙虾', 'keywords': ['龙虾', '芝士', '焗']},
    {'file': 'bzxlx.jpg', 'name': '冰镇小龙虾', 'keywords': ['龙虾', '冰镇', '冰']},
    {'file': 'jylx.jpg', 'name': '椒盐龙虾', 'keywords': ['龙虾', '椒盐']},
    {'file': 'bftlx.jpg', 'name': '避风塘龙虾', 'keywords': ['龙虾', '避风塘', '蒜']},
    
    # 饮品酒水
    {'file': 'kkl.jpg', 'name': '可口可乐', 'keywords': ['可乐', 'cola', '红色']},
    {'file': 'xb.jpg', 'name': '雪碧', 'keywords': ['雪碧', 'sprite', '绿色']},
    {'file': 'cz.jpg', 'name': '橙汁', 'keywords': ['橙汁', '橙色', '果汁']},
    {'file': 'xgz.jpg', 'name': '西瓜汁', 'keywords': ['西瓜', '红色', '果汁']},
    {'file': 'nms.jpg', 'name': '柠檬水', 'keywords': ['柠檬', '水', '黄色']},
    {'file': 'smt.jpg', 'name': '酸梅汤', 'keywords': ['酸梅', '汤', '深色']},
    {'file': 'nc.jpg', 'name': '奶茶', 'keywords': ['奶茶', '珍珠', '茶']},
    {'file': 'dj.jpg', 'name': '豆浆', 'keywords': ['豆浆', '白色', '豆']},
    {'file': 'pj.jpg', 'name': '啤酒', 'keywords': ['啤酒', 'beer', '黄色']},
    {'file': 'hj.jpg', 'name': '红酒', 'keywords': ['红酒', 'wine', '红色']},
    {'file': 'bj.jpg', 'name': '白酒', 'keywords': ['白酒', '酒', '瓶']},
    {'file': 'kqs.jpg', 'name': '矿泉水', 'keywords': ['矿泉水', '水', '瓶']},
]

def process_images():
    """处理下载的图片"""
    download_dir = 'downloads'
    output_dir = 'static/menu/menulist'
    
    # 创建目录
    os.makedirs(download_dir, exist_ok=True)
    os.makedirs(output_dir, exist_ok=True)
    
    # 获取下载文件夹中的所有图片
    image_files = []
    for f in os.listdir(download_dir):
        if f.lower().endswith(('.jpg', '.jpeg', '.png', '.webp')):
            image_files.append(f)
    
    if not image_files:
        print("❌ downloads 文件夹中没有图片文件")
        print()
        print("请按以下步骤操作：")
        print("1. 在项目根目录创建 'downloads' 文件夹")
        print("2. 把从百度下载的图片都放到这个文件夹")
        print("3. 重新运行此脚本")
        return
    
    print("=" * 60)
    print("自动重命名图片工具")
    print("=" * 60)
    print()
    print(f"📁 发现 {len(image_files)} 张图片")
    print()
    
    # 按顺序处理每道菜
    for i, dish in enumerate(DISHES, 1):
        print(f"[{i}/{len(DISHES)}] {dish['name']} ({dish['file']})")
        
        if len(image_files) == 0:
            print("   ⚠️  没有更多图片了")
            break
        
        # 显示当前图片
        print(f"   当前图片: {image_files[0]}")
        
        # 询问用户
        choice = input(f"   这是 {dish['name']} 吗？(y=是/n=否/s=跳过): ").strip().lower()
        
        if choice == 'y':
            # 复制并重命名
            src = os.path.join(download_dir, image_files[0])
            dst = os.path.join(output_dir, dish['file'])
            
            try:
                # 如果是PNG或WEBP，转换为JPG
                if image_files[0].lower().endswith(('.png', '.webp')):
                    img = Image.open(src)
                    if img.mode in ('RGBA', 'LA', 'P'):
                        img = img.convert('RGB')
                    img.save(dst, 'JPEG', quality=90)
                else:
                    shutil.copy2(src, dst)
                
                print(f"   ✅ 已保存为 {dish['file']}")
                image_files.pop(0)
            except Exception as e:
                print(f"   ❌ 保存失败: {e}")
        
        elif choice == 'n':
            print("   ⏭️  跳过此图片，查看下一张")
            image_files.pop(0)
            # 重新处理当前菜品
            i -= 1
        
        elif choice == 's':
            print("   ⏭️  跳过此菜品")
        
        else:
            print("   ⚠️  无效输入，跳过")
        
        print()
    
    print("=" * 60)
    print("处理完成！")
    print("=" * 60)
    
    if image_files:
        print(f"⚠️  还有 {len(image_files)} 张图片未处理")
        print("   可以重新运行脚本继续处理")
    else:
        print("✅ 所有图片已处理完成")

def main():
    """主函数"""
    print()
    print("=" * 60)
    print("自动重命名图片工具")
    print("=" * 60)
    print()
    print("使用说明：")
    print("1. 在项目根目录创建 'downloads' 文件夹")
    print("2. 把从百度下载的所有图片放到 downloads 文件夹")
    print("3. 运行此脚本")
    print("4. 脚本会逐个显示图片，询问是否匹配")
    print("5. 输入 y(是) / n(否，看下一张) / s(跳过此菜品)")
    print()
    input("准备好了吗？按回车键开始...")
    print()
    
    process_images()

if __name__ == '__main__':
    main()
