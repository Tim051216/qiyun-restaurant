<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #ecf5ff; color: #409eff;">
              <el-icon :size="32"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayOrders }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0; color: #f56c6c;">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.todaySales }}</div>
              <div class="stat-label">今日销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f4f4f5; color: #909399;">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.newMembers }}</div>
              <div class="stat-label">新增会员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff; color: #67c23a;">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.monthSales }}</div>
              <div class="stat-label">本月销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>销售趋势</span>
              <el-radio-group v-model="salesPeriod" size="small">
                <el-radio-button value="week">近7天</el-radio-button>
                <el-radio-button value="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="salesChartRef" style="height: 350px;" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>菜品销量 TOP5</span>
          </template>
          <div ref="dishChartRef" style="height: 350px;" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>实时订单</span>
              <el-button type="primary" size="small" @click="$router.push('/order/realtime')">
                查看更多
              </el-button>
            </div>
          </template>
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="tableNo" label="桌号" width="100" />
            <el-table-column prop="dishes" label="菜品" />
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: bold;">¥{{ formatMoney(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="下单时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, getDishTop, getSalesTrend } from '@/api/statistics'
import { getRealtimeOrders } from '@/api/order'

const salesPeriod = ref('week')
const salesChartRef = ref(null)
const dishChartRef = ref(null)

const stats = ref({
  todayOrders: 0,
  todaySales: '0.00',
  newMembers: 0,
  monthSales: '0.00'
})

const recentOrders = ref([])

let salesChart = null
let dishChart = null

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

const formatMoney = (value) => Number(value || 0).toFixed(2)

const renderSalesEmptyState = (title = '暂无数据') => {
  if (!salesChartRef.value) return
  salesChart = salesChart || echarts.init(salesChartRef.value)
  salesChart.setOption({
    title: {
      text: title,
      left: 'center',
      top: 'center',
      textStyle: {
        color: '#909399',
        fontSize: 14,
        fontWeight: 'normal'
      }
    }
  }, true)
}

const renderDishEmptyState = (title = '暂无数据') => {
  if (!dishChartRef.value) return
  dishChart = dishChart || echarts.init(dishChartRef.value)
  dishChart.setOption({
    title: {
      text: title,
      left: 'center',
      top: 'center',
      textStyle: {
        color: '#909399',
        fontSize: 14,
        fontWeight: 'normal'
      }
    }
  }, true)
}

const loadDashboardStats = async () => {
  try {
    const res = await getDashboardStats()
    if (res.code === 1 && res.data) {
      stats.value = {
        todayOrders: res.data.todayOrders || 0,
        todaySales: formatMoney(res.data.todaySales),
        newMembers: res.data.newMembers || 0,
        monthSales: formatMoney(res.data.monthSales)
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentOrders = async () => {
  try {
    const res = await getRealtimeOrders({ page: 1, size: 5 })
    if (res.code === 1 && res.data?.records) {
      recentOrders.value = res.data.records.map((order) => ({
        orderNo: order.orderNo,
        tableNo: order.tableNo,
        dishes: order.details?.map((dish) => dish.dishName).join('、') || '',
        amount: order.amount,
        status: order.status,
        time: order.createTime
      }))
    } else {
      recentOrders.value = []
    }
  } catch (error) {
    recentOrders.value = []
    console.error('加载实时订单失败:', error)
  }
}

const loadSalesTrend = async () => {
  try {
    const days = salesPeriod.value === 'month' ? 30 : 7
    const res = await getSalesTrend(days)
    if (res.code !== 1 || !res.data) {
      renderSalesEmptyState('加载失败')
      return
    }

    const dates = res.data.dates || []
    const sales = (res.data.sales || []).map((item) => Number(item || 0))
    const orders = (res.data.orders || []).map((item) => Number(item || 0))
    const hasData = sales.some((item) => item > 0) || orders.some((item) => item > 0)

    salesChart = salesChart || echarts.init(salesChartRef.value)
    salesChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['销售额', '订单量']
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: [
        {
          type: 'value',
          name: '销售额(元)',
          position: 'left'
        },
        {
          type: 'value',
          name: '订单量',
          position: 'right'
        }
      ],
      series: [
        {
          name: '销售额',
          type: 'line',
          smooth: true,
          data: sales,
          itemStyle: { color: '#409eff' }
        },
        {
          name: '订单量',
          type: 'bar',
          yAxisIndex: 1,
          data: orders,
          itemStyle: { color: '#67c23a' }
        }
      ],
      graphic: hasData ? [] : [
        {
          type: 'text',
          left: 'center',
          top: 'middle',
          style: {
            text: '暂无数据',
            fill: '#909399',
            fontSize: 14
          }
        }
      ]
    }, true)
  } catch (error) {
    console.error('加载销售趋势失败:', error)
    renderSalesEmptyState('加载失败')
  }
}

const loadDishTop = async () => {
  try {
    const res = await getDishTop(5)
    if (res.code !== 1 || !Array.isArray(res.data) || res.data.length === 0) {
      renderDishEmptyState()
      return
    }

    const data = res.data.map((dish) => ({
      value: Number(dish.sales || 0),
      name: dish.name
    }))

    dishChart = dishChart || echarts.init(dishChartRef.value)
    dishChart.setOption({
      tooltip: {
        trigger: 'item'
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            formatter: '{b}: {c}份'
          },
          data
        }
      ]
    }, true)
  } catch (error) {
    console.error('加载菜品销量失败:', error)
    renderDishEmptyState('加载失败')
  }
}

const handleResize = () => {
  salesChart?.resize()
  dishChart?.resize()
}

onMounted(async () => {
  await nextTick()
  await Promise.all([
    loadDashboardStats(),
    loadRecentOrders(),
    loadSalesTrend(),
    loadDishTop()
  ])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  dishChart?.dispose()
})

watch(salesPeriod, () => {
  loadSalesTrend()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .stat-icon {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 5px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .charts-row {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
