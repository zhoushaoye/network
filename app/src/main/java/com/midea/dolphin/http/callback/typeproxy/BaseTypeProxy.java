package com.midea.dolphin.http.callback.typeproxy;


import com.midea.dolphin.http.model.IApiResult;

import java.lang.reflect.Type;
/**
 * 泛型解析代理基类
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public abstract class BaseTypeProxy<T extends IApiResult<R>, R> implements IType<T> {

    /**
     * <T>与基础ApiResult 组合成 ApiResult<T>
     * @return ApiResult<T>
     */
    public abstract Type getParameterizedType();

    /**
     * 获取实际业务数据对象 <T>
     */
    public abstract Type getType();

    /**
     * 顶层数据对象 ApiResult
     */
    public abstract Type getRawType();

}
