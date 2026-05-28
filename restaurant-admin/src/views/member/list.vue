<template>
  <div class="member-list">
    <el-card>
      <el-form :inline="true" class="search-form">
        <el-form-item label="会员昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="searchForm.level" placeholder="请选择" clearable>
            <el-option label="全部" value="" />
            <el-option label="V1" value="V1" />
            <el-option label="V2" value="V2" />
            <el-option label="V3" value="V3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="memberList" style="width: 100%" v-loading="loading">
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="level" label="等级" width="80">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column prop="balance" label="余额" width="100">
          <template #default="{ row }">
            ¥{{ row.balance }}
          </template>
        </el-table-column>
        <el-table-column prop="totalConsume" label="累计消费" width="120">
          <template #default="{ row }">
            ¥{{ row.totalConsume }}
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="100" />
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMemberPage, getMemberById } from '@/api/member'

const loading = ref(false)

const searchForm = reactive({
  nickname: '',
  level: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const memberList = ref([])

// 加载会员列表
const loadMemberList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      nickname: searchForm.nickname,
      level: searchForm.level
    }
    
    const res = await getMemberPage(params)
    if (res.code === 1) {
      memberList.value = res.data.records.map(member => ({
        ...member,
        registerTime: member.createTime
      }))
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('加载会员列表失败:', error)
    ElMessage.error('加载会员列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadMemberList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    nickname: '',
    level: ''
  })
  handleSearch()
}

const handleView = async (row) => {
  try {
    const res = await getMemberById(row.id)
    if (res.code === 1) {
      ElMessage.info('查看会员：' + res.data.nickname)
    }
  } catch (error) {
    console.error('加载会员详情失败:', error)
    ElMessage.error('加载会员详情失败')
  }
}

const handleEdit = (row) => {
  ElMessage.info('编辑会员：' + row.nickname)
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadMemberList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadMemberList()
}

onMounted(() => {
  loadMemberList()
})
</script>

<style lang="scss" scoped>
.member-list {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
