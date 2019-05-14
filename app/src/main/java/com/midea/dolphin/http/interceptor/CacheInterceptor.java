package com.midea.dolphin.http.interceptor;

import android.content.Context;
import android.text.TextUtils;


import com.midea.dolphin.http.utils.HttpLogUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 设置Okhttp缓存功能
 *
 */
public class CacheInterceptor implements Interceptor {

    protected Context mContext;
    protected String mCacheControlValueOffline;
    protected String mCacheControlValueOnline;
    //set cahe times is 3 days
    protected static final int MAX_STALE = 60 * 60 * 24 * 3;
    // read from cache for 60 s
    protected static final int MAX_STALE_ONLINE = 60;

    public CacheInterceptor(Context context) {
        this(context, String.format("max-age=%d", MAX_STALE_ONLINE));
    }

    public CacheInterceptor(Context context, String cacheControlValue) {
        this(context, cacheControlValue, String.format("max-age=%d", MAX_STALE));
    }

    public CacheInterceptor(Context context, String cacheControlValueOffline, String cacheControlValueOnline) {
        this.mContext = context;
        this.mCacheControlValueOffline = cacheControlValueOffline;
        this.mCacheControlValueOnline = cacheControlValueOnline;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //Request request = chain.request();
        Response originalResponse = chain.proceed(chain.request());
        String cacheControl = originalResponse.header("Cache-Control");
        //String cacheControl = request.cacheControl().toString();
        HttpLogUtil.e( MAX_STALE_ONLINE + "s load cache:" + cacheControl);
        if (TextUtils.isEmpty(cacheControl) || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                cacheControl.contains("must-revalidate") || cacheControl.contains("max-age") || cacheControl.contains("max-stale")) {
            return originalResponse.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, max-age=" + MAX_STALE)
                    .build();

        } else {
            return originalResponse;
        }
    }
}
