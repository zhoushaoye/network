package com.midea.dolphin.http.callback;


import com.midea.dolphin.http.callback.typeproxy.IType;
import com.midea.dolphin.http.exception.HttpException;
import com.midea.dolphin.http.model.IApiResult;

/**
 * http请求的过程回调
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public interface IHttpCallBack<T> extends IType<T> {

    /**
     * 请求开始
     */
    void onStart();

    /**
     * 请求成功
     *
     * @param t 服务器返回的结果
     */
    void onSuccess(IApiResult<T> t);

    /**
     * 请求出现异常
     */
    void onError(HttpException e);

    /**
     * 请求完成
     * <p>
     *      不管成功或失败都会回调{@code #onComplete()}
     * </p>
     */
    void onComplete();

}
