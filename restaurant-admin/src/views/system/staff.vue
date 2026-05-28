<template>
  <div class="staff-manage">
    <el-card>
      <el-button type="primary" @click="handleAdd" style="margin-bottom: 20px;">
        <el-icon><Plus /></el-icon>
        添加员工
      </el-button>

      <el-table :data="staffList" style="width: 100%">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="role" label="角色" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
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

const staffList = ref([
  { id: 1, name: '张三', username: 'admin', role: '管理员', phone: '138****8888', status: true, createTime: '2024-01-01 10:00' },
  { id: 2, name: '李四', username: 'waiter01', role: '服务员', phone: '139****6666', status: true, createTime: '2024-01-05 14:30' },
  { id: 3, name: '王五', username: 'chef01', role: '厨师', phone: '136****9999', status: true, createTime: '2024-01-10 09:00' }
])

const handleAdd = () => {
  ElMessage.info('添加员工功能')
}

const handleEdit = (row) => {
  ElMessage.info('编辑员工：' + row.name)
}

const handleDelete = (row) => {
  ElMessage.info('删除员工：' + row.name)
}
</script>
