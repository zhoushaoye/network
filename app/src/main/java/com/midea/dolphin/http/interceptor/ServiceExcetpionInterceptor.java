package com.midea.dolphin.http.interceptor;
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
 * 接口异常返回全局拦截器,继承该拦截器去处理自己的业务错误
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public abstract class ServiceExcetpionInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
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
        boolean isText = isText(contentType);
        if (!isText) {
            return response;
        }
        return onInterceptor(chain,request,response,bodyString);
    }

    public abstract Response onInterceptor(Chain chain,Request request,Response response, String json);


    //过滤调对非请求的影响
    private boolean isText(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype().equals("json")) {
                return true;
            }
        }
        return false;
    }

}
