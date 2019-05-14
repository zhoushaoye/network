package com.midea.dolphin.http.callback.typeproxy;

import com.google.gson.internal.$Gson$Types;
import com.midea.dolphin.http.model.IApiResult;
import com.midea.dolphin.http.utils.HttpUtil;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * 用户自定义传入的Class Type
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class CallTypeProxy<T extends IApiResult<R>, R> extends BaseTypeProxy<T,R> {
    private Type mType;

    protected CallTypeProxy(Type type) {
        this.mType = type;
    }

    @Override
    public Type getParameterizedType() {
        Type typeArguments = null;
        if (mType != null && !HttpUtil.hasUnresolvableType(mType)) {
            typeArguments = mType;
        }
        if (typeArguments == null) {
            typeArguments = ResponseBody.class;
        }
        //获取顶层泛型类型 ApiResult
        Type rawType = getRawType();
        //把传入的实际泛型<T>与基础ApiResult 组合成 ApiResult<T>
        return $Gson$Types.newParameterizedTypeWithOwner(null, rawType, typeArguments);
    }

    @Override
    public Type getType() {
        return mType;
    }

    @Override
    public Type getRawType() {
        Type rawType = HttpUtil.findNeedType(getClass());
        if (rawType instanceof ParameterizedType) {
            rawType = ((ParameterizedType) rawType).getRawType();
        }
        return rawType;
    }
}
