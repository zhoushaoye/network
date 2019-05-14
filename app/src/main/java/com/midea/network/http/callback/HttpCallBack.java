package com.midea.network.http.callback;



import com.midea.network.http.exception.HttpException;
import com.midea.network.http.exception.ServiceException;
import com.midea.network.http.model.IApiResult;
import com.midea.network.http.utils.HttpUtil;


import java.lang.reflect.Type;


/**
 * 添加逻辑处理,简化调用方的编码,只需要重写{@link #onSuccess(T)},{@link #onError(HttpException)}}
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public abstract class HttpCallBack<T> implements IHttpCallBack<T> {

    public HttpCallBack() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onComplete() {

    }

    /**
     * 业务返回失败或其他异常时调用
     * @param e 异常
     */
    @Override
    public abstract void onError(HttpException e);

    /**
     * 仅在业务返回成功时调用 {@link IApiResult} isResultSuccess() == true时
     * @param t 业务对象
     */
    public abstract void onSuccess(T t);

    /**
     * 接口调用成功时回调
     * @param apiResult 结果
     */
    @Override
    public void onSuccess(IApiResult<T> apiResult) {
        if (apiResult.isResultSuccess()) {
            onSuccess(apiResult.getResultData());
        } else {
            onError(HttpException.handleException(
                    new ServiceException(apiResult.getReultCode(), apiResult.getResultMessage())));
        }
    }

    @Override
    public Type getType() {
        return HttpUtil.findNeedClass(getClass());
    }

    @Override
    public Type getRawType() {
        return HttpUtil.findRawType(getClass());
    }
}
