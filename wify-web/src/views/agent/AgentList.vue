<script setup lang="ts">
import { Cpu, Plus, Upload } from '@element-plus/icons-vue'

const agentRows = [
  {
    name: '架构助手',
    model: 'gpt-4.1',
    tools: '12 个工具',
    prompt: 'System Prompt 已版本化',
  },
  {
    name: '发布值班助手',
    model: 'claude-sonnet',
    tools: '5 个工具',
    prompt: '支持发布检查 / 变更摘要',
  },
  {
    name: '知识库问答助手',
    model: 'gemini-2.5-pro',
    tools: 'RAG + MCP',
    prompt: '启用引用回答',
  },
]
</script>

<template>
  <div class="wf-page">
    <section class="wf-page__header">
      <div>
        <span class="wf-page__eyebrow">
          <el-icon><Cpu /></el-icon>
          Agent Studio
        </span>
        <h1 class="wf-page__title">Agent 管理</h1>
        <p class="wf-page__description">
          用统一的配置视图管理 Prompt、模型绑定、工具集和工作流触发条件，适合内部研发团队的高频迭代。
        </p>
      </div>

      <div class="wf-page__actions">
        <span class="wf-chip wf-chip--primary">18 已发布</span>
        <el-button>
          <el-icon><Upload /></el-icon>
          导入模板
        </el-button>
        <el-button type="primary">
          <el-icon><Plus /></el-icon>
          新建 Agent
        </el-button>
      </div>
    </section>

    <section class="wf-panel">
      <header class="wf-panel__header">
        <div>
          <h2 class="wf-panel__title">Agent 资产概览</h2>
          <p class="wf-panel__description">
            当前页面先展示后台骨架，后续可直接扩展为表格、版本历史、工具绑定和配置抽屉。
          </p>
        </div>
      </header>

      <div class="wf-panel__body">
        <div class="wf-placeholder-stack">
          <article
            v-for="agent in agentRows"
            :key="agent.name"
            class="wf-list-row"
          >
            <div>
              <div class="wf-list-row__title">{{ agent.name }}</div>
              <div class="wf-list-row__meta">{{ agent.prompt }}</div>
            </div>

            <div style="display: flex; gap: 10px; flex-wrap: wrap; justify-content: flex-end">
              <span class="wf-chip">{{ agent.model }}</span>
              <span class="wf-chip wf-chip--accent">{{ agent.tools }}</span>
            </div>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>
