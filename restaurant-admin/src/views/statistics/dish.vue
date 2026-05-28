<template>
  <div class="dish-statistics">
    <el-card>
      <template #header>菜品销量统计</template>
      <div ref="chartRef" style="height: 400px;"></div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>菜品销量排行</template>
      <el-table :data="dishRanking" style="width: 100%">
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="name" label="菜品名称" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="sales" label="销量" width="100" />
        <el-table-column prop="amount" label="销售额" width="120">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)

const dishRanking = ref([
  { rank: 1, name: '宫保鸡丁', category: '川味麻辣风', sales: 235, amount: 8930 },
  { rank: 2, name: '麻婆豆腐', category: '川味麻辣风', sales: 198, amount: 5544 },
  { rank: 3, name: '水煮鱼', category: '川味麻辣风', sales: 186, amount: 12648 },
  { rank: 4, name: '红烧肉', category: '家常小炒', sales: 165, amount: 7425 },
  { rank: 5, name: '麻辣小龙虾', category: '特色龙虾', sales: 142, amount: 14200 }
])

let chart = null

const initChart = () => {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: [
          { value: 235, name: '宫保鸡丁' },
          { value: 198, name: '麻婆豆腐' },
          { value: 186, name: '水煮鱼' },
          { value: 165, name: '红烧肉' },
          { value: 142, name: '麻辣小龙虾' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  chart.setOption(option)
}

onMounted(() => {
  initChart()
  
  window.addEventListener('resize', () => {
    chart?.resize()
  })
})
</script>
