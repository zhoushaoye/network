package com.midea.network.http.utils;


import com.google.gson.reflect.TypeToken;

import com.midea.network.http.cache.model.CacheResult;
import com.midea.network.http.callback.typeproxy.BaseTypeProxy;
import com.midea.network.http.config.GlobalConfig;
import com.midea.network.http.config.RequestConfig;
import com.midea.network.http.function.ApiResultFuction;
import com.midea.network.http.function.CacheResultFunction;
import com.midea.network.http.function.HttpResponseErrorFunction;
import com.midea.network.http.function.HttpRetryFunction;
import com.midea.network.http.function.ResponseBodyFunction;
import com.midea.network.http.json.JsonUtil;
import com.midea.network.http.model.IApiResult;
import com.midea.network.http.transformer.SchedulersTransformer;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;


/**
 * Rx流转工具
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HttpRxUtil {

    /**
     * Http 结合 RxJava 进行链式处理
     *
     * 最终处理出来的是一个Observable<IApiResult<T>>
     *
     * @return 可以直接subscribe的Observable<IApiResult<T>>
     */
    @SuppressWarnings("unchecked")
    public static <T> Observable<T> createObservable(Observable observable,
                                                     @NonNull BaseTypeProxy<? extends IApiResult<T>, T> proxy, RequestConfig requestConfig, boolean isSync) {
        return handleObservable(observable, requestConfig)
                //1. 线程切换调度
                .compose(new SchedulersTransformer(isSync))
                //2. ResponseBody --> ApiResult<T>
                .map(new ResponseBodyFunction(JsonUtil.getGson().getAdapter(TypeToken.get(proxy.getParameterizedType()))
                        , JsonUtil.getGson()))
                //3. 错误处理
                .onErrorResumeNext(new HttpResponseErrorFunction<T>())
                //4. 缓存,根据缓存策略最终输出CacheResult<T>
                .compose(requestConfig.getRxCache().transformer(requestConfig.getCacheMode(), proxy.getParameterizedType()))
                //5. 重试
                .retryWhen(new HttpRetryFunction(requestConfig.getRetryCount()))
                //6. CacheResult<T> --> T
                .compose(new ObservableTransformer<CacheResult<T>, T>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull Observable<CacheResult<T>> upstream) {
                        return upstream.map(new CacheResultFunction<T>());
                    }
                });
    }

    /**
     * Http 结合 RxJava 进行链式处理
     *
     * 最终处理出来的是一个Observable<T>
     *
     * @return 可以直接subscribe的Observable<T>
     */
    @SuppressWarnings("unchecked")
    public static <T> Observable<T> createTObservable(Observable observable,
                                                      @NonNull BaseTypeProxy<? extends IApiResult<T>, T> proxy, RequestConfig requestConfig, boolean isSync) {
        return handleObservable(observable, requestConfig)
                //1. 线程切换调度
                .compose(new SchedulersTransformer(isSync))
                //2. ResponseBody --> ApiResult<T>
                .map(new ResponseBodyFunction(JsonUtil.getGson().getAdapter(TypeToken.get(proxy.getParameterizedType()))
                        , JsonUtil.getGson()))
                //3. ApiResult<T> --> T
                .map(new ApiResultFuction<T>())
                //4. 错误处理
                .onErrorResumeNext(new HttpResponseErrorFunction<T>())
                //5. 缓存,根据缓存策略最终输出CacheResult<T>
                .compose(requestConfig.getRxCache().transformer(requestConfig.getCacheMode(), proxy.getType()))
                //6. 重试
                .retryWhen(new HttpRetryFunction(requestConfig.getRetryCount()))
                //7. CacheResult<T> --> T
                .compose(new ObservableTransformer<CacheResult<T>, T>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull Observable<CacheResult<T>> upstream) {
                        return upstream.map(new CacheResultFunction<T>());
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private static Observable handleObservable(Observable observable,final RequestConfig requestConfig) {
        observable = observable.doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                HttpLogUtil.i("Http doOnDispose url... "+requestConfig.getRequestUrl());
                if(requestConfig.getRxCache() != null ){
                    requestConfig.getRxCache().close();
                }
            }
        });
        if(requestConfig.getLifeCycle() != null){
            observable = observable.compose(requestConfig.getLifeCycle());
        }
        //如果设置了Rx超时则使用
        if(requestConfig.getTimeOut() > 0){
            observable = observable.timeout(requestConfig.getTimeOut(), GlobalConfig.TIME_UNIT);
        }
        return observable;
    }

}
