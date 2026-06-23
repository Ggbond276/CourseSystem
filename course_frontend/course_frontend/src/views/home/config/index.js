// 课程搜索栏控件配置
export const courseSearchFieldsConfig = [
  {
    type: 'input',
    label: '课程名称',
    prop: 'courseName',
    size: 'large',
    placeholder: '请输入课程名称'
  },
  {
    type: 'input',
    label: '课程编号',
    prop: 'courseNum',
    size: 'large',
    placeholder: '请输入课程编号'
  }
]

// 课程表格列配置
export const courseTableColumnConfig = [
  {
    width: 60,
    type: 'index',
    label: '序号'
  },
  {
    prop: 'courseName',
    label: '课程名称'
  },
  {
    prop: 'courseNum',
    label: '课程号'
  },
  {
    prop: 'period',
    label: '学时'
  },
  {
    prop: 'credit',
    label: '学分'
  },
  {
    prop: 'status',
    label: '课程状态',
    mapping: (row) => {return row.status === 1 ? '启用' : '停用'}
  },
  {
    prop: 'createTime',
    label: '创建时间'
  }
]

// 新增或编辑课程表单配置
export const addOrEditStudentFormConfig = [
  {
    type: 'input',
    label: '课程名称',
    prop: 'courseName',
    placeholder: '请输入课程名称'
  },
  {
    type: 'number',
    label: '学时',
    prop: 'period',
    width: '100%',
    min: 0,
    placeholder: '请输入学时'
  },
  {
    type: 'number',
    label: '学分',
    prop: 'credit',
    width: '100%',
    min: 0,
    placeholder: '请输入学分'
  },
  {
    type: 'select',
    label: '课程状态',
    prop: 'status',
    placeholder: '请选择课程状态',
    options: [
      {
        label: '启用',
        value: 1
      },
      {
        label: '停用',
        value: 0
      }
    ]
  }
]