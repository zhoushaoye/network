package com.midea.dolphin.http.interceptor;

import android.content.Context;


import com.midea.dolphin.http.utils.HttpLogUtil;
import com.midea.dolphin.http.utils.HttpUtil;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * <p>描述：支持离线缓存,使用OKhttp自带的缓存功能</p>
 *
 * 配置Okhttp的Cache<br>
 * 配置请求头中的cache-control或者统一处理所有请求的请求头<br>
 * 云端配合设置响应头或者自己写拦截器修改响应头中cache-control<br>
 * 列：<br>
 *     <p>
 * 在Retrofit中，我们可以通过@Headers来配置，如：
 *
 * @Headers("Cache-Control: public, max-age=3600)
 * @GET("merchants/{shopId}/icon")
 * Observable<ShopIconEntity> getShopIcon(@Path("shopId") long shopId);
 */
public class CacheInterceptorOffline extends CacheInterceptor {
    public CacheInterceptorOffline(Context context) {
        super(context);
    }

    public CacheInterceptorOffline(Context context, String cacheControlValue) {
        super(context, cacheControlValue);
    }

    public CacheInterceptorOffline(Context context, String cacheControlValue, String cacheOnlineControlValue) {
        super(context, cacheControlValue, cacheOnlineControlValue);
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        if (!HttpUtil.isNetworkAvailable(mContext)) {
            HttpLogUtil.i(" no network load cache:"+ request.cacheControl().toString());
           /* request = request.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "only-if-cached, " + cacheControlValue_Offline)
                    .build();*/

            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    //.cacheControl(CacheControl.FORCE_NETWORK)
                    .build();
            Response response = chain.proceed(request);
            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, only-if-cached, " + mCacheControlValueOffline)
                    .build();
        }
        return chain.proceed(request);
    }
}
