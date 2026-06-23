package com.coursemanager.lang;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author hhl
 * @date 2024/06/07 17:13
 * @description 查询参数类
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = -5481912101653554127L;

    private int pageNum;

    private int pageSize;
}
