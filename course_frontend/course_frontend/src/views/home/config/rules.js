export const addOrEditFormRules = {
  courseName: [
    {
      required: true, message: '课程名字不能为空', trigger: 'blur'
    },
  ],
  period: [
    {
      required: true, message: '课程学时不能为空', trigger: 'blur'
    },
  ],
  credit: [
    {
      required: true, message: '课程学分不能为空', trigger: 'blur'
    },
  ],
  status: [
    {
      required: true, message: '请选择课程状态', trigger: 'change'
    },
  ],
}