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
      component: () => import('@/views/provider/ProviderList.vue'),
    },
    {
      path: '/agents',
      name: 'agent-list',
      component: () => import('@/views/agent/AgentList.vue'),
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('@/views/chat/ChatView.vue'),
    },
  ],
})

export default router
