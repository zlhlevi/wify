<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const menuItems = [
  { index: '/providers', label: '模型管理' },
  { index: '/agents', label: 'Agent 管理' },
  { index: '/chat', label: '对话' },
]
</script>

<template>
  <el-container class="app-layout">
    <el-aside class="app-layout__aside" width="220px">
      <div class="app-layout__title">Wify</div>
      <el-menu
        :default-active="route.path"
        class="app-layout__menu"
        router
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.index"
          :index="item.index"
        >
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-main class="app-layout__main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-layout__aside {
  border-right: 1px solid var(--el-border-color-light);
  background: #ffffff;
}

.app-layout__title {
  padding: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.app-layout__menu {
  border-right: 0;
}

.app-layout__main {
  padding: 24px;
  background: #f4f6fb;
  color: #111827;
}
</style>
