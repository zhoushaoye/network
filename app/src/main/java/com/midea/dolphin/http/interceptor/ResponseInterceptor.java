package com.midea.dolphin.http.interceptor;

import com.midea.dolphin.http.callback.IHttpResponseCallBack;
import com.midea.dolphin.http.utils.HttpUtil;


import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 接口返回拦截器
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class ResponseInterceptor implements Interceptor {

    private IHttpResponseCallBack mHttpResponseCallBack;

    public ResponseInterceptor(IHttpResponseCallBack httpResponseCallBack) {
        this.mHttpResponseCallBack = httpResponseCallBack;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (mHttpResponseCallBack != null) {
            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = HttpUtil.UTF_8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(HttpUtil.UTF_8);
            }
            String bodyString = buffer.clone().readString(charset);
            mHttpResponseCallBack.onResponse(request,response, bodyString);
        }
        return response;
    }

}
