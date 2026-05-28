<template>
  <div class="sales-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>销售统计</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateChange"
          />
        </div>
      </template>
      <div ref="chartRef" style="height: 400px;"></div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>销售额排行</template>
          <el-table :data="salesRanking" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="amount" label="销售额">
              <template #default="{ row }">
                <span style="color: #F56C6C; font-weight: bold;">¥{{ row.amount }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>订单量排行</template>
          <el-table :data="orderRanking" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80" />
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="count" label="订单量">
              <template #default="{ row }">
                <span style="color: #409EFF; font-weight: bold;">{{ row.count }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const dateRange = ref([])
const chartRef = ref(null)

const salesRanking = ref([
  { rank: 1, date: '2024-02-07', amount: 15600 },
  { rank: 2, date: '2024-02-06', amount: 14200 },
  { rank: 3, date: '2024-02-05', amount: 13500 }
])

const orderRanking = ref([
  { rank: 1, date: '2024-02-07', count: 156 },
  { rank: 2, date: '2024-02-05', count: 142 },
  { rank: 3, date: '2024-02-06', count: 135 }
])

let chart = null

const initChart = () => {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['销售额', '订单量']
    },
    xAxis: {
      type: 'category',
      data: ['2月1日', '2月2日', '2月3日', '2月4日', '2月5日', '2月6日', '2月7日']
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
        data: [8200, 9500, 11200, 10800, 13500, 14200, 15600],
        itemStyle: { color: '#F56C6C' }
      },
      {
        name: '订单量',
        type: 'bar',
        yAxisIndex: 1,
        data: [85, 98, 115, 108, 135, 142, 156],
        itemStyle: { color: '#409EFF' }
      }
    ]
  }
  
  chart.setOption(option)
}

const handleDateChange = () => {
  initChart()
}

onMounted(() => {
  initChart()
  
  window.addEventListener('resize', () => {
    chart?.resize()
  })
})
</script>

<style lang="scss" scoped>
.sales-statistics {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
