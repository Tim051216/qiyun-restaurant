<template>
  <div class="system-settings">
    <el-card>
      <template #header>基本设置</template>
      <el-form :model="settingsForm" label-width="120px">
        <el-form-item label="餐厅名称">
          <el-input v-model="settingsForm.name" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="settingsForm.phone" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-time-picker
            v-model="settingsForm.businessHours"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="餐厅地址">
          <el-input v-model="settingsForm.address" />
        </el-form-item>
        <el-form-item label="餐厅简介">
          <el-input
            v-model="settingsForm.description"
            type="textarea"
            :rows="4"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const settingsForm = reactive({
  name: '七云菜馆',
  phone: '400-888-8888',
  businessHours: [],
  address: '北京市朝阳区xxx街道xxx号',
  description: '七云菜馆是一家以川菜为主的特色餐厅，提供优质的菜品和服务。'
})

const handleSave = () => {
  ElMessage.success('保存成功')
}
</script>
