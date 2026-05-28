<template>
  <view class="uni-container">
    <uni-forms ref="form" :model="formData" validateTrigger="bind">
      <uni-forms-item name="table_number" label="桌号" required>
        <uni-easyinput v-model="formData.table_number" placeholder="请输入桌号" trim="both"></uni-easyinput>
      </uni-forms-item>
      <uni-forms-item name="qrcode_image" label="小程序码" required>
        <uni-easyinput v-model="formData.qrcode_image" placeholder="请输入小程序码图片 URL"></uni-easyinput>
      </uni-forms-item>
      <view class="uni-button-group">
        <button type="primary" class="uni-button" style="width: 100px;" @click="submit">提交</button>
        <navigator open-type="navigateBack" style="margin-left: 15px;">
          <button class="uni-button" style="width: 100px;">返回</button>
        </navigator>
      </view>
    </uni-forms>
  </view>
</template>

<script>
import { validator } from '../../js_sdk/validator/zhuohao.js'

const db = uniCloud.database()
const dbCollectionName = 'zhuohao'

function getValidator(fields) {
  const result = {}
  for (const key in validator) {
    if (fields.includes(key)) {
      result[key] = validator[key]
    }
  }
  return result
}

export default {
  data() {
    const formData = {
      table_number: '',
      qrcode_image: ''
    }
    return {
      formData,
      rules: {
        ...getValidator(Object.keys(formData))
      }
    }
  },
  onReady() {
    this.$refs.form.setRules(this.rules)
  },
  methods: {
    submit() {
      uni.showLoading({ mask: true })
      this.$refs.form.validate()
        .then((res) => this.submitForm(res))
        .catch(() => {})
        .finally(() => {
          uni.hideLoading()
        })
    },
    submitForm(value) {
      return db.collection(dbCollectionName).add(value)
        .then(() => {
          uni.showToast({ title: '新增成功' })
          this.getOpenerEventChannel().emit('refreshData')
          setTimeout(() => uni.navigateBack(), 500)
        })
        .catch((err) => {
          uni.showModal({
            content: err.message || '请求服务失败',
            showCancel: false
          })
        })
    }
  }
}
</script>
