package com.coursemanager.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author hhl
 * @date 2024/06/07 15:05
 * @description 父实体类
 */
public abstract class BaseBean<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 6539899982476906196L;

    @TableField("create_time")
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    protected Date createTime;

    @TableField("update_time")
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    protected Date updateTime;

    public Date getCreateTime() {
        return createTime;
    }

    public T setCreateTime() {
        return setCreateTime(new Date());
    }

    @SuppressWarnings("unchecked")
    public T setCreateTime(Date createTime) {
        this.createTime = createTime;
        return (T) this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public T setUpdateTime() {
        return setUpdateTime(new Date());
    }

    @SuppressWarnings("unchecked")
    public T setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return (T) this;
    }

    @Override
    public String toString() {
        return "BaseBean{" +
                "createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
