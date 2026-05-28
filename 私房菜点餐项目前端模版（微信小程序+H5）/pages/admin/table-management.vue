<template>
	<view class="container">
		<!-- 顶部操作栏 -->
		<view class="header">
			<view class="title">桌号管理</view>
			<view class="actions">
				<button class="btn btn-primary" @click="showAddDialog = true">添加桌号</button>
				<button class="btn btn-success" @click="showBatchDialog = true">批量生成</button>
			</view>
		</view>

		<!-- 桌号列表 -->
		<view class="table-list">
			<view v-if="loading" class="loading">
				<text>加载中...</text>
			</view>

			<view v-else-if="tableList.length === 0" class="empty">
				<text>暂无桌号数据</text>
				<text class="empty-tip">点击上方"添加桌号"开始创建</text>
			</view>

			<view v-else class="list-content">
				<view v-for="item in tableList" :key="item._id" class="table-item">
					<view class="item-left">
						<image :src="item.qrcode_image" class="qrcode-image" mode="aspectFit" @click="previewQRCode(item)"></image>
					</view>
					<view class="item-center">
						<view class="table-number">{{ item.table_number }}</view>
						<view class="create-time">创建时间：{{ formatTime(item.create_time) }}</view>
					</view>
					<view class="item-right">
						<button class="btn-mini btn-primary" @click="downloadQRCode(item)">下载</button>
						<button class="btn-mini btn-danger" @click="deleteTable(item)">删除</button>
					</view>
				</view>
			</view>
		</view>

		<!-- 分页 -->
		<view class="pagination" v-if="total > 0">
			<button class="btn-page" :disabled="pageNum === 1" @click="prevPage">上一页</button>
			<text class="page-info">{{ pageNum }} / {{ totalPages }}</text>
			<button class="btn-page" :disabled="pageNum >= totalPages" @click="nextPage">下一页</button>
		</view>

		<!-- 添加桌号弹窗 -->
		<view v-if="showAddDialog" class="modal-mask" @click="showAddDialog = false">
			<view class="modal-content" @click.stop>
				<view class="dialog">
					<view class="dialog-title">添加桌号</view>
					<view class="dialog-content">
						<input class="input" v-model="newTableNumber" placeholder="请输入桌号（如：桌1）" />
					</view>
					<view class="dialog-actions">
						<button class="btn btn-default" @click="showAddDialog = false">取消</button>
						<button class="btn btn-primary" @click="addTable" :disabled="generating">
							{{ generating ? '生成中...' : '确定' }}
						</button>
					</view>
				</view>
			</view>
		</view>

		<!-- 批量生成弹窗 -->
		<view v-if="showBatchDialog" class="modal-mask" @click="showBatchDialog = false">
			<view class="modal-content" @click.stop>
				<view class="dialog">
					<view class="dialog-title">批量生成桌号</view>
					<view class="dialog-content">
						<view class="form-item">
							<text class="label">前缀：</text>
							<input class="input" v-model="batchPrefix" placeholder="如：桌" />
						</view>
						<view class="form-item">
							<text class="label">起始号：</text>
							<input class="input" v-model="batchStart" type="number" placeholder="如：1" />
						</view>
						<view class="form-item">
							<text class="label">结束号：</text>
							<input class="input" v-model="batchEnd" type="number" placeholder="如：10" />
						</view>
					</view>
					<view class="dialog-actions">
						<button class="btn btn-default" @click="showBatchDialog = false">取消</button>
						<button class="btn btn-primary" @click="batchGenerate" :disabled="generating">
							{{ generating ? '生成中...' : '生成' }}
						</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			loading: false,
			generating: false,
			tableList: [],
			total: 0,
			pageSize: 10,
			pageNum: 1,
			
			// 添加桌号
			showAddDialog: false,
			newTableNumber: '',
			
			// 批量生成
			showBatchDialog: false,
			batchPrefix: '桌',
			batchStart: '1',
			batchEnd: '10'
		}
	},
	
	computed: {
		totalPages() {
			return Math.ceil(this.total / this.pageSize)
		}
	},
	
	onLoad() {
		this.loadTableList()
	},
	
	methods: {
		// 加载桌号列表
		async loadTableList() {
			this.loading = true
			try {
				const zhuohao = uniCloud.importObject('zhuohao')
				const result = await zhuohao.getTableList(this.pageSize, this.pageNum)
				
				if (result.success) {
					this.tableList = result.data.list
					this.total = result.data.total
				} else {
					uni.showToast({
						title: result.errMsg || '加载失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('加载桌号列表失败:', error)
				uni.showToast({
					title: '加载失败，请重试',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},
		
		// 添加单个桌号
		async addTable() {
			if (!this.newTableNumber || this.newTableNumber.trim() === '') {
				uni.showToast({
					title: '请输入桌号',
					icon: 'none'
				})
				return
			}
			
			this.generating = true
			try {
				const zhuohao = uniCloud.importObject('zhuohao')
				const result = await zhuohao.generateQRCode(this.newTableNumber.trim())
				
				if (result.success) {
					uni.showToast({
						title: '生成成功',
						icon: 'success'
					})
					this.showAddDialog = false
					this.newTableNumber = ''
					this.loadTableList()
				} else {
					uni.showToast({
						title: result.errMsg || '生成失败',
						icon: 'none'
					})
				}
			} catch (error) {
				console.error('生成桌号失败:', error)
				uni.showToast({
					title: '生成失败，请重试',
					icon: 'none'
				})
			} finally {
				this.generating = false
			}
		},
		
		// 批量生成桌号
		async batchGenerate() {
			const start = Number.parseInt(this.batchStart)
			const end = Number.parseInt(this.batchEnd)
			
			if (Number.isNaN(start) || Number.isNaN(end)) {
				uni.showToast({
					title: '请输入有效的数字',
					icon: 'none'
				})
				return
			}
			
			if (start > end) {
				uni.showToast({
					title: '起始号不能大于结束号',
					icon: 'none'
				})
				return
			}
			
			if (end - start > 50) {
				uni.showToast({
					title: '一次最多生成50个桌号',
					icon: 'none'
				})
				return
			}
			
			this.generating = true
			uni.showLoading({
				title: '批量生成中...'
			})
			
			try {
				const zhuohao = uniCloud.importObject('zhuohao')
				let successCount = 0
				let failCount = 0
				
				for (let i = start; i <= end; i++) {
					const tableNumber = `${this.batchPrefix}${i}`
					try {
						const result = await zhuohao.generateQRCode(tableNumber)
						if (result.success) {
							successCount++
						} else {
							failCount++
							console.log(`${tableNumber} 生成失败:`, result.errMsg)
						}
					} catch (error) {
						failCount++
						console.error(`${tableNumber} 生成失败:`, error)
					}
				}
				
				uni.hideLoading()
				uni.showToast({
					title: `成功${successCount}个，失败${failCount}个`,
					icon: 'none',
					duration: 2000
				})
				
				this.showBatchDialog = false
				this.loadTableList()
			} catch (error) {
				uni.hideLoading()
				console.error('批量生成失败:', error)
				uni.showToast({
					title: '批量生成失败',
					icon: 'none'
				})
			} finally {
				this.generating = false
			}
		},
		
		// 删除桌号
		deleteTable(item) {
			uni.showModal({
				title: '确认删除',
				content: `确定要删除 ${item.table_number} 吗？`,
				success: async (res) => {
					if (res.confirm) {
						try {
							const zhuohao = uniCloud.importObject('zhuohao')
							const result = await zhuohao.deleteTable(item.table_number)
							
							if (result.success) {
								uni.showToast({
									title: '删除成功',
									icon: 'success'
								})
								this.loadTableList()
							} else {
								uni.showToast({
									title: result.errMsg || '删除失败',
									icon: 'none'
								})
							}
						} catch (error) {
							console.error('删除失败:', error)
							uni.showToast({
								title: '删除失败，请重试',
								icon: 'none'
							})
						}
					}
				}
			})
		},
		
		// 预览小程序码
		previewQRCode(item) {
			uni.previewImage({
				urls: [item.qrcode_image],
				current: item.qrcode_image
			})
		},
		
		// 下载小程序码
		downloadQRCode(item) {
			uni.showLoading({
				title: '下载中...'
			})
			
			uni.downloadFile({
				url: item.qrcode_image,
				success: (res) => {
					if (res.statusCode === 200) {
						uni.saveImageToPhotosAlbum({
							filePath: res.tempFilePath,
							success: () => {
								uni.hideLoading()
								uni.showToast({
									title: '保存成功',
									icon: 'success'
								})
							},
							fail: () => {
								uni.hideLoading()
								uni.showToast({
									title: '保存失败',
									icon: 'none'
								})
							}
						})
					}
				},
				fail: () => {
					uni.hideLoading()
					uni.showToast({
						title: '下载失败',
						icon: 'none'
					})
				}
			})
		},
		
		// 格式化时间
		formatTime(timestamp) {
			if (!timestamp) return ''
			const date = new Date(timestamp)
			const year = date.getFullYear()
			const month = String(date.getMonth() + 1).padStart(2, '0')
			const day = String(date.getDate()).padStart(2, '0')
			const hour = String(date.getHours()).padStart(2, '0')
			const minute = String(date.getMinutes()).padStart(2, '0')
			return `${year}-${month}-${day} ${hour}:${minute}`
		},
		
		// 上一页
		prevPage() {
			if (this.pageNum > 1) {
				this.pageNum--
				this.loadTableList()
			}
		},
		
		// 下一页
		nextPage() {
			if (this.pageNum < this.totalPages) {
				this.pageNum++
				this.loadTableList()
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding: 20rpx;
}

.header {
	background-color: white;
	padding: 30rpx;
	border-radius: 10rpx;
	margin-bottom: 20rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	
	.title {
		font-size: 36rpx;
		font-weight: bold;
	}
	
	.actions {
		display: flex;
		gap: 10rpx;
	}
}

.btn {
	padding: 10rpx 20rpx;
	border-radius: 8rpx;
	font-size: 28rpx;
	border: none;
	color: white;
	
	&.btn-primary {
		background-color: #2979ff;
	}
	
	&.btn-success {
		background-color: #19be6b;
	}
	
	&.btn-default {
		background-color: #909399;
	}
	
	&.btn-danger {
		background-color: #fa3534;
	}
}

.btn-mini {
	padding: 8rpx 16rpx;
	border-radius: 6rpx;
	font-size: 24rpx;
	border: none;
	color: white;
	margin-top: 10rpx;
	
	&.btn-primary {
		background-color: #2979ff;
	}
	
	&.btn-danger {
		background-color: #fa3534;
	}
}

.btn-page {
	padding: 8rpx 20rpx;
	border-radius: 6rpx;
	font-size: 26rpx;
	background-color: white;
	border: 1px solid #dcdfe6;
	
	&:disabled {
		opacity: 0.5;
	}
}

.table-list {
	background-color: white;
	border-radius: 10rpx;
	padding: 20rpx;
	min-height: 600rpx;
}

.loading {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
	color: #999;
}

.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
	color: #999;
	
	.empty-tip {
		margin-top: 20rpx;
		font-size: 24rpx;
		color: #c0c4cc;
	}
}

.table-item {
	display: flex;
	align-items: center;
	padding: 20rpx;
	border-bottom: 1px solid #f0f0f0;
	
	&:last-child {
		border-bottom: none;
	}
	
	.item-left {
		.qrcode-image {
			width: 120rpx;
			height: 120rpx;
			border-radius: 8rpx;
			border: 1px solid #e0e0e0;
		}
	}
	
	.item-center {
		flex: 1;
		margin-left: 20rpx;
		
		.table-number {
			font-size: 32rpx;
			font-weight: bold;
			margin-bottom: 10rpx;
		}
		
		.create-time {
			font-size: 24rpx;
			color: #999;
		}
	}
	
	.item-right {
		display: flex;
		flex-direction: column;
	}
}

.pagination {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 30rpx 0;
	
	.page-info {
		margin: 0 30rpx;
		font-size: 28rpx;
	}
}

.modal-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 999;
}

.modal-content {
	width: 80%;
	max-width: 600rpx;
}

.dialog {
	background-color: white;
	padding: 40rpx;
	border-radius: 20rpx;
	
	.dialog-title {
		font-size: 36rpx;
		font-weight: bold;
		text-align: center;
		margin-bottom: 30rpx;
	}
	
	.dialog-content {
		margin-bottom: 30rpx;
		
		.input {
			width: 100%;
			padding: 20rpx;
			border: 1px solid #dcdfe6;
			border-radius: 8rpx;
			font-size: 28rpx;
		}
		
		.form-item {
			display: flex;
			align-items: center;
			margin-bottom: 20rpx;
			
			.label {
				width: 120rpx;
				font-size: 28rpx;
			}
			
			.input {
				flex: 1;
			}
		}
	}
	
	.dialog-actions {
		display: flex;
		justify-content: space-around;
		gap: 20rpx;
		
		.btn {
			flex: 1;
		}
	}
}
</style>
