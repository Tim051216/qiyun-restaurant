<template>
  <div class="coupon-manage">
    <el-card>
      <el-button type="primary" @click="handleAdd" style="margin-bottom: 20px;">
        <el-icon><Plus /></el-icon>
        添加优惠券
      </el-button>

      <el-table :data="couponList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="优惠券名称" width="180" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="amount" label="面额" width="100">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="condition" label="使用条件" width="150" />
        <el-table-column prop="total" label="发放总量" width="100" />
        <el-table-column prop="received" label="已领取" width="100" />
        <el-table-column prop="used" label="已使用" width="100" />
        <el-table-column prop="validDays" label="有效期" width="120">
          <template #default="{ row }">
            {{ row.validDays }}天
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCouponList, deleteCoupon } from '@/api/coupon'

const loading = ref(false)
const couponList = ref([])

// 加载优惠券列表
const loadCouponList = async () => {
  loading.value = true
  try {
    const res = await getCouponList()
    if (res.code === 1) {
      couponList.value = res.data.map(coupon => ({
        ...coupon,
        type: coupon.type === 'discount' ? '满减券' : coupon.type,
        condition: `满${coupon.conditionAmount}元可用`,
        status: coupon.status === 1
      }))
    }
  } catch (error) {
    console.error('加载优惠券列表失败:', error)
    ElMessage.error('加载优惠券列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  ElMessage.info('添加优惠券功能')
}

const handleEdit = (row) => {
  ElMessage.info('编辑优惠券：' + row.name)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该优惠券？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteCoupon(row.id)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        loadCouponList()
      }
    } catch (error) {
      console.error('删除优惠券失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  loadCouponList()
})
</script>
