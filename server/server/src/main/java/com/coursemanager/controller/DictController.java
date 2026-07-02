package com.coursemanager.controller;

import com.coursemanager.pojo.Department;
import com.coursemanager.pojo.Term;
import com.coursemanager.service.IDepartmentService;
import com.coursemanager.service.ITermService;
import com.coursemanager.utils.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典接口 Controller
 *
 * 用途：把"学院统一发布"的字典数据（学期、学院）以 REST 形式开放给前端做下拉选择。
 * 设计原则：
 * 1. 字典数据只读，前端只能 GET，不能 POST/PUT/DELETE
 * 2. 字典变更由数据库管理员（学院/学校）通过 SQL 维护，不开放给业务前端
 * 3. 接口命名 /dict/term /dict/department，路径前缀清晰，方便前端 import
 *
 * @author 架构师
 */
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final ITermService termService;
    private final IDepartmentService departmentService;

    /**
     * 查询所有学期（按学年倒序）
     * GET /dict/term/list
     * 响应：{ code, msg, data: [ { id, schoolYear, semester, displayName, startDate, endDate, isCurrent } ] }
     */
    @GetMapping("/term/list")
    public CommonResult<?> termList() {
        List<Term> data = termService.listAllSorted();
        return CommonResult.success(data, "查询成功");
    }

    /**
     * 查询当前学期（is_current=1）
     * GET /dict/term/current
     * 响应：{ code, msg, data: { id, displayName, ... } | null }
     */
    @GetMapping("/term/current")
    public CommonResult<?> termCurrent() {
        Term current = termService.getCurrentTerm();
        return CommonResult.success(current, "查询成功");
    }

    /**
     * 查询所有学院
     * GET /dict/department/list
     * 响应：{ code, msg, data: [ { id, schoolId, name, code } ] }
     */
    @GetMapping("/department/list")
    public CommonResult<?> departmentList() {
        List<Department> data = departmentService.listAllSorted();
        return CommonResult.success(data, "查询成功");
    }
}