<template>
  <div class="flashsale-manage">
    <el-card>
      <el-button type="primary" @click="handleAdd" style="margin-bottom: 20px;">
        <el-icon><Plus /></el-icon>
        添加秒杀活动
      </el-button>

      <el-table :data="flashsaleList" style="width: 100%">
        <el-table-column prop="name" label="活动名称" width="200" />
        <el-table-column prop="timeSlot" label="时间段" width="150" />
        <el-table-column prop="product" label="商品" width="150" />
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            ¥{{ row.originalPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="salePrice" label="秒杀价" width="100">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ row.salePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="sold" label="已售" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const flashsaleList = ref([
  { id: 1, name: '午市秒杀', timeSlot: '12:00-14:00', product: '50元代金券', originalPrice: 50, salePrice: 5, stock: 100, sold: 78, status: '进行中' },
  { id: 2, name: '晚市秒杀', timeSlot: '18:00-20:00', product: '100元代金券', originalPrice: 100, salePrice: 10, stock: 50, sold: 23, status: '未开始' }
])

const getStatusType = (status) => {
  const map = {
    '进行中': 'success',
    '未开始': 'warning',
    '已结束': 'info'
  }
  return map[status]
}

const handleAdd = () => {
  ElMessage.info('添加秒杀活动功能')
}

const handleEdit = (row) => {
  ElMessage.info('编辑秒杀活动：' + row.name)
}

const handleDelete = (row) => {
  ElMessage.info('删除秒杀活动：' + row.name)
}
</script>
