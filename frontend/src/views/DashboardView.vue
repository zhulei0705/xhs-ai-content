<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { analysisApi } from '@/api/modules'
import type { Dashboard } from '@/types'

const loading = ref(false)
const dashboard = ref<Dashboard>({
  publishedCount: 0,
  totalExposure: 0,
  totalLikes: 0,
  totalFavorites: 0,
  totalComments: 0,
  totalFollowerGrowth: 0,
  averageFavoriteRate: 0,
  averageInteractionRate: 0,
  topContents: [],
})

const formatNumber = (value: number) => new Intl.NumberFormat('zh-CN').format(value)
const formatRate = (value: number) => `${(value * 100).toFixed(2)}%`

async function load() {
  loading.value = true
  try { dashboard.value = await analysisApi.dashboard({}) } finally { loading.value = false }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="page-hero">
      <div><h2>创作增长仪表盘</h2><p>最近 30 天内容数据，所有指标来自人工回填的发布记录</p></div>
      <el-button @click="load">刷新数据</el-button>
    </div>
    <div class="metric-grid">
      <div class="metric-card"><span class="metric-label">已发布内容</span><strong class="metric-value">{{ formatNumber(dashboard.publishedCount) }}</strong><div class="metric-caption">近 30 天发布量</div></div>
      <div class="metric-card"><span class="metric-label">总曝光量</span><strong class="metric-value">{{ formatNumber(dashboard.totalExposure) }}</strong><div class="metric-caption">人工回填累计曝光</div></div>
      <div class="metric-card"><span class="metric-label">平均收藏率</span><strong class="metric-value">{{ formatRate(dashboard.averageFavoriteRate) }}</strong><div class="metric-caption">收藏 ÷ 曝光</div></div>
      <div class="metric-card"><span class="metric-label">平均互动率</span><strong class="metric-value">{{ formatRate(dashboard.averageInteractionRate) }}</strong><div class="metric-caption">赞藏评 ÷ 曝光</div></div>
    </div>
    <div class="two-columns">
      <div class="panel">
        <div class="panel-header"><h3>高曝光内容</h3><span class="muted">TOP 5</span></div>
        <el-table :data="dashboard.topContents" empty-text="发布并回填数据后显示">
          <el-table-column label="内容标题" prop="title" min-width="260" />
          <el-table-column label="曝光" width="120"><template #default="scope">{{ formatNumber(scope.row.exposureCount) }}</template></el-table-column>
          <el-table-column label="互动率" width="120"><template #default="scope">{{ formatRate(scope.row.interactionRate) }}</template></el-table-column>
        </el-table>
      </div>
      <div class="panel">
        <div class="panel-header"><h3>互动拆解</h3><span class="muted">30 DAYS</span></div>
        <div class="panel-body">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="点赞">{{ formatNumber(dashboard.totalLikes) }}</el-descriptions-item>
            <el-descriptions-item label="收藏">{{ formatNumber(dashboard.totalFavorites) }}</el-descriptions-item>
            <el-descriptions-item label="评论">{{ formatNumber(dashboard.totalComments) }}</el-descriptions-item>
            <el-descriptions-item label="涨粉">{{ formatNumber(dashboard.totalFollowerGrowth) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </div>
  </div>
</template>
