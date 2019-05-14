package com.midea.dolphin.http.request;


import com.midea.dolphin.http.callback.IHttpCallBack;

import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * 请求方法封装
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public interface IRequest {

    /**
     * 异步执行Http请求 返回一个Observable
     *
     */
    <T> Disposable execute();
    /**
     * 异步执行Http请求 返回一个Observable
     *
     * @param type 泛型解析对象
     * @param <T>  解析后的对象
     */
    <T> Observable<T> execute(@NonNull Type type);

    /**
     * 异步执行Http请求 返回该请求的可取消对象Disposable
     *
     * @param callBack 请求回调
     * @param <T>      泛型解析对象
     */
    <T> Disposable execute(@NonNull IHttpCallBack<T> callBack);

    /**
     * 同步执行Http请求 返回一个Observable
     *
     * @param type 泛型解析对象
     * @param <T>  解析后的对象
     */
    <T> Observable<T> executeSyn(@NonNull Type type);

    /**
     * 同步执行Http请求 返回该请求的可取消对象Disposable
     */
    <T> Disposable executeSyn();
    /**
     * 同步执行Http请求 返回该请求的可取消对象Disposable
     *
     * @param callBack 请求回调
     * @param <T>      泛型解析对象
     */
    <T> Disposable executeSyn(@NonNull IHttpCallBack<T> callBack);

}
