<template>
  <div class="order-list">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="桌号">
          <el-input v-model="searchForm.tableNo" placeholder="请输入桌号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="全部" value="" />
            <el-option label="待接单" value="pending" />
            <el-option label="制作中" value="cooking" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单列表 -->
      <el-table :data="orderList" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="tableNo" label="桌号" width="100" />
        <el-table-column prop="dinerCount" label="就餐人数" width="100" />
        <el-table-column prop="dishes" label="菜品" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="(dish, index) in row.dishes" :key="index" size="small" style="margin: 2px;">
              {{ dish }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="订单金额" width="120">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'pending'" type="success" size="small" @click="handleAccept(row)">
              接单
            </el-button>
            <el-button v-if="row.status === 'pending'" type="danger" size="small" @click="handleCancel(row)">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="桌号">{{ currentOrder.tableNo }}</el-descriptions-item>
        <el-descriptions-item label="就餐人数">{{ currentOrder.dinerCount }}人</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="菜品明细" :span="2">
          <div v-for="(dish, index) in currentOrder.dishes" :key="index" style="margin: 5px 0;">
            {{ dish }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额" :span="2">
          <span style="color: #F56C6C; font-weight: bold; font-size: 18px;">¥{{ currentOrder.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderPage, getOrderById, updateOrderStatus } from '@/api/order'

const loading = ref(false)
const detailVisible = ref(false)
const currentOrder = ref(null)

const searchForm = reactive({
  orderNo: '',
  tableNo: '',
  status: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const orderList = ref([])

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    cooking: 'primary',
    completed: 'success',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待接单',
    cooking: '制作中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

// 加载订单列表
const loadOrderList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      orderNo: searchForm.orderNo,
      tableNo: searchForm.tableNo,
      status: searchForm.status
    }
    
    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0].toISOString().split('T')[0]
      params.endDate = searchForm.dateRange[1].toISOString().split('T')[0]
    }
    
    const res = await getOrderPage(params)
    if (res.code === 1) {
      orderList.value = res.data.records.map(order => ({
        ...order,
        dishes: order.details ? order.details.map(d => `${d.dishName}x${d.quantity}`) : []
      }))
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadOrderList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    orderNo: '',
    tableNo: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

const handleView = async (row) => {
  try {
    const res = await getOrderById(row.id)
    if (res.code === 1) {
      currentOrder.value = {
        ...res.data,
        dishes: res.data.details ? res.data.details.map(d => `${d.dishName} x${d.quantity} - ¥${d.amount}`) : []
      }
      detailVisible.value = true
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  }
}

const handleAccept = (row) => {
  ElMessageBox.confirm('确认接单？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updateOrderStatus(row.id, 'cooking')
      if (res.code === 1) {
        ElMessage.success('接单成功')
        loadOrderList()
      }
    } catch (error) {
      console.error('接单失败:', error)
      ElMessage.error('接单失败')
    }
  })
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消订单？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updateOrderStatus(row.id, 'cancelled')
      if (res.code === 1) {
        ElMessage.success('订单已取消')
        loadOrderList()
      }
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  })
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadOrderList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadOrderList()
}

onMounted(() => {
  loadOrderList()
})
</script>

<style lang="scss" scoped>
.order-list {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
