<template>
  <div class="realtime-order">
    <div v-if="loading" style="text-align: center; padding: 50px;">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>
    <div v-else-if="orders.length === 0" style="text-align: center; padding: 50px; color: #909399;">
      <el-icon :size="60"><Document /></el-icon>
      <p style="margin-top: 20px;">暂无实时订单</p>
    </div>
    <el-row v-else :gutter="20">
      <el-col :xs="24" :sm="12" :lg="6" v-for="order in orders" :key="order.id">
        <el-card class="order-card" :class="'status-' + order.status">
          <template #header>
            <div class="card-header">
              <span class="table-no">{{ order.tableNo }}</span>
              <el-tag :type="getStatusType(order.status)" size="small">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </template>
          <div class="order-info">
            <div class="order-no">订单号：{{ order.orderNo }}</div>
            <div class="order-time">下单时间：{{ order.time }}</div>
            <div class="order-dishes">
              <div v-for="(dish, index) in order.dishes" :key="index" class="dish-item">
                {{ dish.name }} x{{ dish.count }}
              </div>
            </div>
            <div class="order-amount">
              金额：<span class="amount">¥{{ order.amount }}</span>
            </div>
          </div>
          <template #footer>
            <div class="order-actions">
              <el-button v-if="order.status === 'pending'" type="success" size="small" @click="acceptOrder(order)">
                接单
              </el-button>
              <el-button v-if="order.status === 'cooking'" type="primary" size="small" @click="completeOrder(order)">
                完成
              </el-button>
              <el-button v-if="order.status === 'pending'" type="danger" size="small" @click="cancelOrder(order)">
                取消
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Document } from '@element-plus/icons-vue'
import { getRealtimeOrders, updateOrderStatus } from '@/api/order'

const orders = ref([])
const loading = ref(false)

let timer = null

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    cooking: 'primary',
    completed: 'success'
  }
  return map[status]
}

const getStatusText = (status) => {
  const map = {
    pending: '待接单',
    cooking: '制作中',
    completed: '已完成'
  }
  return map[status]
}

// 加载实时订单
const loadRealtimeOrders = async () => {
  loading.value = true
  try {
    console.log('Loading realtime orders...')
    const res = await getRealtimeOrders({ page: 1, size: 20 })
    console.log('API Response:', res)
    
    if (res.code === 1 && res.data && res.data.records) {
      console.log('Records found:', res.data.records.length)
      orders.value = res.data.records.map(order => ({
        id: order.id,
        orderNo: order.orderNo,
        tableNo: order.tableNo,
        status: order.status,
        time: order.createTime ? order.createTime.substring(11, 16) : '',
        dishes: order.details ? order.details.map(d => ({ name: d.dishName, count: d.quantity })) : [],
        amount: order.amount
      }))
      console.log('Mapped orders:', orders.value)
    } else {
      console.warn('No records in response:', res)
    }
  } catch (error) {
    console.error('加载实时订单失败:', error)
    ElMessage.error('加载实时订单失败')
  } finally {
    loading.value = false
  }
}

const acceptOrder = async (order) => {
  try {
    const res = await updateOrderStatus(order.id, 'cooking')
    if (res.code === 1) {
      order.status = 'cooking'
      ElMessage.success('接单成功')
      playSound()
    }
  } catch (error) {
    console.error('接单失败:', error)
    ElMessage.error('接单失败')
  }
}

const completeOrder = async (order) => {
  try {
    const res = await updateOrderStatus(order.id, 'completed')
    if (res.code === 1) {
      order.status = 'completed'
      ElMessage.success('订单已完成')
      setTimeout(() => {
        const index = orders.value.findIndex(o => o.id === order.id)
        if (index > -1) {
          orders.value.splice(index, 1)
        }
      }, 2000)
    }
  } catch (error) {
    console.error('完成订单失败:', error)
    ElMessage.error('完成订单失败')
  }
}

const cancelOrder = (order) => {
  ElMessageBox.confirm('确认取消订单？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updateOrderStatus(order.id, 'cancelled')
      if (res.code === 1) {
        const index = orders.value.findIndex(o => o.id === order.id)
        if (index > -1) {
          orders.value.splice(index, 1)
        }
        ElMessage.info('订单已取消')
      }
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  })
}

const playSound = () => {
  // 播放提示音
  const audio = new Audio('/notification.mp3')
  audio.play().catch(() => {})
}

// 定时刷新实时订单
const startPolling = () => {
  timer = setInterval(() => {
    loadRealtimeOrders()
  }, 10000) // 每10秒刷新一次
}

onMounted(() => {
  loadRealtimeOrders()
  startPolling()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.realtime-order {
  .order-card {
    margin-bottom: 20px;
    transition: all 0.3s;

    &.status-pending {
      border-left: 4px solid #E6A23C;
    }

    &.status-cooking {
      border-left: 4px solid #409EFF;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .table-no {
        font-size: 20px;
        font-weight: bold;
        color: #303133;
      }
    }

    .order-info {
      .order-no,
      .order-time {
        font-size: 12px;
        color: #909399;
        margin-bottom: 8px;
      }

      .order-dishes {
        margin: 15px 0;
        padding: 10px;
        background-color: #f5f7fa;
        border-radius: 4px;

        .dish-item {
          font-size: 14px;
          color: #606266;
          margin: 5px 0;
        }
      }

      .order-amount {
        font-size: 14px;
        color: #606266;

        .amount {
          font-size: 18px;
          font-weight: bold;
          color: #F56C6C;
        }
      }
    }

    .order-actions {
      display: flex;
      gap: 10px;
    }
  }
}
</style>
