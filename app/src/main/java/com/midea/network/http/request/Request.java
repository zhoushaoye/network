package com.midea.network.http.request;

import android.text.TextUtils;


import com.midea.network.http.callback.IHttpCallBack;
import com.midea.network.http.callback.typeproxy.CallBackProxy;
import com.midea.network.http.callback.typeproxy.CallTypeProxy;
import com.midea.network.http.config.RequestConfig;
import com.midea.network.http.model.ApiResult;
import com.midea.network.http.model.IApiResult;
import com.midea.network.http.model.RequestMethodModel;
import com.midea.network.http.subsciber.CallBackSubsciber;
import com.midea.network.http.utils.HttpRxUtil;

import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * 具体实现类{@link IRequest}
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class Request extends RequestConfig<Request> implements IRequest {

    private RequestMethodModel mMethod;

    public Request(String url, RequestMethodModel methodModel) {
        super(url);
        mMethod = methodModel;
    }

    /**
     * 异步执行请求并返回Observable<ApiResult<T>>
     * @param type 泛型解析对象
     * @param <T> 泛型参数
     * @return Observable<ApiResult<T>>
     */
    @Override
    public <T> Observable<T> execute(@NonNull Type type) {
        return execute(new CallTypeProxy<ApiResult<T>, T>(type) {
        }, false);
    }

    /**
     * 异步执行请求并返回Observable<T>
     * @param tClass class
     * @param <T>  泛型参数
     * @return Observable<T>
     */
    public <T> Observable<T> executeClazz(@NonNull Class<T> tClass) {
        return executeClazz(new CallTypeProxy<ApiResult<T>, T>(tClass) {
        },false);
    }

    /**
     * 异步执行请求并返回Observable<ApiResult<T>>
     * @param proxy 泛型代理
     * @param syn 同步
     * @param <T> 泛型参数
     * @return Observable<ApiResult<T>>
     */
    protected  <T> Observable<T> execute(@NonNull CallTypeProxy<? extends IApiResult<T>, T> proxy, boolean syn) {
        return HttpRxUtil.createObservable(createRequest(), proxy, this, syn);
    }

    /**
     * 异步执行请求并返回Observable<T>
     * @param proxy 泛型代理
     * @param syn 同步
     * @param <T> 泛型参数
     * @return Observable<T>
     */
    protected  <T> Observable<T> executeClazz(@NonNull CallTypeProxy<? extends IApiResult<T>, T> proxy, boolean syn) {
        return HttpRxUtil.createTObservable(createRequest(), proxy, this, syn);
    }

    /**
     * 异步执行请求,返回可取消的Disposable
     * @param callBack 请求回调
     * @param <T> 泛型参数
     * @return Disposable
     */
    @Override
    public <T> Disposable execute(@NonNull IHttpCallBack<T> callBack) {
        return execute(new CallBackProxy<ApiResult<T>, T>(callBack) {
        }, false);
    }

    /**
     * 异步执行请求,返回可取消的Disposable
     * @param proxy 回调泛型代理
     * @param syn 同步
     * @param <T> 泛型参数
     * @return Disposable
     */
    protected <T> Disposable execute(@NonNull CallBackProxy<? extends IApiResult<T>, T> proxy, boolean syn) {
        return HttpRxUtil.createObservable(createRequest(), proxy, this, syn)
                .subscribeWith(new CallBackSubsciber<T>(getContext(),proxy.getHttpCallBack()));
    }

    /**
     * 异步执行请求,返回可取消的Disposable
     * 如果{@link RequestConfig {@link #httpCallBack(IHttpCallBack)}}未设置回调,则不回调
     * @param <T> 泛型参数
     * @return Disposable
     */
    @Override
    public <T> Disposable execute() {
        return execute(new CallBackProxy<ApiResult<T>, T>(mHttpCallBack) {
        }, false);
    }

    /**
     * 同步的方式执行请求,返回可取消的Disposable
     * 如果{@link RequestConfig {@link #httpCallBack(IHttpCallBack)}}未设置回调,则不回调
     * @param <T> 泛型参数
     * @return Disposable
     */
    @Override
    public <T> Disposable executeSyn() {
        return execute(new CallBackProxy<ApiResult<T>, T>(mHttpCallBack) {
        }, true);
    }

    /**
     * 同步的方式执行请求并返回Observable<ApiResult<T>>
     * @param type 泛型解析对象
     * @param <T> 泛型参数
     * @return Observable<ApiResult<T>>
     */
    @Override
    public <T> Observable<T> executeSyn(@NonNull Type type) {
        return execute(new CallTypeProxy<ApiResult<T>, T>(type) {
        }, true);
    }

    /**
     * 同步的方式执行请求并返回Observable<T>
     * @param tClass 泛型解析对象
     * @param <T> 泛型参数
     * @return Observable<T>
     */
    public <T> Observable<T> executeClazzSyn(@NonNull Class<T> tClass) {
        return executeClazz(new CallTypeProxy<ApiResult<T>, T>(tClass) {
        },true);
    }

    /**
     * 同步的方式执行请求,返回可取消的Disposable
     * @param callBack 请求回调
     * @param <T> 泛型参数
     * @return Disposable
     */
    @Override
    public <T> Disposable executeSyn(@NonNull IHttpCallBack<T> callBack) {
        return execute(new CallBackProxy<ApiResult<T>, T>(callBack) {
        }, true);
    }

    /**
     * 根据传入的方法枚举,执行具体的请求
     */
    private Observable<ResponseBody> createRequest() {
        build();
        Observable<ResponseBody> observable = null;
        switch (mMethod) {
            case GET:
                //执行Get请求
                observable = getApiService().get(getHttpHeaders().getHeadersMap(),getRequestUrl(), getHttpRequestParams().getRequestParams());
                break;
            case POST:
                //执行Post请求
                if(TextUtils.isEmpty(getJson())){
                    observable = getApiService().post(getHttpHeaders().getHeadersMap(),getRequestUrl(), getHttpRequestParams().getRequestParams());
                }else {
                    RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), getJson());
                    observable = getApiService().postJson(getHttpHeaders().getHeadersMap(),getRequestUrl(),body);
                }
                break;
            case PUT:
                //执行Put请求
                observable = getApiService().put(getHttpHeaders().getHeadersMap(),getRequestUrl(), getHttpRequestParams().getRequestParams());
                break;
            case DELET:
                //执行Delete请求
                observable = getApiService().delete(getHttpHeaders().getHeadersMap(),getRequestUrl(), getHttpRequestParams().getRequestParams());
                break;
            default:
                break;
        }
        return observable;
    }

}
