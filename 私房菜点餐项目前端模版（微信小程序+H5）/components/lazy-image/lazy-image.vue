<template>
	<view class="lazy-image-wrapper" :style="wrapperStyle">
		<image 
			v-if="showImage"
			:src="currentSrc" 
			:mode="mode"
			:lazy-load="true"
			:class="imageClass"
			:style="imageStyle"
			@load="onImageLoad"
			@error="onImageError"
		/>
		<view v-else class="lazy-image-placeholder" :style="placeholderStyle">
			<view class="loading-icon">📷</view>
		</view>
		<view v-if="showError" class="lazy-image-error" :style="placeholderStyle">
			<view class="error-icon">❌</view>
			<view class="error-text">加载失败</view>
		</view>
	</view>
</template>

<script>
	/**
	 * 懒加载图片组件
	 * 支持占位图、加载失败处理、自动压缩
	 */
	export default {
		name: 'LazyImage',
		props: {
			// 图片地址
			src: {
				type: String,
				required: true
			},
			// 图片裁剪、缩放的模式
			mode: {
				type: String,
				default: 'aspectFill'
			},
			// 宽度
			width: {
				type: [String, Number],
				default: '100%'
			},
			// 高度
			height: {
				type: [String, Number],
				default: 'auto'
			},
			// 圆角
			borderRadius: {
				type: [String, Number],
				default: 0
			},
			// 占位图背景色
			placeholderBg: {
				type: String,
				default: '#f5f5f5'
			},
			// 是否启用压缩（云存储图片）
			compress: {
				type: Boolean,
				default: true
			},
			// 压缩质量 (1-100)
			quality: {
				type: Number,
				default: 80
			},
			// 自定义类名
			customClass: {
				type: String,
				default: ''
			}
		},
		data() {
			return {
				showImage: false,
				showError: false,
				currentSrc: '',
				isLoaded: false
			}
		},
		computed: {
			wrapperStyle() {
				return {
					width: this.formatSize(this.width),
					height: this.formatSize(this.height),
					borderRadius: this.formatSize(this.borderRadius)
				}
			},
			imageStyle() {
				return {
					width: '100%',
					height: '100%',
					borderRadius: this.formatSize(this.borderRadius)
				}
			},
			placeholderStyle() {
				return {
					width: '100%',
					height: '100%',
					backgroundColor: this.placeholderBg,
					borderRadius: this.formatSize(this.borderRadius)
				}
			},
			imageClass() {
				return `lazy-image ${this.customClass} ${this.isLoaded ? 'loaded' : ''}`
			}
		},
		watch: {
			src: {
				handler(newVal) {
					if (newVal) {
						this.loadImage()
					}
				},
				immediate: true
			}
		},
		methods: {
			/**
			 * 加载图片
			 */
			loadImage() {
				this.showError = false
				this.showImage = false
				
				// 处理图片地址（压缩、CDN 等）
				this.currentSrc = this.processImageUrl(this.src)
				
				// 延迟显示图片，给占位图一点时间
				setTimeout(() => {
					this.showImage = true
				}, 50)
			},
			
			/**
			 * 处理图片 URL（添加压缩参数等）
			 */
			processImageUrl(url) {
				if (!url) return ''
				
				// 如果是云存储图片且启用压缩
				if (this.compress && url.includes('unicloud')) {
					// uniCloud 云存储支持图片处理参数
					// 格式：原图URL?x-oss-process=image/quality,q_80/resize,w_800
					const separator = url.includes('?') ? '&' : '?'
					return `${url}${separator}x-oss-process=image/quality,q_${this.quality}`
				}
				
				return url
			},
			
			/**
			 * 格式化尺寸
			 */
			formatSize(size) {
				if (typeof size === 'number') {
					return `${size}rpx`
				}
				return size
			},
			
			/**
			 * 图片加载成功
			 */
			onImageLoad() {
				this.isLoaded = true
				this.$emit('load')
			},
			
			/**
			 * 图片加载失败
			 */
			onImageError(e) {
				console.error('[LazyImage] 图片加载失败:', this.src, e)
				this.showError = true
				this.showImage = false
				this.$emit('error', e)
			}
		}
	}
</script>

<style lang="scss" scoped>
	.lazy-image-wrapper {
		position: relative;
		overflow: hidden;
		display: inline-block;
	}
	
	.lazy-image {
		display: block;
		opacity: 0;
		transition: opacity 0.3s ease-in-out;
		
		&.loaded {
			opacity: 1;
		}
	}
	
	.lazy-image-placeholder {
		display: flex;
		align-items: center;
		justify-content: center;
		
		.loading-icon {
			font-size: 60rpx;
			opacity: 0.3;
			animation: pulse 1.5s ease-in-out infinite;
		}
	}
	
	.lazy-image-error {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		
		.error-icon {
			font-size: 60rpx;
			opacity: 0.3;
			margin-bottom: 10rpx;
		}
		
		.error-text {
			font-size: 24rpx;
			color: #999;
		}
	}
	
	@keyframes pulse {
		0%, 100% {
			opacity: 0.3;
		}
		50% {
			opacity: 0.6;
		}
	}
</style>
