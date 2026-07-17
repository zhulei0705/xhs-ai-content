<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contentApi, topicApi } from '@/api/modules'
import type { Topic } from '@/types'

const router = useRouter()
const loading = ref(false)
const generating = ref(false)
const rows = ref<Topic[]>([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: '', generationDate: '', keyword: '' })
const dialogVisible = ref(false)
const editingId = ref<string>()
const form = reactive({
  topicTitle: '', coreAngle: '', targetAudience: '想利用下班时间尝试AI副业的职场人',
  contentType: '成长复盘', keywordsText: 'AI副业,个人成长', targetMatchScore: 80,
  viralPotentialScore: 80, overallScore: 80, potentialAnalysis: '', publishPriority: 1,
})

async function load() {
  loading.value = true
  try {
    const result = await topicApi.page({ ...query, generationDate: query.generationDate || undefined })
    rows.value = result.records
    total.value = Number(result.total)
  } finally { loading.value = false }
}

async function generate() {
  generating.value = true
  try {
    await topicApi.generate({ count: 5 })
    ElMessage.success('今日选题已准备好')
    query.pageNum = 1
    await load()
  } finally { generating.value = false }
}

async function generateContent(topic: Topic) {
  generating.value = true
  try {
    const content = await contentApi.generate(topic.id)
    ElMessage.success('完整内容包已生成')
    await router.push(`/contents/${content.id}`)
  } finally { generating.value = false }
}

function openCreate() {
  editingId.value = undefined
  Object.assign(form, { topicTitle: '', coreAngle: '', targetAudience: '想利用下班时间尝试AI副业的职场人', contentType: '成长复盘', keywordsText: 'AI副业,个人成长', targetMatchScore: 80, viralPotentialScore: 80, overallScore: 80, potentialAnalysis: '', publishPriority: 1 })
  dialogVisible.value = true
}

function openEdit(row: Topic) {
  editingId.value = row.id
  Object.assign(form, { ...row, keywordsText: row.keywords.join(',') })
  dialogVisible.value = true
}

async function saveTopic() {
  if (!form.topicTitle || !form.coreAngle || !form.potentialAnalysis) {
    ElMessage.warning('请填写选题、切入角度和潜力分析')
    return
  }
  const payload = { ...form, id: editingId.value, keywords: form.keywordsText.split(/[,，]/).map(v => v.trim()).filter(Boolean) }
  if (editingId.value) await topicApi.update(payload)
  else await topicApi.create(payload)
  ElMessage.success('选题已保存')
  dialogVisible.value = false
  await load()
}

async function archive(row: Topic) {
  await topicApi.status(row.id, 'ARCHIVED')
  ElMessage.success('已归档')
  await load()
}

async function remove(row: Topic) {
  await ElMessageBox.confirm(`确认删除“${row.topicTitle}”？`, '删除选题', { type: 'warning' })
  await topicApi.remove(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-hero">
      <div><h2>今天写什么？</h2><p>AI 会结合账号人设生成 5 个差异化选题，并给出目标匹配度和爆款潜力</p></div>
      <div class="toolbar"><el-button @click="openCreate">手动新增</el-button><el-button type="primary" class="xhs-button" :loading="generating" @click="generate">AI 生成今日选题</el-button></div>
    </div>
    <div class="panel">
      <div class="panel-header">
        <div class="toolbar">
          <el-input v-model="query.keyword" clearable placeholder="搜索选题" style="width: 220px" @keyup.enter="load" />
          <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 140px"><el-option label="候选" value="CANDIDATE" /><el-option label="已使用" value="USED" /><el-option label="已归档" value="ARCHIVED" /></el-select>
          <el-date-picker v-model="query.generationDate" value-format="YYYY-MM-DD" type="date" placeholder="生成日期" />
          <el-button @click="query.pageNum = 1; load()">查询</el-button>
        </div>
        <span class="muted">共 {{ total }} 个选题</span>
      </div>
      <el-table v-loading="loading" :data="rows" row-key="id">
        <el-table-column type="expand"><template #default="scope"><div style="padding: 8px 28px 18px"><p><strong>切入角度：</strong>{{ scope.row.coreAngle }}</p><p><strong>潜力分析：</strong>{{ scope.row.potentialAnalysis }}</p><p><strong>目标人群：</strong>{{ scope.row.targetAudience }}</p></div></template></el-table-column>
        <el-table-column label="优先级" width="74" align="center"><template #default="scope"><strong>#{{ scope.row.publishPriority }}</strong></template></el-table-column>
        <el-table-column label="选题" min-width="310"><template #default="scope"><strong>{{ scope.row.topicTitle }}</strong><div class="tag-list" style="margin-top: 9px"><el-tag v-for="tag in scope.row.keywords" :key="tag" size="small" effect="plain">{{ tag }}</el-tag></div></template></el-table-column>
        <el-table-column label="匹配度" width="90" align="center"><template #default="scope"><span class="score">{{ scope.row.targetMatchScore }}</span></template></el-table-column>
        <el-table-column label="爆款潜力" width="100" align="center"><template #default="scope"><span class="score high">{{ scope.row.viralPotentialScore }}</span></template></el-table-column>
        <el-table-column label="综合分" width="84" align="center"><template #default="scope"><el-progress type="circle" :width="46" :stroke-width="5" :percentage="scope.row.overallScore" :show-text="true" /></template></el-table-column>
        <el-table-column label="状态" width="86"><template #default="scope"><el-tag size="small" :type="scope.row.status === 'USED' ? 'success' : scope.row.status === 'ARCHIVED' ? 'info' : 'warning'">{{ { CANDIDATE: '候选', USED: '已使用', ARCHIVED: '已归档', SELECTED: '已选择' }[scope.row.status as string] }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="245" fixed="right"><template #default="scope"><el-button type="primary" link :disabled="scope.row.status === 'ARCHIVED'" @click="generateContent(scope.row)">生成内容</el-button><el-button link :disabled="scope.row.status === 'USED'" @click="openEdit(scope.row)">编辑</el-button><el-dropdown><el-button link>更多</el-button><template #dropdown><el-dropdown-menu><el-dropdown-item @click="archive(scope.row)">归档</el-dropdown-item><el-dropdown-item :disabled="scope.row.status === 'USED'" @click="remove(scope.row)">删除</el-dropdown-item></el-dropdown-menu></template></el-dropdown></template></el-table-column>
      </el-table>
      <div style="padding: 18px; display: flex; justify-content: flex-end"><el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load" /></div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑选题' : '新增选题'" width="680px">
      <el-form label-position="top">
        <el-form-item label="选题"><el-input v-model="form.topicTitle" maxlength="200" show-word-limit /></el-form-item>
        <el-form-item label="核心切入角度"><el-input v-model="form.coreAngle" type="textarea" :rows="2" /></el-form-item>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px"><el-form-item label="目标人群"><el-input v-model="form.targetAudience" /></el-form-item><el-form-item label="内容类型"><el-input v-model="form.contentType" /></el-form-item></div>
        <el-form-item label="关键词（逗号分隔）"><el-input v-model="form.keywordsText" /></el-form-item>
        <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px"><el-form-item label="匹配度"><el-input-number v-model="form.targetMatchScore" :min="0" :max="100" /></el-form-item><el-form-item label="爆款潜力"><el-input-number v-model="form.viralPotentialScore" :min="0" :max="100" /></el-form-item><el-form-item label="综合分"><el-input-number v-model="form.overallScore" :min="0" :max="100" /></el-form-item><el-form-item label="优先级"><el-input-number v-model="form.publishPriority" :min="1" :max="10" /></el-form-item></div>
        <el-form-item label="潜力分析"><el-input v-model="form.potentialAnalysis" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" class="xhs-button" @click="saveTopic">保存</el-button></template>
    </el-dialog>
  </div>
</template>
