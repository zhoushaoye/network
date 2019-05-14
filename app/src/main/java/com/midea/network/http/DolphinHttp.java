package com.midea.network.http;

import com.midea.network.http.config.GlobalConfig;
import com.midea.network.http.interceptor.HttpLoggingInterceptor;
import com.midea.network.http.request.DownloadRequest;
import com.midea.network.http.request.PostUploadRequest;
import com.midea.network.http.request.PutUploadRequest;
import com.midea.network.http.request.Request;
import com.midea.network.http.utils.HttpLogUtil;
import com.midea.network.http.utils.HttpSchedulerUtil;
import com.midea.network.http.utils.HttpUtil;


import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Http请求实例
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
public final class DolphinHttp {

    private volatile static DolphinHttp instance = null;

    private static GlobalConfig mGlobalConfig;

    public static final String TAG = "DolphinHttp";

    private DolphinHttp() {}

    public static DolphinHttp getInstance() {
        if (instance == null) {
            synchronized (DolphinHttp.class) {
                if (instance == null) {
                    instance = new DolphinHttp();
                }
            }
        }
        return instance;
    }

    public DolphinHttp init(GlobalConfig globalConfig){
        DolphinHttp.mGlobalConfig = HttpUtil.checkNotNull(globalConfig,"globalConfig = null");
        return this;
    }

    /**
     * 调试模式
     */
    public DolphinHttp debug(boolean debug) {
        return debug(debug, HttpLoggingInterceptor.Level.BODY);
    }
    /**
     * 调试模式
     */
    public DolphinHttp debug(boolean debug, HttpLoggingInterceptor.Level level) {
        if (debug) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(level);
            getGlobalConfig().getOkHttpClientBuilder().addInterceptor(loggingInterceptor);
            HttpLogUtil.isDebug = debug;
        }
        HttpLogUtil.d(TAG +" Debug: "+debug);
        return this;
    }

    public static GlobalConfig getGlobalConfig() {
        return HttpUtil.checkNotNull(mGlobalConfig,"you must be setGlobalConfig ...");
    }

    /**
     * download
     * @param url api
     */
    public static DownloadRequest download(String url) {
        return new DownloadRequest(url);
    }

    /**
     * put的方式上传RequestBody
     * @param url api
     */
    public static PutUploadRequest putUpload(String url) {
        return new PutUploadRequest(url);
    }

    /**
     * post的方式上传
     * @param url api
     */
    public static PostUploadRequest postUpload(String url) {
        return new PostUploadRequest(url);
    }

    /**
     * post请求
     * @param url api
     */
    public static Request post(String url) {
        return getGlobalConfig().getRequestMethod().doPost(url);
    }

    /**
     * get请求
     * @param url api
     */
    public static Request get(String url) {
        return getGlobalConfig().getRequestMethod().doGet(url);
    }

    /**
     * put请求
     * @param url api
     */
    public static Request put(String url) {
        return getGlobalConfig().getRequestMethod().doPut(url);
    }

    /**
     * delete请求
     * @param url api
     */
    public static Request delete(String url) {
        return getGlobalConfig().getRequestMethod().doDelete(url);
    }

    /**
     * Dispose the resource
     */
    public void disposable(Disposable disposable) {
        if(disposable !=null && !disposable.isDisposed()){
            disposable.dispose();
        }
    }

    /**
     * 清除所有的接口缓存
     */
    public Disposable clearAllRxCache() {
        return getGlobalConfig().getRxCacheBuilder().build().clear()
                .compose(HttpSchedulerUtil.<Boolean>_io_main())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) {
                        HttpLogUtil.i("clearCache success!!!");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) {
                        HttpLogUtil.i("clearCache err!!!");
                    }
                });
    }

    /**
     * 清除指定的接口缓存（key）
     */
    public Disposable removeRxCache(String key) {
        return getGlobalConfig().getRxCacheBuilder().build().remove(key)
                .compose(HttpSchedulerUtil.<Boolean>_io_main())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) {
                        HttpLogUtil.i("removeCache success!!!");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) {
                        HttpLogUtil.i("removeCache err!!!");
                    }
                });

    }

}
