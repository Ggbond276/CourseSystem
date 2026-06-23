package com.coursemanager.dto;

import com.coursemanager.pojo.BaseBean;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author hhl
 * @date 2024/06/07 15:22
 * @description dto->pojo操作接口
 */
@SuppressWarnings("null")
public interface DtoTrans<T extends BaseBean<?>> extends Serializable {

    /**
     * 传输类转model
     * @param clazz modelClass
     * @param consumer 自定义操作
     * @return model
     */
    default T toModel(Class<T> clazz, Consumer<T> consumer) {
        T model = null;
        try {
            model = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(this, model);
        T finalModel = model;
        Optional.ofNullable(consumer).ifPresent((c) -> {
            c.accept(finalModel);
        });
        return finalModel;
    }

    /**
     * 默认dto转model时设置创建时间
     * @param clazz modelClass
     * @return model
     */
    default T toModelDefaultCreateTime(Class<T> clazz) {
        return toModel(clazz, BaseBean::setCreateTime);
    }

    /**
     * 默认dto转model时设置更新时间
     * @param clazz modelClass
     * @return model
     */
    default T toModelDefaultUpdateTime(Class<T> clazz) {
        return toModel(clazz, BaseBean::setUpdateTime);
    }
}
