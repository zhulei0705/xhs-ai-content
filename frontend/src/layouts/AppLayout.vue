<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { DataAnalysis, Document, EditPen, House, Promotion, TrendCharts } from '@element-plus/icons-vue'

const route = useRoute()
const title = computed(() => String(route.meta.title ?? '创作台'))
const today = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">薯</div>
        <div><strong>红薯创作台</strong><span>AI CONTENT STUDIO</span></div>
      </div>
      <el-menu :default-active="route.path" router class="nav-menu">
        <el-menu-item index="/dashboard"><el-icon><House /></el-icon><span>数据概览</span></el-menu-item>
        <el-menu-item index="/topics"><el-icon><EditPen /></el-icon><span>AI 选题</span></el-menu-item>
        <el-menu-item index="/contents"><el-icon><Document /></el-icon><span>内容管理</span></el-menu-item>
        <el-menu-item index="/publish"><el-icon><Promotion /></el-icon><span>发布记录</span></el-menu-item>
        <el-menu-item index="/analysis"><el-icon><TrendCharts /></el-icon><span>AI 数据复盘</span></el-menu-item>
      </el-menu>
      <div class="safety-note">
        <el-icon><DataAnalysis /></el-icon>
        <div><strong>人工发布模式</strong><span>AI 生成后需人工确认，不使用非官方发布接口</span></div>
      </div>
    </aside>
    <main class="main-panel">
      <header class="topbar">
        <div><p>{{ today }}</p><h1>{{ title }}</h1></div>
        <div class="profile-chip"><span class="online-dot" />程序员 AI 副业博主</div>
      </header>
      <section class="page-container"><router-view /></section>
    </main>
  </div>
</template>
