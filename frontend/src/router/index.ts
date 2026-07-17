import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppLayout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '数据概览' } },
        { path: 'topics', name: 'topics', component: () => import('@/views/TopicView.vue'), meta: { title: 'AI 选题' } },
        { path: 'contents', name: 'contents', component: () => import('@/views/ContentView.vue'), meta: { title: '内容管理' } },
        { path: 'contents/new', name: 'content-create', component: () => import('@/views/ContentWorkbenchView.vue'), meta: { title: '新建内容' } },
        { path: 'contents/:id', name: 'content-workbench', component: () => import('@/views/ContentWorkbenchView.vue'), meta: { title: '内容工作台' } },
        { path: 'publish', name: 'publish', component: () => import('@/views/PublishView.vue'), meta: { title: '发布记录' } },
        { path: 'analysis', name: 'analysis', component: () => import('@/views/AnalysisView.vue'), meta: { title: 'AI 数据复盘' } },
      ],
    },
  ],
})

router.afterEach((to) => {
  document.title = `${String(to.meta.title ?? '创作台')} · 红薯创作台`
})

export default router
