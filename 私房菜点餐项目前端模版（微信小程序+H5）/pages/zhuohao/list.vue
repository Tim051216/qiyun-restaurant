<template>
  <view>
    <view class="uni-header">
      <view class="uni-group">
        <view class="uni-title"></view>
        <view class="uni-sub-title"></view>
      </view>
      <view class="uni-group">
        <input class="uni-search" type="text" v-model="query" @confirm="search" placeholder="请输入搜索内容" />
        <button class="uni-button" type="default" size="mini" @click="search">搜索</button>
        <button class="uni-button" type="default" size="mini" @click="navigateTo('./add')">新增</button>
        <button class="uni-button" type="default" size="mini" :disabled="!selectedIndexs.length" @click="delTable">批量删除</button>
        <download-excel class="hide-on-phone" :fields="exportExcel.fields" :data="exportExcelData" :type="exportExcel.type" :name="exportExcel.filename">
          <button class="uni-button" type="primary" size="mini">导出 Excel</button>
        </download-excel>
      </view>
    </view>
    <view class="uni-container">
      <unicloud-db
        ref="udb"
        :collection="collectionList"
        field="table_number,qrcode_image"
        :where="where"
        page-data="replace"
        :orderby="orderby"
        :getcount="true"
        :page-size="options.pageSize"
        :page-current="options.pageCurrent"
        :options="options"
        loadtime="manual"
        v-slot:default="{ data, pagination, loading, error }"
        @load="onqueryload"
      >
        <uni-table ref="table" :loading="loading" :emptyText="(error && error.message) || '没有更多数据'" border stripe type="selection" @selection-change="selectionChange">
          <uni-tr>
            <uni-th align="center" filter-type="search" @filter-change="filterChange($event, 'table_number')" sortable @sort-change="sortChange($event, 'table_number')">桌号</uni-th>
            <uni-th align="center" filter-type="search" @filter-change="filterChange($event, 'qrcode_image')" sortable @sort-change="sortChange($event, 'qrcode_image')">小程序码</uni-th>
            <uni-th align="center">操作</uni-th>
          </uni-tr>
          <uni-tr v-for="(item, index) in data" :key="index">
            <uni-td align="center">{{ item.table_number }}</uni-td>
            <uni-td align="center">{{ item.qrcode_image }}</uni-td>
            <uni-td align="center">
              <view class="uni-group">
                <button class="uni-button" size="mini" type="primary" @click="navigateTo('./edit?id=' + item._id, false)">修改</button>
                <button class="uni-button" size="mini" type="warn" @click="confirmDelete(item._id)">删除</button>
              </view>
            </uni-td>
          </uni-tr>
        </uni-table>
        <view class="uni-pagination-box">
          <uni-pagination show-icon :page-size="pagination.size" v-model="pagination.current" :total="pagination.count" @change="onPageChanged" />
        </view>
      </unicloud-db>
    </view>
  </view>
</template>

<script>
import { enumConverter, filterToWhere } from '../../js_sdk/validator/zhuohao.js'

const db = uniCloud.database()
const dbOrderBy = ''
const dbSearchFields = []
const pageSize = 20
const pageCurrent = 1

const orderByMapping = {
  ascending: 'asc',
  descending: 'desc'
}

export default {
  data() {
    return {
      collectionList: 'zhuohao',
      query: '',
      where: '',
      orderby: dbOrderBy,
      orderByFieldName: '',
      selectedIndexs: [],
      options: {
        pageSize,
        pageCurrent,
        filterData: {},
        ...enumConverter
      },
      exportExcel: {
        filename: 'zhuohao.xls',
        type: 'xls',
        fields: {
          桌号: 'table_number',
          小程序码: 'qrcode_image'
        }
      },
      exportExcelData: []
    }
  },
  onLoad() {
    this._filter = {}
  },
  onReady() {
    this.$refs.udb.loadData()
  },
  methods: {
    onqueryload(data) {
      this.exportExcelData = data
    },
    getWhere() {
      const query = this.query.trim()
      if (!query) return ''
      const queryRe = new RegExp(query, 'i')
      return dbSearchFields.map(name => `${queryRe}.test(${name})`).join(' || ')
    },
    search() {
      this.where = this.getWhere()
      this.$nextTick(() => {
        this.loadData()
      })
    },
    loadData(clear = true) {
      this.$refs.udb.loadData({ clear })
    },
    onPageChanged(e) {
      this.selectedIndexs.length = 0
      this.$refs.table.clearSelection()
      this.$refs.udb.loadData({ current: e.current })
    },
    navigateTo(url, clear = true) {
      uni.navigateTo({
        url,
        events: {
          refreshData: () => {
            this.loadData(clear)
          }
        }
      })
    },
    selectedItems() {
      const dataList = this.$refs.udb.dataList
      return this.selectedIndexs.map(i => dataList[i]._id)
    },
    delTable() {
      this.$refs.udb.remove(this.selectedItems(), {
        success: () => {
          this.$refs.table.clearSelection()
        }
      })
    },
    selectionChange(e) {
      this.selectedIndexs = e.detail.index
    },
    confirmDelete(id) {
      this.$refs.udb.remove(id, {
        success: () => {
          this.$refs.table.clearSelection()
        }
      })
    },
    sortChange(e, name) {
      this.orderByFieldName = name
      this.orderby = e.order ? `${name} ${orderByMapping[e.order]}` : ''
      this.$refs.table.clearSelection()
      this.$nextTick(() => {
        this.$refs.udb.loadData()
      })
    },
    filterChange(e, name) {
      this._filter[name] = {
        type: e.filterType,
        value: e.filter
      }
      const newWhere = filterToWhere(this._filter, db.command)
      this.where = Object.keys(newWhere).length ? newWhere : ''
      this.$nextTick(() => {
        this.$refs.udb.loadData()
      })
    }
  }
}
</script>

<style>
</style>
