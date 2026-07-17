<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { contentApi, reviewApi } from '@/api/modules'
import type { AiReview, ContentCard, ContentDetail, ContentTitle } from '@/types'

const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id as string | undefined)
const loading = ref(false)
const saving = ref(false)
const reviewing = ref(false)
const review = ref<AiReview>()
const status = ref('DRAFT')
const form = reactive({ topicId: undefined as string | undefined, selectedTitle: '', body: '', coverTitle: '', coverSubtitle: '', tags: [] as string[], interactionGuide: '', titles: [{ text: '', sortOrder: 1, attractionScore: 80 }] as ContentTitle[], cards: [{ cardNo: 1, title: '', body: '' }] as ContentCard[] })
const readonly = computed(() => ['PUBLISHED', 'ARCHIVED'].includes(status.value))
const statusLabel = computed(() => ({ DRAFT: '草稿', PENDING_REVIEW: '待审核', READY_TO_PUBLISH: '待发布', PUBLISHED: '已发布', ARCHIVED: '已归档' }[status.value] ?? status.value))

function apply(detail: ContentDetail) {
  status.value = detail.status
  review.value = detail.latestReview
  Object.assign(form, { topicId: detail.topicId, selectedTitle: detail.selectedTitle, body: detail.body, coverTitle: detail.coverTitle, coverSubtitle: detail.coverSubtitle, tags: detail.tags, interactionGuide: detail.interactionGuide, titles: detail.titles, cards: detail.cards })
}

async function load() {
  if (!id.value) return
  loading.value = true
  try { apply(await contentApi.detail(id.value)) } finally { loading.value = false }
}

function payload() {
  return { id: id.value, topicId: form.topicId, selectedTitle: form.selectedTitle, body: form.body, coverTitle: form.coverTitle, coverSubtitle: form.coverSubtitle, tags: form.tags, interactionGuide: form.interactionGuide, titles: form.titles.map((v, i) => ({ text: v.text, sortOrder: i + 1, attractionScore: v.attractionScore ?? 80 })), cards: form.cards.map((v, i) => ({ cardNo: i + 1, title: v.title, body: v.body })) }
}

function validate() {
  if (!form.selectedTitle || !form.body || !form.coverTitle || !form.coverSubtitle || !form.interactionGuide || !form.tags.length) { ElMessage.warning('请完整填写标题、正文、封面、标签和互动语'); return false }
  if (form.titles.some(v => !v.text) || form.cards.some(v => !v.title || !v.body)) { ElMessage.warning('候选标题和卡片文案不能留空'); return false }
  return true
}

async function save(silent = false) {
  if (!validate()) return undefined
  saving.value = true
  try {
    const detail = id.value ? await contentApi.update(payload()) : await contentApi.create(payload())
    if (!id.value) await router.replace(`/contents/${detail.id}`)
    apply(detail)
    if (!silent) ElMessage.success('内容已保存')
    return detail
  } finally { saving.value = false }
}

async function executeReview() {
  const saved = await save(true)
  const contentId = id.value ?? saved?.id
  if (!contentId) return
  reviewing.value = true
  try { review.value = await reviewApi.execute(contentId); status.value = 'PENDING_REVIEW'; ElMessage.success('AI 审核完成，请人工确认') } finally { reviewing.value = false }
}

async function confirm(approved: boolean) {
  if (!id.value) return
  await reviewApi.confirm(id.value, approved)
  status.value = approved ? 'READY_TO_PUBLISH' : 'DRAFT'
  ElMessage.success(approved ? '人工确认通过，内容已待发布' : '已退回草稿')
}

async function copyAll() {
  if (!id.value) return
  const data = await contentApi.copy(id.value)
  await navigator.clipboard.writeText(`${data.title}\n\n${data.bodyWithTags}\n\n${data.interactionGuide}`)
  ElMessage.success('发布文案已复制')
}

function selectTitle(title: ContentTitle) { form.selectedTitle = title.text }
function addTitle() { if (form.titles.length < 5) form.titles.push({ text: '', sortOrder: form.titles.length + 1, attractionScore: 80 }) }
function addCard() { if (form.cards.length < 6) form.cards.push({ cardNo: form.cards.length + 1, title: '', body: '' }) }

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="page-hero"><div><div class="toolbar"><h2 style="margin:0">{{ id ? '内容工作台' : '新建内容' }}</h2><el-tag>{{ statusLabel }}</el-tag></div><p>编辑真实表达，完成 AI 审核后由你做最终发布决定</p></div><div class="toolbar"><el-button @click="router.push('/contents')">返回列表</el-button><el-button v-if="id" @click="copyAll">复制发布文案</el-button><el-button v-if="!readonly" :loading="saving" @click="save(false)">保存</el-button><el-button v-if="!readonly" type="primary" class="xhs-button" :loading="reviewing" @click="executeReview">保存并 AI 审核</el-button></div></div>
    <div style="display:grid;grid-template-columns:minmax(0,1.45fr) minmax(340px,.75fr);gap:18px">
      <div class="panel"><div class="panel-header"><h3>正文与标题</h3><span class="muted">保持真实，不承诺收益</span></div><div class="panel-body">
        <el-form label-position="top" :disabled="readonly">
          <el-form-item label="当前标题"><el-input v-model="form.selectedTitle" maxlength="100" show-word-limit /></el-form-item>
          <el-form-item label="5 个候选标题"><div style="width:100%;display:grid;gap:8px"><div v-for="(title,index) in form.titles" :key="index" style="display:flex;gap:8px"><el-input v-model="title.text" @focus="selectTitle(title)"><template #prepend>{{ index + 1 }}</template></el-input><el-input-number v-model="title.attractionScore" :min="0" :max="100" style="width:110px" /><el-button v-if="form.titles.length>1" @click="form.titles.splice(index,1)">×</el-button></div><el-button v-if="form.titles.length<5" plain @click="addTitle">添加候选标题</el-button></div></el-form-item>
          <el-form-item label="小红书正文"><el-input v-model="form.body" type="textarea" :rows="16" maxlength="10000" show-word-limit /></el-form-item>
          <el-form-item label="话题标签"><el-select v-model="form.tags" multiple filterable allow-create default-first-option placeholder="输入后回车" style="width:100%" /></el-form-item>
          <el-form-item label="评论区互动引导"><el-input v-model="form.interactionGuide" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
        </el-form>
      </div></div>
      <div style="display:grid;gap:18px;align-content:start">
        <div class="panel"><div class="panel-header"><h3>封面预览</h3></div><div class="panel-body"><div style="aspect-ratio:3/4;border-radius:14px;padding:30px 24px;background:linear-gradient(155deg,#ff4c65,#ff2442 55%,#c9092c);color:white;display:flex;flex-direction:column;justify-content:center;box-shadow:inset 0 0 0 1px #ffffff30"><small style="letter-spacing:.15em">PROGRAMMER × AI</small><strong style="font-size:34px;line-height:1.2;margin:18px 0">{{ form.coverTitle || '封面主标题' }}</strong><span style="line-height:1.6">{{ form.coverSubtitle || '封面副标题' }}</span></div><el-form label-position="top" :disabled="readonly" style="margin-top:18px"><el-form-item label="封面主标题"><el-input v-model="form.coverTitle" maxlength="50" /></el-form-item><el-form-item label="封面副标题"><el-input v-model="form.coverSubtitle" maxlength="80" /></el-form-item></el-form></div></div>
        <div v-if="review" class="panel"><div class="panel-header"><h3>AI 审核报告</h3><el-tag :type="review.riskLevel==='LOW'?'success':review.riskLevel==='MEDIUM'?'warning':'danger'">{{ review.riskLevel }} 风险</el-tag></div><div class="panel-body"><div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:16px"><el-statistic title="自然表达" :value="review.aiToneScore" /><el-statistic title="标题吸引力" :value="review.titleAttractionScore" /><el-statistic title="真实性" :value="review.authenticityScore" /><el-statistic title="平台适配" :value="review.platformFitScore" /></div><p style="line-height:1.7">{{ review.summary }}</p><ul style="padding-left:18px;color:#6f696c;line-height:1.8"><li v-for="item in review.suggestions" :key="item">{{ item }}</li></ul><el-alert v-if="review.exaggeratedIncomeRisk || review.sensitiveExpressionRisk || review.marketingRisk" title="存在风险项，请先修改并重新审核" type="warning" :closable="false" /><div v-if="status==='PENDING_REVIEW'" class="toolbar" style="margin-top:16px"><el-button @click="confirm(false)">退回修改</el-button><el-button type="primary" class="xhs-button" :disabled="review.riskLevel==='HIGH'" @click="confirm(true)">人工确认通过</el-button></div></div></div>
      </div>
    </div>
    <div class="panel" style="margin-top:18px"><div class="panel-header"><h3>6 张图文卡片</h3><el-button v-if="!readonly && form.cards.length<6" link @click="addCard">添加卡片</el-button></div><div class="panel-body" style="display:grid;grid-template-columns:repeat(3,1fr);gap:14px"><div v-for="(card,index) in form.cards" :key="index" style="border:1px solid var(--line);border-radius:12px;padding:14px"><strong style="color:var(--xhs-red)">CARD {{ index+1 }}</strong><el-input v-model="card.title" :disabled="readonly" placeholder="卡片标题" style="margin:10px 0" /><el-input v-model="card.body" :disabled="readonly" type="textarea" :rows="4" placeholder="卡片正文" /><el-button v-if="!readonly && form.cards.length>1" link style="margin-top:6px" @click="form.cards.splice(index,1)">删除卡片</el-button></div></div></div>
  </div>
</template>
