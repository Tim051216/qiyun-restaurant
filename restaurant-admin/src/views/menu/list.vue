<template>
  <div class="menu-list">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加菜品
        </el-button>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索菜品名称"
          style="width: 300px;"
          clearable
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <el-table :data="menuList" style="width: 100%" v-loading="loading">
        <el-table-column prop="image" label="图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.image"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜品名称" width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sales" label="销量" width="100" sortable />
        <el-table-column prop="stock" label="库存" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stock > 10 ? 'success' : row.stock > 0 ? 'warning' : 'danger'">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-text="上架"
              inactive-text="下架"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 添加/编辑菜品弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="菜品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入菜品名称" />
        </el-form-item>
        <el-form-item label="菜品分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类">
            <el-option label="川味麻辣风" value="川味麻辣风" />
            <el-option label="家常小炒" value="家常小炒" />
            <el-option label="时蔬素菜" value="时蔬素菜" />
            <el-option label="汤品主食" value="汤品主食" />
            <el-option label="夜宵烧烤" value="夜宵烧烤" />
            <el-option label="特色龙虾" value="特色龙虾" />
            <el-option label="饮品酒水" value="饮品酒水" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" />
        </el-form-item>
        <el-form-item label="菜品描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入菜品描述"
          />
        </el-form-item>
        <el-form-item label="菜品图片" prop="image">
          <el-input v-model="formData.image" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加菜品')
const searchKeyword = ref('')
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 30
})

const formData = reactive({
  name: '',
  category: '',
  price: 0,
  stock: 0,
  description: '',
  image: '',
  status: true
})

const rules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const menuList = ref([
  {
    id: 1,
    name: '宫保鸡丁',
    category: '川味麻辣风',
    price: 38,
    description: '经典川菜，鸡肉鲜嫩，花生酥脆',
    image: '/static/menu/menulist/gbyd.png',
    sales: 235,
    stock: 50,
    status: true
  },
  {
    id: 2,
    name: '麻婆豆腐',
    category: '川味麻辣风',
    price: 28,
    description: '麻辣鲜香，豆腐嫩滑',
    image: '/static/menu/menulist/mpdf.png',
    sales: 198,
    stock: 30,
    status: true
  },
  {
    id: 3,
    name: '水煮鱼',
    category: '川味麻辣风',
    price: 68,
    description: '鱼肉鲜嫩，麻辣过瘾',
    image: '/static/menu/menulist/slt.png',
    sales: 186,
    stock: 5,
    status: true
  }
])

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleAdd = () => {
  dialogTitle.value = '添加菜品'
  Object.assign(formData, {
    name: '',
    category: '',
    price: 0,
    stock: 0,
    description: '',
    image: '',
    status: true
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑菜品'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该菜品？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = menuList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      menuList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

const handleStatusChange = (row) => {
  ElMessage.success(row.status ? '已上架' : '已下架')
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      dialogVisible.value = false
      ElMessage.success('保存成功')
    }
  })
}
</script>

<style lang="scss" scoped>
.menu-list {
  .toolbar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
  }
}
</style>
