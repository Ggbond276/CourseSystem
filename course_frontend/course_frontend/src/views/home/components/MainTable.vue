<template>
  <header class="title">
    {{ props.title }}信息管理
  </header>
  <div class="table-control">
    <div class="control-left">
      <el-button
          type="primary"
          size="large"
          @click="handleAdd"
      >新增
      </el-button>
    </div>
    <div class="control-right">
      <el-space :size="10">
        <template v-for="item in searchFieldConfig">
          <el-input
              v-if="item.type==='input'"
              v-model="search[item.prop]"
              :placeholder="item.placeholder"
              style="width: 150px"
              :size="item.size"/>
          <el-select
              v-else-if="item.type==='select'"
              v-model="search[item.prop]"
              :placeholder="item.placeholder"
              style="width: 150px"
              :size="item.size"
          >
            <el-option
                v-for="option in item.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
            />
          </el-select>
        </template>
      </el-space>
      <el-button
          size="large"
          type="info"
          style="margin-left: 20px"
          @click="handleReset"
      >
        重置
      </el-button>
      <el-button
          type="primary"
          style="margin-left: 10px"
          size="large"
          @click="handleSearch"
      >搜索
      </el-button>
    </div>
  </div>
  <div class="table" v-loading="isLoading">
    <el-table
        :data="tableData"
        :max-height="600"
        show-header
        style="width:100%; height: 600px"
        size="large"
        stripe
    >
      <el-table-column
          v-for="item in tableColumnConfig"
          :key="item.prop"
          :type="item.type??null"
          :prop="item.prop"
          :label="item.label"
          :width="item.width??''"
          :index="item.type==='index'?(row)=>(pager.pageNum-1)*pager.pageSize+row+1:null"
          align="center"
      >
        <template
            v-if="item.mapping"
            #default="{row}"
        >
          {{ item.mapping(row) }}
        </template>
      </el-table-column>
      <el-table-column
          label="操作"
          align="center"
      >
        <template #default="{row}">
          <div class="operation">
            <el-button
                type="primary"
                @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-popconfirm
                width="220"
                confirm-button-text="确认"
                cancel-button-text="取消"
                icon-color="#626AEF"
                title="确认删除?"
                :hide-after="0.5"
                @confirm="()=>handleDeleteSingle(row)"
            >
              <template #reference>
                <el-button
                    type="danger"
                >
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <!--    分页 -->
    <div class="pager">
      <el-pagination
          :current-page="pager.pageNum"
          :page-size="pager.pageSize"
          :total="pager.total"
          :page-sizes="[5,10, 20, 30, 40]"
          layout="total, sizes, prev, pager, next, jumper,slot"
          background
          @size-change="sizeChange"
          @current-change="currentChange"/>
    </div>
  </div>
  <!--    弹窗-->
  <el-dialog
      v-model="visible"
      :title="dialogTitle"
      @close="cancel"
  >
    <EditForm
        ref="editFormRef"
        :formFieldConfig="addOrEditFormFieldConfig"
        :form="form"
        :rulesConfig="rulesConfig"
        size="large"/>
    <template #footer>
      <div class="button-group">
        <el-button size="large" @click="cancel">取消</el-button>
        <el-button type="primary" size="large" @click="submit">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>

import { nextTick, ref } from 'vue'
import EditForm from '@/views/home/components/EditForm.vue'
import { dialogType } from '@/constant/index.js'
import { ElMessage } from 'element-plus'
import { addCourseAPI, deleteCourseAPI, fetchCourseList, updateCourseAPI } from '@/api/index.js'

const props = defineProps({
  title: String, // 表格标题
  searchFieldConfig: Array, // 搜索字段配置
  tableColumnConfig: Array, // 表格列字段配置
  addOrEditFormFieldConfig:Array, // 新增/编辑表单字段配置
  rulesConfig:Object, // 字段校验规则
  search: Object, // 搜索对象
  form: Object, // 新增/编辑表单对象
})
const isLoading = ref(true)
const tableData = ref([])
// 弹窗是否显示
const visible = ref(false)
// 弹窗标题
const dialogTitle = ref(dialogType.ADD)
// 分页
const pager = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
// 弹窗表单的数据
const form = ref({ ...props.form })

// 点击新增按钮
const handleAdd = () => {
  visible.value = true
  dialogTitle.value = dialogType.ADD
}

// 新增
const add = () => {
  addCourseAPI(form.value).then(() => {
    ElMessage.success('新增成功')
  }).catch(err => {
    ElMessage.error(err.response.data.msg || '新增失败')
  }).finally(() => {
    cancel()
    handleReset()
  })
}

// 编辑btn
const handleEdit = (row) => {
  visible.value = true
  dialogTitle.value = dialogType.EDIT
  nextTick(() => form.value = { ...row })
}

// 编辑
const edit = () => {
  const params = {id:form.value.id}
  // 只拿出需要的字段
  Object.keys(form.value).forEach(key => {
    if (key in props.form) {
      params[key] = form.value[key]
    }
  })
  updateCourseAPI(params).then(() => {
    ElMessage.success('修改成功')
  }).catch(err => {
    ElMessage.error(err.response.data.msg || '修改失败')
  }).finally(() => {
    cancel()
    handleReset()
  })
}

// 提交表单
const submit = () => {
  editFormRef.value.formRef.validate(valid => {
    if (!valid) {
      return
    }

    if (dialogTitle.value === dialogType.ADD) {
      add()
    } else {
      edit()
    }
  })
}

// 删除单个
const handleDeleteSingle = (row) => {
  deleteCourseAPI(row.id).then(res => {
    console.log(res)
    if (res.data.code !== 200) {
      ElMessage.error('删除失败')
      return
    }
    ElMessage.success(res.message || '删除成功')
    fetchData()
  })
}

// 搜索
// 搜索条件
const search = ref({ ...props.search })

// 点击搜索按钮
const handleSearch = () => {
  pager.value.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  search.value = { ...props.search }
  pager.value.pageNum = 1
  fetchData()
}

// 取消弹窗
const editFormRef = ref(null)
const cancel = () => {
  visible.value = false
  editFormRef.value.formRef.resetFields()
}

// 分页
const sizeChange = (val) => {
  pager.value.pageSize = val
  fetchData()
}
const currentChange = (val) => {
  pager.value.pageNum = val
  fetchData()
}

// 去除参数中的空值
const removeEmptyValue = (params) => {
  for (let key in params) {
    if (params[key] === '') {
      delete params[key]
    }
  }
  return params
}

// 获取数据
const fetchData = () => {
  isLoading.value = true
  const params = { ...search.value, pageNum: pager.value.pageNum, pageSize: pager.value.pageSize }
  fetchCourseList(removeEmptyValue(params)).then(res => {
    if (res.data.code !== 200) {
      ElMessage.error('查询失败')
      return
    }
    console.log(res.data)
    tableData.value = res.data.data.list
    pager.value.total = res.data.data.total

  }).finally(() => {
    isLoading.value = false
  })
}

fetchData()
</script>

<style scoped>

.title {
  color: #409EFF;
  text-align: center;
  font-size: 30px;
  margin-bottom: 20px;
}

.table-control {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.control-left, .control-right {
  flex: auto;
}

.control-right {
  display: flex;
  justify-content: end;
}

.table {
  width: 100%;
  border: 10px solid white;
  border-radius: 10px;
  box-shadow: 0 2px 10px gray;
}

.operation {
  display: flex;
  justify-content: center;
  align-items: center;
}

.pager {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  justify-content: end;
  background-color: white;
  padding: 10px 0;
}
</style>