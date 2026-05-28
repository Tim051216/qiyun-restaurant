<template>
  <div class="category-manage">
    <el-card>
      <el-button type="primary" @click="handleAdd" style="margin-bottom: 20px;">
        <el-icon><Plus /></el-icon>
        添加分类
      </el-button>

      <el-table :data="categoryList" style="width: 100%">
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="dishCount" label="菜品数量" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
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

const categoryList = ref([
  { id: 1, name: '川味麻辣风', sort: 1, dishCount: 8, status: true },
  { id: 2, name: '家常小炒', sort: 2, dishCount: 9, status: true },
  { id: 3, name: '时蔬素菜', sort: 3, dishCount: 9, status: true },
  { id: 4, name: '汤品主食', sort: 4, dishCount: 10, status: true },
  { id: 5, name: '夜宵烧烤', sort: 5, dishCount: 12, status: true },
  { id: 6, name: '特色龙虾', sort: 6, dishCount: 9, status: true },
  { id: 7, name: '饮品酒水', sort: 7, dishCount: 12, status: true }
])

const handleAdd = () => {
  ElMessage.info('添加分类功能')
}

const handleEdit = (row) => {
  ElMessage.info('编辑分类：' + row.name)
}

const handleDelete = (row) => {
  ElMessage.info('删除分类：' + row.name)
}
</script>
