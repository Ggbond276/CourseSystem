<template>
  <div class="form-container">
    <el-form
        ref="formRef"
        :model="props.form"
        :rules="rulesConfig"
        size="large"
    >
      <template v-for="item in props.formFieldConfig">
        <el-form-item
            label-width="120px"
            :prop="item.prop"
            :label="item.label"
        >
          <el-input
              v-if="item.type==='input'"
              v-model="props.form[item.prop]"
              :placeholder="item.placeholder"
              :style="{width: item.width}"
          />
          <el-select
              v-else-if="item.type==='select'"
              v-model="props.form[item.prop]"
              :placeholder="item.placeholder"
              :style="{width: item.width}"
          >
            <el-option
                v-for="option in item.options"
                :key="option"
                :label="option.label"
                :value="option.value"
                :style="{width: item.width}"
            />
          </el-select>
          <el-input-number
              v-else-if="item.type==='number'"
              v-model="props.form[item.prop]"
              :placeholder="item.placeholder"
              :style="{width: item.width}"
              :min="item.min"
              :max="item.max"
          />
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup>

import { ref } from 'vue'

const props = defineProps({
  form: {
    type: Object,
    required: true,
    default: () => Object.create({})
  }, // 表单对象
  formFieldConfig: {
    type: Array,
    required: true,
    default: () => []
  }, // 表单字段配置
  rulesConfig:{
    type:Object,
    required:true,
    default:{}
  } // 校验规则配置
})

// 表单的实例
const formRef = ref(null)

defineExpose({
  formRef
})
</script>

<style scoped>
.form-container {
  padding: 20px 60px;
}
</style>