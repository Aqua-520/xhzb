<template>
  <div class="app-container">
    <!--  搜索表单区域  -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="名称" prop="planName">
        <el-input
          v-model="queryParams.planName"
          placeholder="请输入"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option
            v-for="dict in nursing_project_status"
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
          v-hasPermi="['nursing:plan:add']"
        >新增护理计划</el-button>
      </el-col>
    </el-row>

    <!-- 表格区域   -->
    <el-table v-loading="loading" :data="planList">
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="名称" align="center" prop="planName" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width operate-column">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:plan:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:plan:remove']">删除</el-button>
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['nursing:plan:query']">查看</el-button>
          <el-button
            link
            :type="scope.row.status === 1 ? 'danger' : 'primary'"
            :icon="scope.row.status === 1 ? 'CircleClose' : 'CircleCheck'"
            @click="handleStatusChange(scope.row)"
            v-hasPermi="['nursing:plan:edit']"
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

    <!-- 添加、修改或查看护理计划对话框 -->
    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="planRef" :model="form" :rules="rules" label-width="110px" :disabled="isView">
        <el-form-item label="护理计划名称" prop="planName">
          <el-input v-model="form.planName" maxlength="10" show-word-limit placeholder="请输入" style="width: 320px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="1" controls-position="right" style="width: 150px" />
        </el-form-item>
        <el-form-item label="护理项目">
          <el-table :data="form.projectPlans" border>
            <el-table-column label="护理项目名称" align="center" min-width="160">
              <template #default="scope">
                <el-select v-model="scope.row.projectId" placeholder="请选择" clearable style="width: 100%">
                  <el-option
                    v-for="item in projectOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="期望服务时间" align="center" width="160">
              <template #default="scope">
                <el-time-picker
                  v-model="scope.row.executeTime"
                  format="HH:mm"
                  value-format="HH:mm"
                  placeholder="08:00"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="执行周期" align="center" width="130">
              <template #default="scope">
                <el-select v-model="scope.row.executeCycle" placeholder="请选择" style="width: 100%">
                  <el-option label="每日" :value="0" />
                  <el-option label="每周" :value="1" />
                  <el-option label="每月" :value="2" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="执行频次（次）" align="center" width="150">
              <template #default="scope">
                <el-input-number v-model="scope.row.executeFrequency" :min="1" :controls="false" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="110" v-if="!isView">
              <template #default="scope">
                <el-button link type="danger" icon="Minus" @click="handleRemoveProject(scope.$index)" />
                <el-button v-if="scope.$index === 0" link type="primary" icon="Plus" @click="handleAddProject" />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button v-if="!isView" type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Plan">
import { listPlan, getPlan, delPlan, addPlan, updatePlan } from "@/api/nursing/plan"
import { listAllProject } from "@/api/nursing/project"

const { proxy } = getCurrentInstance()
const { nursing_project_status } = useDict('nursing_project_status')

const planList = ref([])
const projectOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const isView = ref(false)

/** 护理项目子表空行 */
function emptyProjectPlan() {
  return {
    projectId: undefined,
    executeTime: "08:00",
    executeCycle: 0,
    executeFrequency: 1
  }
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planName: undefined,
    status: undefined,
  },
  rules: {
    planName: [
      { required: true, message: "护理计划名称不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "状态不能为空", trigger: "change" }
    ],
    sortNo: [
      { required: true, message: "排序不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询护理计划列表 */
function getList() {
  loading.value = true
  listPlan(queryParams.value).then(response => {
    planList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 查询启用的护理项目（弹窗下拉用） */
function getProjectOptions() {
  listAllProject({ status: 1 }).then(response => {
    projectOptions.value = response.data
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
    planName: null,
    status: 1,
    sortNo: 1,
    projectPlans: [emptyProjectPlan()]
  }
  isView.value = false
  proxy.resetForm("planRef")
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
  title.value = "新建护理计划"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  getPlan(row.id).then(response => {
    form.value = response.data
    // 后端暂未返回项目明细，保证至少一行
    if (!form.value.projectPlans || form.value.projectPlans.length === 0) {
      form.value.projectPlans = [emptyProjectPlan()]
    }
    open.value = true
    title.value = "修改护理计划"
  })
}

/** 查看按钮操作 */
function handleView(row) {
  reset()
  getPlan(row.id).then(response => {
    form.value = response.data
    if (!form.value.projectPlans || form.value.projectPlans.length === 0) {
      form.value.projectPlans = [emptyProjectPlan()]
    }
    isView.value = true
    open.value = true
    title.value = "查看护理计划"
  })
}

/** 添加一行护理项目 */
function handleAddProject() {
  form.value.projectPlans.push(emptyProjectPlan())
}

/** 删除一行护理项目（至少保留一行） */
function handleRemoveProject(index) {
  if (form.value.projectPlans.length > 1) {
    form.value.projectPlans.splice(index, 1)
  }
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["planRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updatePlan(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPlan(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除护理计划"' + row.planName + '"？').then(function() {
    return delPlan(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 启用/禁用按钮操作 */
function handleStatusChange(row) {
  const status = row.status === 1 ? 0 : 1
  const text = status === 1 ? "启用" : "禁用"
  updatePlan({ ...row, status: status }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
    getList()
  })
}

getList()
getProjectOptions()
</script>

<style scoped>
:deep(.operate-column .cell) {
  white-space: nowrap;
}
</style>
