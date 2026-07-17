<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contentApi } from '@/api/modules'
import type { ContentListItem, ContentStatus } from '@/types'

const router = useRouter()
const loading = ref(false)
const rows = ref<ContentListItem[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: '', keyword: '' })
const labels: Record<ContentStatus, string> = { DRAFT: '草稿', PENDING_REVIEW: '待审核', READY_TO_PUBLISH: '待发布', PUBLISHED: '已发布', ARCHIVED: '已归档' }

async function load() {
  loading.value = true
  try { const result = await contentApi.page(query); rows.value = result.records; total.value = Number(result.total) } finally { loading.value = false }
}

async function copy(row: ContentListItem) {
  const data = await contentApi.copy(row.id)
  await navigator.clipboard.writeText(`${data.title}\n\n${data.bodyWithTags}\n\n${data.interactionGuide}`)
  ElMessage.success('标题、正文、标签和互动语已复制')
}

async function remove(row: ContentListItem) {
  await ElMessageBox.confirm(`确认删除“${row.selectedTitle}”？`, '删除内容', { type: 'warning' })
  await contentApi.remove(row.id)
  ElMessage.success('已删除')
  await load()
}

async function archive(row: ContentListItem) {
  await contentApi.status(row.id, 'ARCHIVED')
  ElMessage.success('已归档')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-hero"><div><h2>内容资产</h2><p>从 AI 草稿到人工确认、复制发布的完整状态管理</p></div><el-button type="primary" class="xhs-button" @click="router.push('/contents/new')">新建内容</el-button></div>
    <div class="panel">
      <div class="panel-header"><div class="toolbar"><el-input v-model="query.keyword" clearable placeholder="搜索标题" style="width:220px" @keyup.enter="load" /><el-select v-model="query.status" clearable placeholder="全部状态" style="width:150px"><el-option v-for="(label,key) in labels" :key="key" :label="label" :value="key" /></el-select><el-button @click="query.pageNum=1;load()">查询</el-button></div><span class="muted">共 {{ total }} 篇</span></div>
      <el-table v-loading="loading" :data="rows">
        <el-table-column label="内容标题" min-width="340"><template #default="scope"><strong>{{ scope.row.selectedTitle }}</strong><div class="tag-list" style="margin-top:9px"><el-tag v-for="tag in scope.row.tags.slice(0,4)" :key="tag" size="small" effect="plain">#{{ tag }}</el-tag></div></template></el-table-column>
        <el-table-column label="封面文案" min-width="180" prop="coverTitle" />
        <el-table-column label="状态" width="120"><template #default="scope"><span :class="`status-dot status-${scope.row.status}`" />{{ labels[scope.row.status as ContentStatus] }}</template></el-table-column>
        <el-table-column label="最后更新" width="180"><template #default="scope">{{ new Date(scope.row.updatedAt).toLocaleString('zh-CN') }}</template></el-table-column>
        <el-table-column label="操作" width="260" fixed="right"><template #default="scope"><el-button type="primary" link @click="router.push(`/contents/${scope.row.id}`)">{{ ['PUBLISHED','ARCHIVED'].includes(scope.row.status) ? '查看' : '编辑 / 审核' }}</el-button><el-button link @click="copy(scope.row)">一键复制</el-button><el-dropdown><el-button link>更多</el-button><template #dropdown><el-dropdown-menu><el-dropdown-item @click="archive(scope.row)">归档</el-dropdown-item><el-dropdown-item :disabled="['PUBLISHED','ARCHIVED'].includes(scope.row.status)" @click="remove(scope.row)">删除</el-dropdown-item></el-dropdown-menu></template></el-dropdown></template></el-table-column>
      </el-table>
      <div style="padding:18px;display:flex;justify-content:flex-end"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" layout="total,prev,pager,next" @current-change="load" /></div>
    </div>
  </div>
</template>
