package com.coursemanager.controller;

import com.coursemanager.utils.CommonResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author 架构师
 * @date 2024/06/28
 * @description 通用文件上传接口（本地存储版）
 *
 * 适用场景：学生作业附件上传（后续可平滑替换为 OSS / MinIO）
 *
 * 接口：
 *   POST /upload/file  form-data: file=<二进制>
 *   响应：{ code, msg, data: { fileName, fileUrl, originalName, size } }
 *
 * 设计要点：
 *   1) 文件落地到 ./uploads/{yyyyMMdd}/{uuid}.{ext} —— 启动时自动建目录
 *   2) 对外暴露的访问 URL 是 /uploads/{yyyyMMdd}/{uuid}.{ext}，由 WebMvcConfig 做静态映射
 *   3) 不限制文件类型（学习场景），但限制单文件大小（后续可改为 application.yml 配置）
 *   4) 同名文件永不冲突：UUID 重命名
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    /**
     * 业务上传目录（相对项目根目录）
     * 与 WebMvcConfig.addResourceHandlers 中的路径一一对应
     */
    private static final String UPLOAD_BASE_DIR = "uploads";

    /**
     * 单文件大小上限：20MB（封面 PPT / 报告 PDF 足够；视频/大文件应走对象存储）
     */
    private static final long MAX_FILE_SIZE = 20L * 1024L * 1024L;

    /**
     * 通用文件上传接口
     *
     * @param file 前端 el-upload 组件上传的二进制文件（form-data 字段名必须叫 file）
     * @return 上传成功后的访问 URL，以及原始文件名、字节大小等元信息
     */
    @PostMapping("/file")
    public CommonResult<?> upload(@RequestParam("file") MultipartFile file) {
        // 1. 入参防御
        if (file == null || file.isEmpty()) {
            return CommonResult.fail("上传文件不能为空");
        }

        // 2. 大小限制（防止学生误传几个 G 的视频把服务器撑爆）
        if (file.getSize() > MAX_FILE_SIZE) {
            return CommonResult.fail("文件过大，单文件上限 20MB");
        }

        // 3. 拿到原始文件名 + 后缀
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            originalName = "unknown";
        }
        String fileExtension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalName.length() - 1) {
            fileExtension = originalName.substring(dotIndex);
        }

        // 4. 用日期分目录，避免单目录文件过多
        String dateFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // 5. 用 UUID 重命名，避免用户上传同名文件互相覆盖、避免路径注入
        String uuidFileName = UUID.randomUUID().toString().replace("-", "") + fileExtension;

        // 6. 拼出落盘的相对路径
        String relativePath = dateFolder + File.separator + uuidFileName;

        // 7. 拿到项目根目录（Spring Boot 启动目录），确保 ./uploads 写在项目根下
        String projectRoot = System.getProperty("user.dir");
        File targetDir = new File(projectRoot + File.separator + UPLOAD_BASE_DIR + File.separator + dateFolder);

        // 8. 目录不存在则递归创建
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return CommonResult.fail("服务器存储目录创建失败");
        }

        // 9. 落盘（最朴素的文件拷贝，错误时抛业务异常由全局异常处理器返回 500）
        File destFile = new File(targetDir, uuidFileName);
        try {
            file.transferTo(destFile);
        } catch (IOException ioException) {
            return CommonResult.fail("文件写入磁盘失败: " + ioException.getMessage());
        }

        // 10. 拼出对外可访问的 URL（注意：与 WebMvcConfig.addResourceHandlers 里的 /uploads/** 映射一致）
        String fileUrl = "/uploads/" + relativePath.replace(File.separator, "/");

        // 11. 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", uuidFileName);
        result.put("originalName", originalName);
        result.put("fileUrl", fileUrl);
        result.put("size", file.getSize());

        return CommonResult.success(result, "上传成功");
    }
}
