package com.midea.dolphin.http.callback.typeproxy;

import com.google.gson.internal.$Gson$Types;
import com.midea.dolphin.http.callback.IHttpCallBack;
import com.midea.dolphin.http.model.IApiResult;
import com.midea.dolphin.http.utils.HttpUtil;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * 用户不传入指定的class,由框架尝试动态解析
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public class CallBackProxy<T extends IApiResult<R>, R> extends BaseTypeProxy<T,R> {

    private IHttpCallBack<R> mCallBack;

    private Type mType;

    protected CallBackProxy(IHttpCallBack<R> callBack) {
        mCallBack =  callBack;
        if(mCallBack != null){
            mType = mCallBack.getType();
        }
    }

    public IHttpCallBack<R> getHttpCallBack() {
        return mCallBack;
    }

    @Override
    public Type getParameterizedType() {
        //解析实际的类型参数
        Type typeArguments = null;
        if (mCallBack != null && !HttpUtil.hasUnresolvableType(mType)) {
            Type rawType = mCallBack.getRawType();
            if (List.class.isAssignableFrom(HttpUtil.getClass(rawType, 0)) ||
                    Map.class.isAssignableFrom(HttpUtil.getClass(rawType, 0))) {
                typeArguments = mType;
            } else {
                typeArguments = HttpUtil.getClass(mType, 0);
            }
        }

        if (typeArguments == null) {
            typeArguments = ResponseBody.class;
        }
        //获取顶层泛型类型 ApiResult
        Type rawType = getRawType();
        //<T>与基础ApiResult 组合成 ApiResult<T> 
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
