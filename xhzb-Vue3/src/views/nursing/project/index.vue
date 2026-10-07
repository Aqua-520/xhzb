<template>
  <div class="app-container">
    <!--  搜索表单区域  -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
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
          v-hasPermi="['nursing:project:add']"
        >新增护理项目</el-button>
      </el-col>
    </el-row>

    <!-- 表格区域   -->
    <el-table v-loading="loading" :data="projectList">
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="名称" align="center" prop="name" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="排序号" align="center" prop="orderNo" width="90" />
      <el-table-column label="单位" align="center" prop="unit" width="90" />
      <el-table-column label="价格（元）" align="center" prop="price" width="100" />
      <el-table-column label="图片" align="center" prop="image" width="100">
        <template #default="scope">
          <image-preview :src="scope.row.image" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="护理要求" align="center" prop="nursingRequirement" min-width="130">
        <template #default="scope">
          <el-tooltip
            :disabled="!scope.row.nursingRequirement || scope.row.nursingRequirement.length <= 5"
            :content="scope.row.nursingRequirement"
            placement="top"
          >
            <span>{{ formatNursingRequirement(scope.row.nursingRequirement) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
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
      <el-table-column label="操作" align="center" width="240" class-name="small-padding fixed-width operate-column">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['nursing:project:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['nursing:project:remove']">删除</el-button>
          <el-button
            link
            :type="scope.row.status === 1 ? 'danger' : 'primary'"
            :icon="scope.row.status === 1 ? 'CircleClose' : 'CircleCheck'"
            @click="handleStatusChange(scope.row)"
            v-hasPermi="['nursing:project:edit']"
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

    <!-- 添加或修改护理项目对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="projectRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" maxlength="10" show-word-limit placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="排序号" prop="orderNo">
              <el-input-number v-model="form.orderNo" :min="0" controls-position="right" placeholder="请输入排序号" style="width: 150px" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" maxlength="5" show-word-limit placeholder="请输入单位" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.01" controls-position="right" placeholder="请输入价格" style="width: 150px" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="图片" prop="image">
              <image-upload v-model="form.image"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="护理要求" prop="nursingRequirement">
              <el-input v-model="form.nursingRequirement" type="textarea" :rows="3" maxlength="50" show-word-limit placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
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

<script setup name="Project">
import { listProject, getProject, delProject, addProject, updateProject } from "@/api/nursing/project"

const { proxy } = getCurrentInstance()
const { nursing_project_status } = useDict('nursing_project_status')

const projectList = ref([])
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
      { required: true, message: "名称不能为空", trigger: "blur" }
    ],
    price: [
      { required: true, message: "价格不能为空", trigger: "blur" }
    ],
    image: [
      { required: true, message: "图片不能为空", trigger: "change" }
    ],
    nursingRequirement: [
      { required: true, message: "护理要求不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询护理项目列表 */
function getList() {
  loading.value = true
  listProject(queryParams.value).then(response => {
    projectList.value = response.rows
    total.value = response.total
    loading.value = false
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
    orderNo: null,
    unit: null,
    price: null,
    image: null,
    nursingRequirement: null,
    status: 1,
    createBy: null,
    updateBy: null,
    remark: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("projectRef")
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
  title.value = "新增护理项目"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  getProject(row.id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改护理项目"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["projectRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProject(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProject(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除护理项目编号为"' + row.id + '"的数据项？').then(function() {
    return delProject(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 启用/禁用按钮操作 */
function handleStatusChange(row) {
  const status = row.status === 1 ? 0 : 1
  const text = status === 1 ? "启用" : "禁用"
  updateProject({ ...row, status: status }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
    getList()
  })
}

/** 护理要求截断显示（最多5个字，超出显示省略号） */
function formatNursingRequirement(text) {
  if (!text) return ""
  return text.length > 5 ? text.slice(0, 5) + "..." : text
}

getList()
</script>

<style scoped>
:deep(.operate-column .cell) {
  white-space: nowrap;
}
</style>
