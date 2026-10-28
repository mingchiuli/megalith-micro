<script lang="ts" setup>
import { diffLines } from 'diff'
import { useI18n } from 'vue-i18n'
import { useHttp } from '@/http/http'
import { API_ENDPOINTS } from '@/config/apiConfig'
import {
  ButtonAuth,
  type BlogVersion,
  type BlogVersionSummary,
  type BlogVersionComparison,
  type PageAdapter
} from '@/type/entity'
import { checkButtonAuth } from '@/utils/permissions'

const props = defineProps<{
  blogId: number
  visible: boolean
}>()
const emit = defineEmits<{
  'update:visible': [visible: boolean]
}>()

const { t } = useI18n()
const { GET, POST } = useHttp()
const visible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value)
})
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)
const versions = ref<BlogVersionSummary[]>([])
const comparison = ref<BlogVersionComparison>()
const compareVisible = ref(false)
const compareLoading = ref(false)
const viewVisible = ref(false)
const viewLoading = ref(false)
const selectedVersion = ref<BlogVersion>()
const restoringVersionId = ref<number>()
const mode = ref<'split' | 'unified'>('split')
let versionRequest = 0

type DiffLine = {
  left: string
  right: string
  leftType: 'same' | 'removed' | 'empty'
  rightType: 'same' | 'added' | 'empty'
  number: number
}

const loadVersions = async () => {
  if (!props.blogId) return
  const request = ++versionRequest
  loading.value = true
  try {
    const url = new URL(API_ENDPOINTS.BLOG_ADMIN.GET_VERSIONS(props.blogId), 'http://localhost')
    url.searchParams.set('currentPage', String(page.value))
    url.searchParams.set('size', String(pageSize))
    if (keyword.value.trim()) url.searchParams.set('keywords', keyword.value.trim())
    const data = await GET<PageAdapter<BlogVersionSummary>>(url.pathname + url.search)
    if (request !== versionRequest) return
    versions.value = data.content
    total.value = data.totalElements
  } catch {
    if (request !== versionRequest) return
    versions.value = []
    total.value = 0
  } finally {
    if (request === versionRequest) loading.value = false
  }
}

const compareWithPrevious = async (version: BlogVersionSummary) => {
  compareLoading.value = true
  compareVisible.value = true
  try {
    comparison.value = await GET<BlogVersionComparison>(
      API_ENDPOINTS.BLOG_ADMIN.COMPARE_VERSION(props.blogId, version.id)
    )
  } catch {
    compareVisible.value = false
  } finally {
    compareLoading.value = false
  }
}

const viewVersion = async (version: BlogVersionSummary) => {
  viewLoading.value = true
  viewVisible.value = true
  try {
    selectedVersion.value = await GET<BlogVersion>(
      API_ENDPOINTS.BLOG_ADMIN.GET_VERSION(props.blogId, version.id)
    )
  } catch {
    viewVisible.value = false
  } finally {
    viewLoading.value = false
  }
}

const restoreVersion = async (version: BlogVersionSummary) => {
  const confirmed = await ElMessageBox.confirm(
    t('admin.restoreVersionConfirm', { version: version.versionNo }),
    t('common.confirm'),
    { type: 'warning' }
  ).catch(() => false)
  if (!confirmed) return
  restoringVersionId.value = version.id
  try {
    await POST<null>(API_ENDPOINTS.BLOG_ADMIN.PUBLISH(props.blogId), {
      revisionId: version.id
    })
    ElNotification({
      title: t('common.operationSuccess'),
      message: t('admin.restoreVersion'),
      type: 'success'
    })
    await loadVersions()
  } catch {
    ElNotification({
      title: t('common.operationFailed'),
      message: t('admin.restoreVersionFailed'),
      type: 'error'
    })
  } finally {
    restoringVersionId.value = undefined
  }
}

const splitLines = computed<DiffLine[]>(() => {
  if (!comparison.value) return []
  const changes = diffLines(comparison.value.previous.content, comparison.value.current.content)
  const rows: DiffLine[] = []
  let number = 1
  for (const change of changes) {
    const lines = change.value.replace(/\n$/, '').split('\n')
    if (change.removed) {
      lines.forEach((line) => {
        rows.push({
          left: line,
          right: '',
          leftType: 'removed',
          rightType: 'empty',
          number: number++
        })
      })
    } else if (change.added) {
      lines.forEach((line) => {
        rows.push({
          left: '',
          right: line,
          leftType: 'empty',
          rightType: 'added',
          number: number++
        })
      })
    } else {
      lines.forEach((line) => {
        rows.push({
          left: line,
          right: line,
          leftType: 'same',
          rightType: 'same',
          number: number++
        })
      })
    }
  }
  return rows
})

const changedFields = computed(() => {
  if (!comparison.value) return []
  const { previous, current } = comparison.value
  return [
    ['title', previous.title, current.title],
    ['description', previous.description, current.description],
    ['link', previous.link, current.link],
    ['renderMode', previous.renderMode, current.renderMode]
  ].filter(([, left, right]) => left !== right)
})

const searchVersions = () => {
  page.value = 1
  void loadVersions()
}

const resetPanel = () => {
  keyword.value = ''
  page.value = 1
  total.value = 0
  versions.value = []
  comparison.value = undefined
  compareVisible.value = false
  compareLoading.value = false
  viewVisible.value = false
  viewLoading.value = false
  selectedVersion.value = undefined
  restoringVersionId.value = undefined
  mode.value = 'split'
}

watch(
  [visible, () => props.blogId],
  ([isVisible, blogId], [wasVisible, previousBlogId]) => {
    if (!isVisible) return
    if (!wasVisible || blogId !== previousBlogId) {
      resetPanel()
      void loadVersions()
    }
  },
  { immediate: true }
)
</script>

<template>
  <el-drawer v-model="visible" size="70%" :title="t('admin.versionHistory')">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        clearable
        :placeholder="t('common.input')"
        @keyup.enter="searchVersions"
      />
      <el-button @click="searchVersions">{{ t('common.search') }}</el-button>
    </div>
    <el-table v-loading="loading" :data="versions" border>
      <el-table-column prop="versionNo" :label="t('admin.versionNo')" width="90" />
      <el-table-column prop="origin" :label="t('admin.versionOrigin')" width="130" />
      <el-table-column prop="title" :label="t('common.title')" min-width="180" />
      <el-table-column :label="t('common.status')" width="130">
        <template #default="{ row }">
          <el-tag v-if="row.currentPublished" type="success">
            {{ t('admin.currentPublished') }}
          </el-tag>
          <el-tag v-else-if="!row.firstPublishedAt" type="info">
            {{ t('common.draft') }}
          </el-tag>
          <el-tag v-else type="warning">{{ t('admin.historicalPublished') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" :label="t('common.updatedAt')" width="180" />
      <el-table-column :label="t('common.operations')" width="260">
        <template #default="{ row }">
          <el-button
            v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_VERSION_VIEW)"
            size="small"
            @click="viewVersion(row as BlogVersionSummary)"
          >
            {{ t('common.view') }}
          </el-button>
          <el-button
            v-if="
              row.firstPublishedAt &&
              !row.currentPublished &&
              checkButtonAuth(ButtonAuth.SYS_EDIT_RESTORE_VERSION)
            "
            size="small"
            type="primary"
            :loading="restoringVersionId === row.id"
            @click="restoreVersion(row as BlogVersionSummary)"
          >
            {{ t('admin.restoreVersion') }}
          </el-button>
          <el-button
            v-if="checkButtonAuth(ButtonAuth.SYS_EDIT_VERSION_COMPARE)"
            size="small"
            :disabled="row.versionNo <= 1"
            @click="compareWithPrevious(row as BlogVersionSummary)"
          >
            {{ t('admin.comparePrevious') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next, total"
      @current-change="loadVersions"
    />
  </el-drawer>

  <el-dialog v-model="viewVisible" width="75%" :title="t('admin.versionView')">
    <el-skeleton :loading="viewLoading" animated :rows="10">
      <template #default>
        <template v-if="selectedVersion">
          <el-descriptions :column="2" border>
            <el-descriptions-item :label="t('admin.versionNo')">
              {{ selectedVersion.versionNo }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('admin.versionOrigin')">
              {{ selectedVersion.origin }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('common.title')">
              {{ selectedVersion.title }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('common.createdAt')">
              {{ selectedVersion.createdAt }}
            </el-descriptions-item>
          </el-descriptions>
          <pre class="version-content">{{ selectedVersion.content }}</pre>
        </template>
      </template>
    </el-skeleton>
  </el-dialog>

  <el-dialog
    v-model="compareVisible"
    width="90%"
    :title="
      t('admin.versionCompare', {
        from: comparison?.previous.versionNo,
        to: comparison?.current.versionNo
      })
    "
  >
    <el-skeleton :loading="compareLoading" animated :rows="12">
      <template #default>
        <el-radio-group v-model="mode" class="compare-mode">
          <el-radio-button value="split">{{ t('admin.splitDiff') }}</el-radio-button>
          <el-radio-button value="unified">{{ t('admin.unifiedDiff') }}</el-radio-button>
        </el-radio-group>
        <el-descriptions v-if="changedFields.length" :column="1" border class="metadata-diff">
          <el-descriptions-item
            v-for="[field, before, after] in changedFields"
            :key="String(field)"
            :label="String(field)"
          >
            <span class="removed">{{ before || '∅' }}</span>
            <span class="arrow">→</span>
            <span class="added">{{ after || '∅' }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="mode === 'split'" class="diff-grid">
          <div class="diff-header">{{ t('admin.previousVersion') }}</div>
          <div class="diff-header">{{ t('admin.currentVersion') }}</div>
          <template v-for="line in splitLines" :key="`${line.number}-${line.left}-${line.right}`">
            <div :class="['diff-line', line.leftType]">
              <span class="line-number">{{ line.number }}</span
              >{{ line.left }}
            </div>
            <div :class="['diff-line', line.rightType]">
              <span class="line-number">{{ line.number }}</span
              >{{ line.right }}
            </div>
          </template>
        </div>

        <div v-else class="diff-unified">
          <template v-for="line in splitLines" :key="`${line.number}-${line.left}-${line.right}`">
            <div v-if="line.leftType !== 'empty'" :class="['diff-line', line.leftType]">
              <span class="line-number">{{ line.number }}</span
              >- {{ line.left }}
            </div>
            <div v-if="line.rightType !== 'empty'" :class="['diff-line', line.rightType]">
              <span class="line-number">{{ line.number }}</span
              >+ {{ line.right }}
            </div>
          </template>
        </div>
      </template>
    </el-skeleton>
  </el-dialog>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.version-content {
  max-height: 60vh;
  overflow: auto;
  white-space: pre-wrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.compare-mode {
  margin-bottom: 14px;
}

.metadata-diff {
  margin-bottom: 14px;
}

.diff-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  border: 1px solid var(--el-border-color);
  max-height: 65vh;
  overflow: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 13px;
}

.diff-unified {
  border: 1px solid var(--el-border-color);
  max-height: 65vh;
  overflow: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 13px;
}

.diff-header {
  position: sticky;
  top: 0;
  z-index: 1;
  padding: 8px;
  font-weight: 600;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color);
}

.diff-line {
  min-height: 22px;
  padding: 2px 8px;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.removed {
  background: color-mix(in srgb, var(--el-color-danger) 18%, transparent);
}

.added {
  background: color-mix(in srgb, var(--el-color-success) 18%, transparent);
}

.empty {
  background: var(--el-fill-color-lighter);
}

.same {
  background: var(--el-bg-color);
}

.line-number {
  display: inline-block;
  width: 42px;
  margin-right: 8px;
  color: var(--el-text-color-secondary);
  text-align: right;
  user-select: none;
}

.arrow {
  margin: 0 10px;
  color: var(--el-text-color-secondary);
}
</style>
