package com.coursemanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * @author 架构师
 * @date 2024/06/28
 * @description Spring MVC 全局配置：把项目根目录下的 ./uploads 映射成对外可访问的 /uploads/** 静态路径
 *
 * 与 UploadController 的输出路径严丝合缝：
 *   写盘路径：./uploads/{yyyyMMdd}/{uuid}.{ext}
 *   访问 URL：/uploads/{yyyyMMdd}/{uuid}.{ext}
 *
 * 不加这个映射，老师/学生上传的附件会得到 404。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 把 URL /uploads/** 映射到磁盘上的 ./uploads 目录
     * 注意：路径必须以 file: 前缀开头，告诉 Spring 这是本地文件系统而非 classpath
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目启动目录（uploadController 也是用 System.getProperty("user.dir") 拼的，确保一致）
        String projectRoot = System.getProperty("user.dir");
        String absoluteUploadPath = "file:" + projectRoot + File.separator + "uploads" + File.separator;

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absoluteUploadPath);
    }
}
