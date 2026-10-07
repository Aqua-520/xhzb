<template>
  <div class="app-container">
    <!--  搜索表单区域  -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="等级名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option
            v-for="dict in nursing_level"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </el-form-item>
    </el-form>

    <!--  操作按钮区域  -->
    <el-row :gutter="10" class="mb8" justify="end">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['nursing:level:add']"
        >新增护理等级</el-button>
      </el-col>
    </el-row>

    <!-- 表格区域   -->
    <el-table v-loading="loading" :data="levelList">
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="护理等级名称" align="center" prop="name" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="执行护理计划" align="center" prop="planName" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column label="护理费用（元/月）" align="center" prop="fee" width="140">
        <template #default="scope">
          <span>{{ scope.row.fee == null ? '' : Number(scope.row.fee).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="等级说明" align="center" prop="description" min-width="150">
        <template #default="scope">
          <el-tooltip
            :disabled="!scope.row.description || scope.row.description.length <= 10"
            :content="scope.row.description"
            placement="top"
          >
            <span>{{ formatDescription(scope.row.description) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="240" class-name="small-padding fixed-width operate-column">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:level:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:level:remove']">删除</el-button>
          <el-button
            link
            :type="scope.row.status === 1 ? 'danger' : 'primary'"
            :icon="scope.row.status === 1 ? 'CircleClose' : 'CircleCheck'"
            @click="handleStatusChange(scope.row)"
            v-hasPermi="['nursing:level:edit']"
          >{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!--分页条码区域-->
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改护理等级对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="levelRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="form.name" maxlength="10" show-word-limit placeholder="请输入" />
        </el-form-item>
        <el-form-item label="护理计划" prop="lplanId">
          <el-select v-model="form.lplanId" placeholder="请设置" clearable style="width: 100%">
            <el-option
              v-for="item in planOptions"
              :key="item.id"
              :label="item.planName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="护理费用" prop="fee">
          <el-input-number v-model="form.fee" :min="0" :precision="2" :step="0.01" controls-position="right" style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="等级说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="50" show-word-limit placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Level">
import { listLevel, getLevel, delLevel, addLevel, updateLevel } from "@/api/nursing/level"
import { listAllPlan } from "@/api/nursing/plan"

const { proxy } = getCurrentInstance()
const { nursing_level } = useDict('nursing_level')

const levelList = ref([])
const planOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    status: undefined,
  },
  rules: {
    name: [
      { required: true, message: "等级名称不能为空", trigger: "blur" }
    ],
    lplanId: [
      { required: true, message: "护理计划不能为空", trigger: "change" }
    ],
    fee: [
      { required: true, message: "护理费用不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "状态不能为空", trigger: "change" }
    ],
    description: [
      { required: true, message: "等级说明不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询护理等级列表 */
function getList() {
  loading.value = true
  listLevel(queryParams.value).then(response => {
    levelList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 查询启用的护理计划（弹窗下拉用） */
function getPlanOptions() {
  listAllPlan({ status: 1 }).then(response => {
    planOptions.value = response.data
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: null,
    name: null,
    lplanId: null,
    fee: 0,
    status: 1,
    description: null
  }
  proxy.resetForm("levelRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增护理等级"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  getLevel(row.id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改护理等级"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["levelRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateLevel(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addLevel(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除护理等级"' + row.name + '"？').then(function() {
    return delLevel(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 启用/禁用按钮操作 */
function handleStatusChange(row) {
  const status = row.status === 1 ? 0 : 1
  const text = status === 1 ? "启用" : "禁用"
  updateLevel({ ...row, status: status }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
    getList()
  })
}

/** 等级说明截断显示（最多10个字，超出显示省略号） */
function formatDescription(text) {
  if (!text) return ""
  return text.length > 10 ? text.slice(0, 10) + "..." : text
}

getList()
getPlanOptions()
</script>

<style scoped>
:deep(.operate-column .cell) {
  white-space: nowrap;
}
</style>
