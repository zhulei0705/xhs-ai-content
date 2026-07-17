<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { analysisApi, publishApi } from '@/api/modules'
import type { PerformanceAnalysis, PublishRecord } from '@/types'

const loading = ref(false)
const analyzingId = ref<string>()
const rows = ref<PublishRecord[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const drawerVisible = ref(false)
const analysis = ref<PerformanceAnalysis>()
const formatRate = (value: number) => `${(value * 100).toFixed(2)}%`

async function load() {
  loading.value = true
  try { const result = await publishApi.page(query); rows.value = result.records; total.value = Number(result.total) } finally { loading.value = false }
}

async function execute(row: PublishRecord) {
  analyzingId.value = row.id
  try {
    analysis.value = await analysisApi.execute(row.id)
    drawerVisible.value = true
    ElMessage.success('AI 复盘已生成')
  } finally { analyzingId.value = undefined }
}

async function latest(row: PublishRecord) {
  analysis.value = await analysisApi.latest(row.id)
  drawerVisible.value = true
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-hero"><div><h2>用数据指导下一篇</h2><p>结合曝光、收藏率和互动率复盘选题与标题，输出下一批具体建议</p></div></div>
    <div class="panel">
      <div class="panel-header"><h3>选择一篇已发布内容</h3><span class="muted">每次复盘都会保留历史版本</span></div>
      <el-table v-loading="loading" :data="rows">
        <el-table-column label="标题" prop="contentTitle" min-width="320" />
        <el-table-column label="曝光" prop="exposureCount" width="110" align="right" />
        <el-table-column label="收藏率" width="110"><template #default="scope">{{ formatRate(scope.row.favoriteRate) }}</template></el-table-column>
        <el-table-column label="互动率" width="110"><template #default="scope">{{ formatRate(scope.row.interactionRate) }}</template></el-table-column>
        <el-table-column label="涨粉" prop="followerGrowth" width="90" align="right" />
        <el-table-column label="操作" width="220"><template #default="scope"><el-button type="primary" link :loading="analyzingId===scope.row.id" @click="execute(scope.row)">生成新复盘</el-button><el-button link @click="latest(scope.row)">查看最近复盘</el-button></template></el-table-column>
      </el-table>
      <div style="padding:18px;display:flex;justify-content:flex-end"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" layout="total,prev,pager,next" @current-change="load" /></div>
    </div>
    <el-drawer v-model="drawerVisible" title="AI 数据复盘" size="620px">
      <div v-if="analysis" style="display:grid;gap:16px">
        <div style="display:flex;align-items:center;gap:16px;padding:18px;border-radius:14px;background:#fff3f5"><div style="width:62px;height:62px;border-radius:18px;background:var(--xhs-red);color:white;display:grid;place-items:center;font-size:25px;font-weight:800">{{ analysis.overallGrade }}</div><div><strong>综合表现</strong><p style="margin:6px 0 0;color:#6f696c;line-height:1.6">{{ analysis.performanceSummary }}</p></div></div>
        <el-card shadow="never"><template #header><strong>选题分析</strong></template><p style="line-height:1.8;margin:0">{{ analysis.topicAnalysis }}</p></el-card>
        <el-card shadow="never"><template #header><strong>标题分析</strong></template><p style="line-height:1.8;margin:0">{{ analysis.titleAnalysis }}</p></el-card>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px"><el-card shadow="never"><template #header><strong>收藏率</strong></template><p style="line-height:1.8;margin:0">{{ analysis.favoriteRateAnalysis }}</p></el-card><el-card shadow="never"><template #header><strong>互动率</strong></template><p style="line-height:1.8;margin:0">{{ analysis.interactionRateAnalysis }}</p></el-card></div>
        <el-card shadow="never"><template #header><strong>下一批选题建议</strong></template><ol style="padding-left:22px;line-height:2"><li v-for="item in analysis.nextTopicSuggestions" :key="item">{{ item }}</li></ol></el-card>
        <p class="muted">由 {{ analysis.provider }} / {{ analysis.modelName }} 于 {{ new Date(analysis.analyzedAt).toLocaleString('zh-CN') }} 生成</p>
      </div>
    </el-drawer>
  </div>
</template>
