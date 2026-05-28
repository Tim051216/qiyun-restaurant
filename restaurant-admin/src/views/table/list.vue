<template>
  <div class="table-manage">
    <el-card>
      <el-button type="primary" @click="handleAdd" style="margin-bottom: 20px;">
        <el-icon><Plus /></el-icon>
        添加桌台
      </el-button>

      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="table in tableList" :key="table.id">
          <el-card class="table-card" :class="'status-' + table.status">
            <div class="table-info">
              <div class="table-no">{{ table.tableNo }}</div>
              <el-tag :type="getStatusType(table.status)">{{ getStatusText(table.status) }}</el-tag>
            </div>
            <div class="table-details">
              <div>容纳人数：{{ table.capacity }}人</div>
              <div v-if="table.status === 'occupied'">就餐人数：{{ table.dinerCount }}人</div>
              <div v-if="table.status === 'occupied'">消费金额：¥{{ table.amount }}</div>
            </div>
            <div class="table-actions">
              <el-button size="small" @click="handleEdit(table)">编辑</el-button>
              <el-button v-if="table.status === 'occupied'" type="danger" size="small" @click="handleCheckout(table)">
                结账
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTableList, updateTableStatus } from '@/api/table'

const loading = ref(false)
const tableList = ref([])

// 加载桌台列表
const loadTableList = async () => {
  loading.value = true
  try {
    const res = await getTableList()
    if (res.code === 1) {
      tableList.value = res.data
    }
  } catch (error) {
    console.error('加载桌台列表失败:', error)
    ElMessage.error('加载桌台列表失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = {
    available: 'success',
    occupied: 'danger',
    reserved: 'warning'
  }
  return map[status]
}

const getStatusText = (status) => {
  const map = {
    available: '空闲',
    occupied: '使用中',
    reserved: '已预订'
  }
  return map[status]
}

const handleAdd = () => {
  ElMessage.info('添加桌台功能')
}

const handleEdit = (table) => {
  ElMessage.info('编辑桌台：' + table.tableNo)
}

const handleCheckout = (table) => {
  ElMessageBox.confirm('确认结账？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await updateTableStatus(table.id, 'available')
      if (res.code === 1) {
        ElMessage.success('结账成功')
        loadTableList()
      }
    } catch (error) {
      console.error('结账失败:', error)
      ElMessage.error('结账失败')
    }
  })
}

onMounted(() => {
  loadTableList()
})
</script>

<style lang="scss" scoped>
.table-manage {
  .table-card {
    margin-bottom: 20px;
    transition: all 0.3s;

    &.status-occupied {
      border-left: 4px solid #F56C6C;
    }

    &.status-available {
      border-left: 4px solid #67C23A;
    }

    &.status-reserved {
      border-left: 4px solid #E6A23C;
    }

    .table-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      .table-no {
        font-size: 24px;
        font-weight: bold;
      }
    }

    .table-details {
      margin-bottom: 15px;
      font-size: 14px;
      color: #606266;

      div {
        margin: 5px 0;
      }
    }

    .table-actions {
      display: flex;
      gap: 10px;
    }
  }
}
</style>
