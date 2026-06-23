<template>
  <div class="wrapper">
    <MainTable
        title="课程"
        :search-field-config="courseSearchFieldsConfig"
        :table-column-config="courseTableColumnConfig"
        :addOrEditFormFieldConfig="addOrEditStudentFormConfig"
        :rulesConfig="addOrEditFormRules"
        :search="search"
        :form="form"
    />
  </div>

</template>
<script setup>
import { courseSearchFieldsConfig, courseTableColumnConfig,addOrEditStudentFormConfig} from '@/views/home/config/index.js'
import { addOrEditFormRules } from '@/views/home/config/rules.js'
import MainTable from '@/views/home/components/MainTable.vue'


//  只适用于一层对象
const search = {
  courseName: '',
  courseNum: ''
}

const form = {
  courseName: '',
  period: 0,
  credit: 0,
  status: ''
}
</script>

<style scoped>
.wrapper {
  padding: 40px 0;
  margin: 0 auto;
  width: 1600px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}
</style>