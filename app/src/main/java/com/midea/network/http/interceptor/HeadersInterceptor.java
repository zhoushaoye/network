package com.midea.network.http.interceptor;


import com.midea.network.http.model.HttpHeaders;
import com.midea.network.http.utils.HttpLogUtil;


import java.io.IOException;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 公共请求头,根据当前的baseUrl添加
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class HeadersInterceptor implements Interceptor {

    private HttpHeaders headers;

    private String mBaseUrl;

    public HeadersInterceptor(HttpHeaders headers, String baseUrl) {
        this.headers = headers;
        this.mBaseUrl = baseUrl;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        if (headers.getHeadersMap().isEmpty()) {
            return chain.proceed(builder.build());
        }
        try {
            HttpUrl url = request.url();
            if (url.toString().startsWith(mBaseUrl)) {
                for (Map.Entry<String, String> entry : headers.getHeadersMap().entrySet()) {
                    builder.header(entry.getKey(), entry.getValue()).build();
                }
            }
        } catch (Exception e) {
            HttpLogUtil.e(e);
        }
        return chain.proceed(builder.build());

    }
}
