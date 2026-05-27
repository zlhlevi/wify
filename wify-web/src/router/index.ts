import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/providers',
    },
    {
      path: '/providers',
      name: 'provider-list',
      meta: {
        section: '工作台',
        title: '模型管理',
      },
      component: () => import('@/views/provider/ProviderList.vue'),
    },
    {
      path: '/agents',
      name: 'agent-list',
      meta: {
        section: '工作台',
        title: 'Agent 管理',
      },
      component: () => import('@/views/agent/AgentList.vue'),
    },
    {
      path: '/chat',
      name: 'chat',
      meta: {
        section: '工作台',
        title: '对话',
      },
      component: () => import('@/views/chat/ChatView.vue'),
    },
  ],
})

export default router
