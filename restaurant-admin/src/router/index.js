import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据概览', icon: 'DataAnalysis' }
      }
    ]
  },
  {
    path: '/order',
    component: Layout,
    redirect: '/order/list',
    meta: { title: '订单管理', icon: 'List' },
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('@/views/order/list.vue'),
        meta: { title: '订单列表', icon: 'Document' }
      },
      {
        path: 'realtime',
        name: 'OrderRealtime',
        component: () => import('@/views/order/realtime.vue'),
        meta: { title: '实时订单', icon: 'Bell' }
      }
    ]
  },
  {
    path: '/menu',
    component: Layout,
    redirect: '/menu/list',
    meta: { title: '菜品管理', icon: 'Food' },
    children: [
      {
        path: 'list',
        name: 'MenuList',
        component: () => import('@/views/menu/list-new.vue'),
        meta: { title: '菜品列表', icon: 'List' }
      },
      {
        path: 'category',
        name: 'MenuCategory',
        component: () => import('@/views/menu/category.vue'),
        meta: { title: '分类管理', icon: 'Menu' }
      }
    ]
  },
  {
    path: '/table',
    component: Layout,
    children: [
      {
        path: 'list',
        name: 'TableList',
        component: () => import('@/views/table/list.vue'),
        meta: { title: '桌台管理', icon: 'Grid' }
      }
    ]
  },
  {
    path: '/marketing',
    component: Layout,
    redirect: '/marketing/coupon',
    meta: { title: '营销管理', icon: 'Promotion' },
    children: [
      {
        path: 'coupon',
        name: 'Coupon',
        component: () => import('@/views/marketing/coupon.vue'),
        meta: { title: '优惠券管理', icon: 'Ticket' }
      },
      {
        path: 'activity',
        name: 'Activity',
        component: () => import('@/views/marketing/activity.vue'),
        meta: { title: '活动管理', icon: 'Present' }
      },
      {
        path: 'flashsale',
        name: 'FlashSale',
        component: () => import('@/views/marketing/flashsale.vue'),
        meta: { title: '秒杀管理', icon: 'Timer' }
      }
    ]
  },
  {
    path: '/member',
    component: Layout,
    redirect: '/member/list',
    meta: { title: '会员管理', icon: 'User' },
    children: [
      {
        path: 'list',
        name: 'MemberList',
        component: () => import('@/views/member/list.vue'),
        meta: { title: '会员列表', icon: 'UserFilled' }
      },
      {
        path: 'level',
        name: 'MemberLevel',
        component: () => import('@/views/member/level.vue'),
        meta: { title: '等级管理', icon: 'TrophyBase' }
      }
    ]
  },
  {
    path: '/statistics',
    component: Layout,
    redirect: '/statistics/sales',
    meta: { title: '数据统计', icon: 'TrendCharts' },
    children: [
      {
        path: 'sales',
        name: 'SalesStatistics',
        component: () => import('@/views/statistics/sales.vue'),
        meta: { title: '销售统计', icon: 'Histogram' }
      },
      {
        path: 'dish',
        name: 'DishStatistics',
        component: () => import('@/views/statistics/dish.vue'),
        meta: { title: '菜品统计', icon: 'PieChart' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/settings',
    meta: { title: '系统设置', icon: 'Setting' },
    children: [
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/settings.vue'),
        meta: { title: '基本设置', icon: 'Tools' }
      },
      {
        path: 'staff',
        name: 'Staff',
        component: () => import('@/views/system/staff.vue'),
        meta: { title: '员工管理', icon: 'Avatar' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')  // 修改为 'token'
  
  if (to.path === '/login') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
