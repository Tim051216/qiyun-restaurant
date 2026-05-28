<template>
  <div class="menu-list">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加菜品
        </el-button>
        <div style="display: flex; gap: 10px;">
          <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable style="width: 150px;" @change="getDishList">
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
          <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px;" @change="getDishList">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
          <el-input
            v-model="searchForm.name"
            placeholder="搜索菜品名称"
            style="width: 300px;"
            clearable
            @change="getDishList"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="菜品名称" width="180" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sales" label="销量" width="100" sortable />
        <el-table-column prop="stock" label="库存" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stock > 100 ? 'success' : row.stock > 0 ? 'warning' : 'danger'">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text="上架"
              inactive-text="下架"
              @change="handleStatusChange(row)"
            />
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

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="getDishList"
        @current-change="getDishList"
      />
    </el-card>

    <!-- 添加/编辑菜品弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="dishForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="dishForm.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品分类" prop="categoryId">
          <el-select v-model="dishForm.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="dishForm.price" :min="0" :precision="2" :step="0.5" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="dishForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="dishForm.description" type="textarea" :rows="3" placeholder="请输入菜品描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dishForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getDishPage, getCategoryList, addDish, updateDish, deleteDish, updateDishStatus } from '@/api/dish'

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: '',
  status: ''
})

// 分类列表
const categoryList = ref([])

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加菜品')
const submitLoading = ref(false)
const formRef = ref(null)
const dishForm = reactive({
  id: null,
  name: '',
  categoryId: '',
  price: 0,
  image: '',
  description: '',
  stock: 999,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

// 获取分类列表
const loadCategoryList = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取菜品列表
const getDishList = async () => {
  loading.value = true
  try {
    const res = await getDishPage({
      page: currentPage.value,
      size: pageSize.value,
      name: searchForm.name || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取菜品列表失败:', error)
    ElMessage.error('获取菜品列表失败')
  } finally {
    loading.value = false
  }
}

// 添加菜品
const handleAdd = () => {
  dialogTitle.value = '添加菜品'
  Object.assign(dishForm, {
    id: null,
    name: '',
    categoryId: '',
    price: 0,
    image: '',
    description: '',
    stock: 999,
    status: 1
  })
  dialogVisible.value = true
}

// 编辑菜品
const handleEdit = (row) => {
  dialogTitle.value = '编辑菜品'
  Object.assign(dishForm, {
    id: row.id,
    name: row.name,
    categoryId: row.categoryId,
    price: row.price,
    image: row.image || '',
    description: row.description || '',
    stock: row.stock,
    status: row.status
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dishForm.id) {
          await updateDish(dishForm)
          ElMessage.success('更新成功')
        } else {
          await addDish(dishForm)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        getDishList()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除菜品
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该菜品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDish(row.id)
      ElMessage.success('删除成功')
      getDishList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 更新状态
const handleStatusChange = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateDishStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    getDishList()
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
  }
}

onMounted(() => {
  loadCategoryList()
  getDishList()
})
</script>

<style scoped>
.menu-list {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>
