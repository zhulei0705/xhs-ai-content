<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { contentApi, publishApi } from '@/api/modules'
import type { ContentListItem, PublishRecord } from '@/types'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const rows = ref<PublishRecord[]>([])
const readyContents = ref<ContentListItem[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ contentId: '', publishedAt: '', noteUrl: '', exposureCount: 0, likeCount: 0, favoriteCount: 0, commentCount: 0, followerGrowth: 0 })
const formatNumber = (value: number) => new Intl.NumberFormat('zh-CN').format(value)
const formatRate = (value: number) => `${(value * 100).toFixed(2)}%`

function localDateTime() {
  const date = new Date()
  const offset = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offset).toISOString().slice(0, 19)
}

async function load() {
  loading.value = true
  try { const result = await publishApi.page(query); rows.value = result.records; total.value = Number(result.total) } finally { loading.value = false }
}

async function loadReady() {
  const result = await contentApi.page({ pageNum: 1, pageSize: 100, status: 'READY_TO_PUBLISH' })
  readyContents.value = result.records
}

async function openCreate() {
  await loadReady()
  Object.assign(form, { contentId: '', publishedAt: localDateTime(), noteUrl: '', exposureCount: 0, likeCount: 0, favoriteCount: 0, commentCount: 0, followerGrowth: 0 })
  dialogVisible.value = true
}

function openUpdate(row: PublishRecord) {
  readyContents.value = [{ id: row.contentId, selectedTitle: row.contentTitle } as ContentListItem]
  Object.assign(form, { contentId: row.contentId, publishedAt: row.publishedAt.slice(0, 19), noteUrl: row.noteUrl, exposureCount: row.exposureCount, likeCount: row.likeCount, favoriteCount: row.favoriteCount, commentCount: row.commentCount, followerGrowth: row.followerGrowth })
  dialogVisible.value = true
}

async function save() {
  if (!form.contentId || !form.publishedAt || !/^https?:\/\//.test(form.noteUrl)) {
    ElMessage.warning('请选择内容，并填写正确的发布时间和笔记链接')
    return
  }
  saving.value = true
  try { await publishApi.save(form); ElMessage.success('发布数据已保存'); dialogVisible.value = false; await load() } finally { saving.value = false }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-hero"><div><h2>人工发布台账</h2><p>在小红书人工发布后回填链接和数据；系统不会代替你登录或发布</p></div><el-button type="primary" class="xhs-button" @click="openCreate">登记人工发布</el-button></div>
    <div class="panel">
      <div class="panel-header"><h3>发布记录</h3><span class="muted">数据可随时更新</span></div>
      <el-table v-loading="loading" :data="rows">
        <el-table-column label="内容" min-width="280"><template #default="scope"><strong>{{ scope.row.contentTitle }}</strong><div style="margin-top:7px"><el-link :href="scope.row.noteUrl" target="_blank" type="primary">打开小红书笔记</el-link></div></template></el-table-column>
        <el-table-column label="发布时间" width="170"><template #default="scope">{{ new Date(scope.row.publishedAt).toLocaleString('zh-CN') }}</template></el-table-column>
        <el-table-column label="曝光" width="105" align="right"><template #default="scope">{{ formatNumber(scope.row.exposureCount) }}</template></el-table-column>
        <el-table-column label="点赞" width="85" align="right" prop="likeCount" />
        <el-table-column label="收藏" width="85" align="right" prop="favoriteCount" />
        <el-table-column label="评论" width="85" align="right" prop="commentCount" />
        <el-table-column label="收藏率" width="100"><template #default="scope"><strong>{{ formatRate(scope.row.favoriteRate) }}</strong></template></el-table-column>
        <el-table-column label="互动率" width="100"><template #default="scope"><strong>{{ formatRate(scope.row.interactionRate) }}</strong></template></el-table-column>
        <el-table-column label="涨粉" width="80" align="right"><template #default="scope">+{{ scope.row.followerGrowth }}</template></el-table-column>
        <el-table-column label="操作" width="100" fixed="right"><template #default="scope"><el-button type="primary" link @click="openUpdate(scope.row)">更新数据</el-button></template></el-table-column>
      </el-table>
      <div style="padding:18px;display:flex;justify-content:flex-end"><el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total" layout="total,prev,pager,next" @current-change="load" /></div>
    </div>
    <el-dialog v-model="dialogVisible" title="发布记录" width="660px">
      <el-alert title="请先在小红书人工发布，再回到这里登记。不要填写账号密码或 Token。" type="info" :closable="false" style="margin-bottom:18px" />
      <el-form label-position="top">
        <el-form-item label="待发布内容"><el-select v-model="form.contentId" placeholder="选择已人工确认的内容" style="width:100%"><el-option v-for="item in readyContents" :key="item.id" :label="item.selectedTitle" :value="item.id" /></el-select></el-form-item>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px"><el-form-item label="发布时间"><el-date-picker v-model="form.publishedAt" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item><el-form-item label="小红书笔记链接"><el-input v-model="form.noteUrl" placeholder="https://www.xiaohongshu.com/..." /></el-form-item></div>
        <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px"><el-form-item label="曝光量"><el-input-number v-model="form.exposureCount" :min="0" controls-position="right" /></el-form-item><el-form-item label="点赞"><el-input-number v-model="form.likeCount" :min="0" controls-position="right" /></el-form-item><el-form-item label="收藏"><el-input-number v-model="form.favoriteCount" :min="0" controls-position="right" /></el-form-item><el-form-item label="评论"><el-input-number v-model="form.commentCount" :min="0" controls-position="right" /></el-form-item><el-form-item label="涨粉数"><el-input-number v-model="form.followerGrowth" controls-position="right" /></el-form-item></div>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" class="xhs-button" :loading="saving" @click="save">保存记录</el-button></template>
    </el-dialog>
  </div>
</template>
