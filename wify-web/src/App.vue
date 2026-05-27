<script setup lang="ts">
import { ChatDotRound, Expand, Fold, Setting, User } from '@element-plus/icons-vue'
import packageJson from '../package.json'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useViewportWidth } from '@/composables/useViewportWidth'

const route = useRoute()
const AUTO_COLLAPSE_BREAKPOINT = 1200
const sidebarCollapsed = ref(false)
const appVersion = `v${packageJson.version}`
const userProfile = {
  initials: 'DP',
  name: 'dev.placeholder',
  subtitle: '内部用户',
}
const { width: viewportWidth } = useViewportWidth()

const menuItems = [
  {
    index: '/providers',
    label: '模型管理',
    icon: Setting,
  },
  {
    index: '/agents',
    label: 'Agent 管理',
    icon: User,
  },
  {
    index: '/chat',
    label: '对话',
    icon: ChatDotRound,
  },
]

const toggleSidebar = () => {
  if (viewportWidth.value < AUTO_COLLAPSE_BREAKPOINT) {
    return
  }

  sidebarCollapsed.value = !sidebarCollapsed.value
}

const isCompactViewport = computed(() => viewportWidth.value < AUTO_COLLAPSE_BREAKPOINT)
const isSidebarCollapsed = computed(() => sidebarCollapsed.value || isCompactViewport.value)

const currentMenuItem = computed(() => (
  menuItems.find((item) => route.path.startsWith(item.index))
))

const breadcrumbItems = computed(() => {
  const section = typeof route.meta.section === 'string' ? route.meta.section : '工作台'
  const title = typeof route.meta.title === 'string' ? route.meta.title : currentMenuItem.value?.label
  const items: Array<{ label: string; to?: string }> = [{ label: section, to: '/providers' }]

  if (title && title !== section) {
    items.push({ label: title })
  }

  return items
})
</script>

<template>
  <el-container class="app-layout">
    <el-aside
      :width="isSidebarCollapsed ? '88px' : '248px'"
      class="app-layout__aside"
      :class="{ 'is-collapsed': isSidebarCollapsed }"
    >
      <div class="app-layout__brand">
        <div class="app-layout__brand-mark">W</div>
        <div v-show="!isSidebarCollapsed" class="app-layout__brand-copy">
          <div class="app-layout__brand-title">Wify</div>
          <div class="app-layout__brand-subtitle">AI Agent Platform</div>
        </div>
      </div>

      <div v-show="!isSidebarCollapsed" class="app-layout__section-label">工作台</div>

      <el-menu
        :default-active="route.path"
        :collapse="isSidebarCollapsed"
        :collapse-transition="false"
        class="app-layout__menu"
        router
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.index"
          :index="item.index"
          class="app-layout__menu-item"
        >
          <el-icon class="app-layout__menu-icon">
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>

      <div class="app-layout__footer">
        <button
          type="button"
          class="app-layout__collapse-button"
          :aria-label="isCompactViewport ? '窄屏自动折叠' : (isSidebarCollapsed ? '展开侧边栏' : '折叠侧边栏')"
          :disabled="isCompactViewport"
          @click="toggleSidebar"
        >
          <el-icon class="app-layout__collapse-icon">
            <component :is="isSidebarCollapsed ? Expand : Fold" />
          </el-icon>
          <span v-if="!isSidebarCollapsed" class="app-layout__collapse-text">
            {{ isSidebarCollapsed ? '展开侧栏' : '折叠侧栏' }}
          </span>
        </button>

        <div class="app-layout__version" :class="{ 'is-collapsed': isSidebarCollapsed }">
          <span v-if="!isSidebarCollapsed" class="app-layout__version-label">版本</span>
          <span class="app-layout__version-value">{{ appVersion }}</span>
        </div>
      </div>
    </el-aside>

    <el-main class="app-layout__main">
      <div class="app-layout__main-shell">
        <header class="app-layout__topbar">
          <div class="app-layout__topbar-main">
            <div class="app-layout__topbar-caption">当前路径</div>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item
                v-for="item in breadcrumbItems"
                :key="item.label"
                :to="item.to ? { path: item.to } : undefined"
              >
                {{ item.label }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="app-layout__user-card">
            <el-avatar class="app-layout__user-avatar" :size="40">
              {{ userProfile.initials }}
            </el-avatar>
            <div class="app-layout__user-copy">
              <div class="app-layout__user-name">{{ userProfile.name }}</div>
              <div class="app-layout__user-subtitle">{{ userProfile.subtitle }}</div>
            </div>
          </div>
        </header>

        <section class="app-layout__page">
          <router-view />
        </section>
      </div>
    </el-main>
  </el-container>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: transparent;
}

.app-layout__aside {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
  height: 100vh;
  padding: 22px 16px 16px;
  border-right: 1px solid var(--wf-sidebar-border);
  background:
    linear-gradient(180deg, rgba(111, 124, 255, 0.16) 0%, rgba(111, 124, 255, 0) 20%),
    linear-gradient(180deg, rgba(66, 215, 198, 0.08) 0%, rgba(66, 215, 198, 0) 30%),
    var(--color-bg-dark);
  color: var(--wf-sidebar-text);
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.03);
  transition:
    width var(--wf-duration-base) var(--wf-ease-standard),
    padding var(--wf-duration-base) var(--wf-ease-standard);
}

.app-layout__aside.is-collapsed {
  padding-inline: 12px;
}

.app-layout__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 10px 10px;
  min-height: 64px;
}

.app-layout__aside.is-collapsed .app-layout__brand {
  justify-content: center;
  padding-inline: 0;
}

.app-layout__brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.06),
    0 12px 28px rgba(var(--wf-primary-rgb), 0.18);
}

.app-layout__brand-copy {
  min-width: 0;
}

.app-layout__brand-title {
  font-size: 22px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0;
  background: var(--wf-gradient-brand);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.app-layout__brand-subtitle {
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.56);
  font-size: 12px;
  line-height: 1.3;
}

.app-layout__section-label {
  padding: 0 12px;
  color: rgba(226, 232, 240, 0.48);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.app-layout__menu {
  flex: 1;
  border-right: 0;
  background: transparent;
}

.app-layout__menu :deep(.el-menu) {
  border-right: 0;
  background: transparent;
}

.app-layout__menu :deep(.el-menu-item) {
  position: relative;
  height: 48px;
  min-height: 48px;
  margin-bottom: 8px;
  padding: 0 16px;
  border-radius: 12px;
  background: transparent;
  color: rgba(255, 255, 255, 0.92);
  line-height: 1.4;
  font-size: 14px;
  font-weight: 600;
  transition: var(--wf-transition-base);
}

.app-layout__menu :deep(.el-menu--collapse .el-menu-item) {
  justify-content: center;
  padding: 0;
}

.app-layout__menu :deep(.el-menu--collapse .app-layout__menu-icon) {
  margin-right: 0;
}

.app-layout__menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.app-layout__menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.14);
  color: #ffffff;
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.04),
    0 12px 30px rgba(var(--wf-primary-rgb), 0.14);
}

.app-layout__menu :deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  bottom: 9px;
  width: 3px;
  border-radius: 0 999px 999px 0;
  background: var(--wf-gradient-brand);
  opacity: 0;
  transform: scaleY(0.5);
  transition:
    opacity var(--wf-duration-fast) var(--wf-ease-standard),
    transform var(--wf-duration-base) var(--wf-ease-standard);
}

.app-layout__menu :deep(.el-menu-item.is-active::before) {
  opacity: 1;
  transform: scaleY(1);
}

.app-layout__menu-icon {
  margin-right: 12px;
  font-size: 18px;
  color: currentColor;
}

.app-layout__footer {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 10px 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.app-layout__collapse-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  min-height: 40px;
  padding: 0 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.92);
  outline: none;
  cursor: pointer;
  transition: var(--wf-transition-base);
}

.app-layout__collapse-button:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.18);
  box-shadow: var(--wf-shadow-glow-primary);
}

.app-layout__collapse-button:focus-visible {
  border-color: rgba(var(--wf-primary-rgb), 0.42);
  box-shadow: var(--wf-focus-ring);
}

.app-layout__collapse-button:disabled,
.app-layout__collapse-button:disabled:hover {
  border-color: rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.48);
  box-shadow: none;
  cursor: not-allowed;
}

.app-layout__collapse-icon {
  font-size: 16px;
}

.app-layout__collapse-text {
  font-size: 13px;
  font-weight: 600;
}

.app-layout__version {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.64);
}

.app-layout__version.is-collapsed {
  justify-content: center;
}

.app-layout__version-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.app-layout__version-value {
  font-size: 12px;
  font-weight: 700;
  color: #ffffff;
}

.app-layout__main {
  padding: 24px;
  background: var(--color-bg-secondary);
  color: var(--wf-text-default);
}

.app-layout__main-shell {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  max-width: 1480px;
  margin: 0 auto;
}

.app-layout__topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: var(--wf-radius-lg);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--wf-shadow-sm);
  backdrop-filter: blur(14px);
}

.app-layout__topbar-main {
  min-width: 0;
}

.app-layout__topbar-caption {
  margin-bottom: 8px;
  color: var(--wf-text-muted);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.app-layout__topbar :deep(.el-breadcrumb) {
  line-height: 1.2;
}

.app-layout__topbar :deep(.el-breadcrumb__inner) {
  color: var(--wf-text-secondary);
  font-size: 14px;
  font-weight: 600;
}

.app-layout__topbar :deep(.el-breadcrumb__inner.is-link) {
  color: var(--wf-text-muted);
  transition: color var(--wf-duration-fast) var(--wf-ease-standard);
}

.app-layout__topbar :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--wf-color-primary-600);
}

.app-layout__topbar :deep(.el-breadcrumb__separator) {
  color: var(--wf-text-disabled);
  margin: 0 10px;
}

.app-layout__user-card {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px 8px 8px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 999px;
  background: #ffffff;
  box-shadow: var(--wf-shadow-xs);
}

.app-layout__user-avatar {
  color: #ffffff;
  font-size: 13px;
  font-weight: 700;
  background: var(--wf-gradient-brand);
  box-shadow: 0 8px 20px rgba(var(--wf-primary-rgb), 0.22);
}

.app-layout__user-copy {
  min-width: 0;
}

.app-layout__user-name {
  color: var(--wf-text-strong);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.2;
}

.app-layout__user-subtitle {
  margin-top: 4px;
  color: var(--wf-text-muted);
  font-size: 12px;
  line-height: 1.2;
}

.app-layout__page {
  min-width: 0;
}

@media (max-width: 960px) {
  .app-layout__aside {
    position: static;
    height: auto;
    padding-inline: 12px;
  }

  .app-layout__main {
    padding: 16px;
  }

  .app-layout__topbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .app-layout__user-card {
    width: 100%;
  }
}
</style>
