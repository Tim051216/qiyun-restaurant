<template>
  <div class="level-manage">
    <el-card>
      <el-table :data="levelList" style="width: 100%">
        <el-table-column prop="level" label="等级" width="100" />
        <el-table-column prop="name" label="等级名称" width="150" />
        <el-table-column prop="growthValue" label="成长值要求" width="150" />
        <el-table-column prop="discount" label="折扣" width="100">
          <template #default="{ row }">
            {{ row.discount }}折
          </template>
        </el-table-column>
        <el-table-column prop="benefits" label="权益" min-width="300" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const levelList = ref([
  { id: 1, level: 'V1', name: '普通会员', growthValue: 0, discount: 9.9, benefits: '积分兑换、生日优惠' },
  { id: 2, level: 'V2', name: '银卡会员', growthValue: 500, discount: 9.5, benefits: '积分兑换、生日优惠、专属客服' },
  { id: 3, level: 'V3', name: '金卡会员', growthValue: 1500, discount: 9.0, benefits: '积分兑换、生日优惠、专属客服、优先预订' },
  { id: 4, level: 'V4', name: '白金会员', growthValue: 3000, discount: 8.8, benefits: '积分兑换、生日优惠、专属客服、优先预订、免费停车' },
  { id: 5, level: 'V5', name: '钻石会员', growthValue: 5000, discount: 8.5, benefits: '积分兑换、生日优惠、专属客服、优先预订、免费停车、包厢优先' }
])

const handleEdit = (row) => {
  ElMessage.info('编辑等级：' + row.name)
}
</script>
