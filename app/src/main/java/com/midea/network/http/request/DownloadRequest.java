package com.midea.network.http.request;


import com.midea.network.http.body.DownloadResponseBody;
import com.midea.network.http.callback.IProgressCallBack;
import com.midea.network.http.config.RequestConfig;
import com.midea.network.http.subsciber.CallBackSubsciber;
import com.midea.network.http.transformer.HandleErrTransformer;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 文件下载
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class DownloadRequest extends RequestConfig<DownloadRequest> {

    public DownloadRequest(String url) {
        super(url);
    }

    private String mSavePath;

    private String mFileName;

    /**
     * 下载文件路径<br>
     * 默认在当前目录
     */
    public DownloadRequest savePath(String savePath) {
        this.mSavePath = savePath;
        return this;
    }

    /**
     * 下载文件名称<br>
     * 默认名字是时间戳生成的<br>
     */
    public DownloadRequest fileName(String fileName) {
        this.mFileName = fileName;
        return this;
    }

    /**
     *
     *  执行下载
     * @param callBack 下载回调
     *
     */
    @SuppressWarnings("unchecked")
    public Disposable execute(final IProgressCallBack callBack) {
        //添加下载拦截器
        addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                return response.newBuilder().body(
                        new DownloadResponseBody(getContext(), mSavePath,
                                mFileName,response.body(),callBack))
                        .build();
            }
        });
        return (Disposable) createRequest().compose(new ObservableTransformer<ResponseBody, ResponseBody>() {
            @Override
            public ObservableSource<ResponseBody> apply(@NonNull Observable<ResponseBody> upstream) {
                return upstream.subscribeOn(Schedulers.computation());
            }
        }).compose(new HandleErrTransformer()).subscribeWith(new CallBackSubsciber(getContext()));
    }

    private Observable<ResponseBody> createRequest() {
        return build().getApiService().downloadFile(getHttpHeaders().getHeadersMap(),getRequestUrl());
    }
}
