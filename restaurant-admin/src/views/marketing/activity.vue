<template>
  <div class="activity-manage">
    <el-card>
      <el-button type="primary" @click="handleAdd" style="margin-bottom: 20px;">
        <el-icon><Plus /></el-icon>
        添加活动
      </el-button>

      <el-table :data="activityList" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="活动名称" width="200" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="participants" label="参与人数" width="120" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivityList, deleteActivity } from '@/api/activity'

const loading = ref(false)
const activityList = ref([])

// 加载活动列表
const loadActivityList = async () => {
  loading.value = true
  try {
    const res = await getActivityList()
    if (res.code === 1) {
      activityList.value = res.data.map(activity => ({
        ...activity,
        status: getStatusText(activity.status)
      }))
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = {
    '进行中': 'success',
    '即将开始': 'warning',
    '已结束': 'info'
  }
  return map[status]
}

const getStatusText = (status) => {
  const map = {
    'ongoing': '进行中',
    'upcoming': '即将开始',
    'ended': '已结束'
  }
  return map[status] || status
}

const handleAdd = () => {
  ElMessage.info('添加活动功能')
}

const handleEdit = (row) => {
  ElMessage.info('编辑活动：' + row.title)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该活动？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteActivity(row.id)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        loadActivityList()
      }
    } catch (error) {
      console.error('删除活动失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  loadActivityList()
})
</script>
