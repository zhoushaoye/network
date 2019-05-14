package com.midea.network.http.request;

import com.midea.network.http.body.UploadRequestBody;
import com.midea.network.http.callback.IHttpCallBack;
import com.midea.network.http.callback.IProgressCallBack;
import com.midea.network.http.callback.typeproxy.CallBackProxy;
import com.midea.network.http.callback.typeproxy.CallTypeProxy;
import com.midea.network.http.config.RequestConfig;
import com.midea.network.http.model.ApiResult;
import com.midea.network.http.model.HttpRequestParams;
import com.midea.network.http.model.IApiResult;
import com.midea.network.http.subsciber.CallBackSubsciber;
import com.midea.network.http.utils.HttpRequestBodyUtil;
import com.midea.network.http.utils.HttpRxUtil;
import com.midea.network.http.utils.HttpUtil;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * body请求
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public abstract class BaseBodyRequest<R extends BaseBodyRequest> extends RequestConfig<R> {

    //上传的文本内容
    String mStringContent;

    //上传的文本内容
    MediaType mMediaTypeContent;

    String mJsonStr;

    //上传的字节数据
    byte[] mBytesContent;

    public enum UploadType {
        /**
         * MultipartBody.Part方式上传
         */
        PART,
        /**
         * Map RequestBody方式上传
         */
        BODY
    }

    UploadType mCurrentUploadType = UploadType.BODY;

    public BaseBodyRequest(String url) {
        super(url);
    }

    public R upString(String string) {
        this.mStringContent = string;
        this.mMediaTypeContent = MediaType.parse("text/plain");
        return (R) this;
    }

    public R upString(String string, String mediaType) {
        this.mStringContent = string;
        HttpUtil.checkNotNull(mediaType, "mediaType==null");
        this.mMediaTypeContent = MediaType.parse(mediaType);
        return (R) this;
    }

    public R upBytes(byte[] bs) {
        this.mBytesContent = bs;
        return (R) this;
    }

    public R param(String key, File file) {
        getHttpRequestParams().putFileParam(key, file);
        return (R) this;
    }

    public R param(String key, InputStream stream, String fileName) {
        getHttpRequestParams().putFileParam(key, stream, fileName);
        return (R) this;
    }

    public R param(String key, byte[] bytes, String fileName) {
        getHttpRequestParams().putFileParam(key, bytes, fileName);
        return (R) this;
    }

    public R addFileParams(String key, List<File> files) {
        getHttpRequestParams().putFileParams(key, files);
        return (R) this;
    }

    public R addFileWrapperParams(String key, List<HttpRequestParams.FileWrapper> fileWrappers) {
        getHttpRequestParams().putFileWrapperParams(key, fileWrappers);
        return (R) this;
    }

    public R param(String key, File file, String fileName) {
        getHttpRequestParams().putFileParam(key, file, fileName);
        return (R) this;
    }

    public <T> R param(String key, T file, String fileName, MediaType contentType) {
        getHttpRequestParams().putFileParam(key, file, fileName, contentType);
        return (R) this;
    }

    /**
     * 上传文件的方式，默认BODY方式上传
     */
    public R uploadType(UploadType uploadtype) {
        mCurrentUploadType = uploadtype;
        return (R) this;
    }

    List<MultipartBody.Part> uploadFilesWithParts() {
        List<MultipartBody.Part> parts = new ArrayList<>();
        //拼接参数键值对
        for (Map.Entry<String, String> mapEntry : getHttpRequestParams().getRequestParams().entrySet()) {
            parts.add(MultipartBody.Part.createFormData(mapEntry.getKey(), mapEntry.getValue()));
        }
        //拼接文件
        for (Map.Entry<String, List<HttpRequestParams.FileWrapper>> entry : getHttpRequestParams().getFileParams()
                .entrySet()) {
            List<HttpRequestParams.FileWrapper> fileValues = entry.getValue();
            for (HttpRequestParams.FileWrapper fileWrapper : fileValues) {
                MultipartBody.Part part = addFile(entry.getKey(), fileWrapper);
                parts.add(part);
            }
        }
        return parts;
    }

    Map<String, RequestBody> uploadFilesWithBodys() {
        Map<String, RequestBody> mBodyMap = new HashMap<>();
        //拼接参数键值对
        for (Map.Entry<String, String> mapEntry : getHttpRequestParams().getRequestParams().entrySet()) {
            RequestBody body = RequestBody.create(MediaType.parse("text/plain"), mapEntry.getValue());
            mBodyMap.put(mapEntry.getKey(), body);
        }
        //拼接文件
        for (Map.Entry<String, List<HttpRequestParams.FileWrapper>> entry : getHttpRequestParams().getFileParams()
                .entrySet()) {
            List<HttpRequestParams.FileWrapper> fileValues = entry.getValue();
            for (HttpRequestParams.FileWrapper fileWrapper : fileValues) {
                RequestBody requestBody = getRequestBody(fileWrapper);
                mBodyMap.put(entry.getKey(), requestBody);
            }
        }
        return mBodyMap;
    }

    RequestBody uploadFilesWithBody() {
        RequestBody requestBody = null;
        for (Map.Entry<String, List<HttpRequestParams.FileWrapper>> entry : getHttpRequestParams().getFileParams()
                .entrySet()) {
            List<HttpRequestParams.FileWrapper> fileValues = entry.getValue();
            for (HttpRequestParams.FileWrapper fileWrapper : fileValues) {
                requestBody = getRequestBody(fileWrapper);
            }
        }
        return requestBody;
    }

    //文件方式
    private MultipartBody.Part addFile(String key, HttpRequestParams.FileWrapper fileWrapper) {
        RequestBody requestBody = getRequestBody(fileWrapper);
        HttpUtil.checkNotNull(requestBody, "requestBody == null,file must be File/InputStream/byte[]");
        MultipartBody.Part part = MultipartBody.Part.createFormData(key, fileWrapper.getFileName(), requestBody);
        return part;
    }

    private RequestBody getRequestBody(HttpRequestParams.FileWrapper fileWrapper) {
        RequestBody requestBody = null;
        if (fileWrapper.getFile() instanceof File) {
            requestBody = RequestBody.create(fileWrapper.getContentType(), (File) fileWrapper.getFile());
        } else if (fileWrapper.getFile() instanceof InputStream) {
            requestBody = HttpRequestBodyUtil.create(fileWrapper.getContentType(), (InputStream) fileWrapper.getFile());
        } else if (fileWrapper.getFile() instanceof byte[]) {
            requestBody = RequestBody.create(fileWrapper.getContentType(), (byte[]) fileWrapper.getFile());
        }
        return requestBody;
    }

    public <T> Observable<T> execute(@NonNull Type type) {
        return execute(new CallTypeProxy<ApiResult<T>, T>(type) {
        }, false);
    }

    protected <T> Observable<T> execute(CallTypeProxy<? extends IApiResult<T>, T> proxy, boolean syn) {
        return HttpRxUtil.createObservable(createRequest(), proxy, this, syn);
    }


    public <T> Disposable execute(@NonNull IHttpCallBack<T> callBack) {
        return execute(new CallBackProxy<ApiResult<T>, T>(callBack) {
        }, false);
    }

    public <T> Disposable execute() {
        return execute(new CallBackProxy<ApiResult<T>, T>(mHttpCallBack) {
        }, false);
    }

    protected <T> Disposable execute(CallBackProxy<? extends IApiResult<T>, T> proxy, boolean syn) {
        return HttpRxUtil.createObservable(createRequest(), proxy, this, syn)
                .subscribeWith(new CallBackSubsciber<T>(getContext(),proxy.getHttpCallBack()));
    }

    public <T> Observable<T> execute(IProgressCallBack progressCallBack, @NonNull Type type) {
        setUploadProgressListener(progressCallBack);
        return execute(type);
    }

    public <T> Disposable execute(IProgressCallBack progressCallBack, @NonNull IHttpCallBack<T> callBack) {
        setUploadProgressListener(progressCallBack);
        return execute(callBack);
    }

    public Disposable execute(IProgressCallBack progressCallBack) {
        setUploadProgressListener(progressCallBack);
        return execute();
    }

    /**
     * 上传进度监听
     */
    private void setUploadProgressListener(final IProgressCallBack progressCallBack) {
        addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (null == request.body()) {
                    return chain.proceed(request);
                }
                Request build = request.newBuilder()
                        .method(request.method(),
                                new UploadRequestBody(request.body(),request.url().toString(),progressCallBack)
                        )
                        .build();
                return chain.proceed(build);
            }
        });
    }

    abstract Observable<ResponseBody> createRequest();
}
