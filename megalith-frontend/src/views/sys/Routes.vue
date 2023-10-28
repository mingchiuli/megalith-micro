<script lang="ts" setup>
import { GET, POST } from '@/http/http'
import type { MenuSys } from '@/type/entity'
import type { ElTree, FormInstance, FormRules } from 'element-plus'
import { reactive, ref, toRefs } from 'vue'

const dialogVisible = ref(false)
const loading = ref(false)
const tableData = ref<MenuSys[]>([])
const formRef = ref<FormInstance>()

type Form = {
  menuId?: number
  parentId: number
  title: string
  name: string
  url: string
  component: string
  type: number
  icon: string
  orderNum: number
  status: number
}

const form: Form = reactive({
  menuId: undefined,
  parentId: 0,
  title: '',
  name: '',
  url: '',
  component: '',
  type: 0,
  icon: '',
  orderNum: 0,
  status: 0
})

const editFormRules = reactive<FormRules<Form>>({
  parentId: [
    { required: true, message: 'Please select the parent menu', trigger: 'blur' }
  ],
  name: [
    { required: true, message: 'Please enter name', trigger: 'blur' }
  ],
  type: [
    { required: true, message: 'Please select type', trigger: 'blur' }
  ],
  orderNum: [
    { required: true, message: 'Please select order number', trigger: 'blur' }
  ],
  status: [
    { required: true, message: 'Please select status', trigger: 'blur' }
  ]
})

const handleEdit = (row: MenuSys) => {

}

const handleDelete = (row: MenuSys) => {

}

const handleClose = () => {

}

const clearForm = () => {

}

const queryMenus = async () => {
  loading.value = true
  const data = await GET<Array<MenuSys>>('/sys/menu/list')
  tableData.value = data
  loading.value = false
}

const submitForm = async (ref: FormInstance) => {
  await ref.validate(async (valid, _fields) => {
    if (valid) {
      await POST<null>('/sys/menu/save', form)
      ElNotification({
        title: '操作成功',
        message: '编辑成功',
        type: 'success',
      })
      clearForm()
      dialogVisible.value = false
      await queryMenus()
    }
  })
}

const resetForm = (ref: FormInstance) => ref.resetFields();

(async () => {
  await queryMenus()
})()

</script>

<template>
  <el-form :inline="true" @submit.prevent class="button-form">
    <el-form-item>
      <el-button type="primary" @click="dialogVisible = true">新增</el-button>
    </el-form-item>
  </el-form>

  <el-table v-loading="loading" :data="tableData" row-key="menuId" border stripe
    :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">

    <el-table-column prop="title" label="标题" sortable width="180" align="center">
    </el-table-column>

    <el-table-column prop="icon" label="图标" align="center">
    </el-table-column>

    <el-table-column prop="type" label="类型" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.type === 0">分类</el-tag>
        <el-tag size="small" v-else-if="scope.row.type === 1" type="success">菜单</el-tag>
        <el-tag size="small" v-else-if="scope.row.type === 2" type="info">路由</el-tag>
      </template>

    </el-table-column>

    <el-table-column prop="url" label="URL" align="center" />
    <el-table-column prop="component" label="组件" align="center" />
    <el-table-column prop="orderNum" label="排序" align="center" />
    <el-table-column prop="status" label="状态" align="center">
      <template #default="scope">
        <el-tag size="small" v-if="scope.row.status === 0" type="success">Normal</el-tag>
        <el-tag size="small" v-else-if="scope.row.status === 1" type="danger">Disable</el-tag>
      </template>

    </el-table-column>
    <el-table-column prop="icon" label="操作" align="center" fixed="right">
      <template #default="scope">
        <el-button size="small" type="success" @click="handleEdit(scope.row)">编辑</el-button>
        <el-popconfirm title="确定删除?" @confirm="handleDelete(scope.row)">
          <template #reference>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </el-table-column>

  </el-table>

  <el-dialog title="新增/编辑" v-model="dialogVisible" width="600px" :before-close="handleClose">

    <el-form :model="form" :rules="editFormRules" ref="formRef" label-width="100px">

      <el-form-item label="Parent menu" prop="parentId">
        <el-select v-model="form.parentId" placeholder="Please select the parent menu">
          <template v-for="item in tableData">
            <el-option :label="item.title" :value="item.menuId"></el-option>
            <template v-for="child in item.children">
              <el-option :label="child.title" :value="child.menuId">
                <span>{{ "- " + child.title }}</span>
              </el-option>
            </template>
          </template>
        </el-select>
      </el-form-item>

      <el-form-item label="Title" prop="title" label-width="100px">
        <el-input v-model="form.title" autocomplete="off"></el-input>
      </el-form-item>

      <el-form-item label="Icon" prop="icon" label-width="100px">
        <el-input v-model="form.icon" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="Url" prop="url" label-width="100px">
        <el-input v-model="form.url" autocomplete="off"></el-input>
      </el-form-item>

      <el-form-item label="Component" prop="component" label-width="100px">
        <el-input v-model="form.component" autocomplete="off"></el-input>
      </el-form-item>

      <el-form-item label="Type" prop="type" label-width="100px">
        <el-radio-group v-model="form.type">
          <el-radio :label=0>Catalogue</el-radio>
          <el-radio :label=1>Menu</el-radio>
          <el-radio :label=2>Route</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="Status" prop="status" label-width="100px">
        <el-radio-group v-model="form.status">
          <el-radio :label=0>Normal</el-radio>
          <el-radio :label=1>Disable</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="Order Number" prop="orderNum" label-width="100px">
        <el-input-number v-model="form.orderNum" :min="1" label="Order Number">1</el-input-number>
      </el-form-item>


      <el-form-item>
        <el-button type="primary" @click="submitForm(formRef!)">Submit</el-button>
        <el-button @click="resetForm(formRef!)">Reset</el-button>
      </el-form-item>
    </el-form>

  </el-dialog>
</template>

<style scoped>
.button-form .el-form-item {
  margin-right: 10px;
}
</style>