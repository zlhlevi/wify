<script setup lang="ts">
import { ChatDotRound, Delete, Plus, Promotion } from '@element-plus/icons-vue'
</script>

<template>
  <div class="wf-page">
    <section class="wf-page__header">
      <div>
        <span class="wf-page__eyebrow">
          <el-icon><ChatDotRound /></el-icon>
          Live Console
        </span>
        <h1 class="wf-page__title">对话控制台</h1>
        <p class="wf-page__description">
          这个页面保留浅底阅读体验，把科技感集中到消息高亮、状态胶囊和发送动作上，适合长时间调试多轮会话。
        </p>
      </div>

      <div class="wf-page__actions">
        <span class="wf-chip wf-chip--accent">Streaming</span>
        <span class="wf-chip wf-chip--primary">RAG Ready</span>
        <el-button type="danger">
          <el-icon><Delete /></el-icon>
          清空会话
        </el-button>
        <el-button type="primary">
          <el-icon><Plus /></el-icon>
          新建会话
        </el-button>
      </div>
    </section>

    <section class="wf-chat-layout">
      <div class="wf-panel">
        <header class="wf-panel__header">
          <div>
            <h2 class="wf-panel__title">会话流</h2>
            <p class="wf-panel__description">消息流、工具调用和引用结果都会在这里汇总。</p>
          </div>
        </header>

        <div class="wf-panel__body wf-chat-stream">
          <article class="wf-message wf-message--user">
            <div class="wf-message__meta">User</div>
            帮我比较 OpenAI 和 Claude 两个 Provider 的当前可用模型，并给出推荐绑定策略。
          </article>

          <article class="wf-message wf-message--assistant">
            <div class="wf-message__meta">Agent</div>
            已读取 Provider 配置与模型清单。我建议把通用对话默认绑定到稳定性更高的主力模型，Embedding 与 Rerank
            则拆分为独立配置，避免单 Provider 过载。
          </article>

          <div class="wf-composer">
            <div class="wf-composer__hint">输入消息、Prompt patch 或调试指令…</div>
            <el-button type="primary">
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </div>

      <aside class="wf-panel">
        <header class="wf-panel__header">
          <div>
            <h2 class="wf-panel__title">会话上下文</h2>
            <p class="wf-panel__description">适合放 Agent、模型、知识库和工具绑定摘要。</p>
          </div>
        </header>

        <div class="wf-panel__body">
          <div class="wf-placeholder-stack">
            <article class="wf-list-row">
              <div>
                <div class="wf-list-row__title">当前 Agent</div>
                <div class="wf-list-row__meta">架构助手 / 已启用工具 4 个</div>
              </div>
              <span class="wf-chip wf-chip--primary">gpt-4.1</span>
            </article>

            <article class="wf-list-row">
              <div>
                <div class="wf-list-row__title">知识库</div>
                <div class="wf-list-row__meta">工程规范、接口文档、发布流程</div>
              </div>
              <span class="wf-chip wf-chip--accent">RAG</span>
            </article>

            <article class="wf-list-row">
              <div>
                <div class="wf-list-row__title">响应模式</div>
                <div class="wf-list-row__meta">SSE 流式输出 / 引用回答 / 工具可见</div>
              </div>
              <span class="wf-chip">Verbose</span>
            </article>
          </div>
        </div>
      </aside>
    </section>
  </div>
</template>
