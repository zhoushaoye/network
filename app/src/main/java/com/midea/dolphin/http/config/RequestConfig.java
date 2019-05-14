package com.midea.dolphin.http.config;

import android.content.Context;


import com.midea.dolphin.http.DolphinHttp;
import com.midea.dolphin.http.apiservice.ApiService;
import com.midea.dolphin.http.cache.RxCache;
import com.midea.dolphin.http.cache.model.CacheMode;
import com.midea.dolphin.http.callback.IHttpCallBack;
import com.midea.dolphin.http.callback.IHttpResponseCallBack;
import com.midea.dolphin.http.cookie.CookieManger;
import com.midea.dolphin.http.interceptor.CacheInterceptor;
import com.midea.dolphin.http.interceptor.CacheInterceptorOffline;
import com.midea.dolphin.http.interceptor.ResponseInterceptor;
import com.midea.dolphin.http.json.JsonUtil;
import com.midea.dolphin.http.model.HttpHeaders;
import com.midea.dolphin.http.model.HttpRequestParams;
import com.midea.dolphin.http.utils.HttpLogUtil;
import com.midea.dolphin.http.utils.HttpUtil;
import com.midea.dolphin.http.utils.HttpsCertificateUtil;
import com.trello.rxlifecycle3.LifecycleTransformer;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * 请求参数配置,会默认使用全局配置项,如果修改则使用修改后的值
 * 区别于全局配置{@link GlobalConfig},{@link RequestConfig}的参数只针对本次请求生效
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/14
 */
@SuppressWarnings("unchecked")
public class RequestConfig<R extends RequestConfig> {

    private Cache mCache;

    //默认无缓存
    private CacheMode mCacheMode;

    //缓存时间
    private long mCacheTime;

    //缓存Key,默认使用当前请求Url作为缓存key
    private String mCacheKey;

    //请求url
    private String mRequestUrl;

    private int mTimeOut;

    private int mRetryCount;

    //上传Json的方式请求,注意只有post生效
    private String mJson;

    //最小缓存50M
    private static final long CACHE_MAX_SIZE = 5 * 1024 * 1024;

    //用户手动添加的Cookie
    private List<Cookie> mCookies = new ArrayList<>();

    private final List<Interceptor> mNetworkInterceptors = new ArrayList<>();

    //header
    private HttpHeaders mHttpHeaders = new HttpHeaders();

    //requestParam
    private HttpRequestParams mHttpRequestParams = new HttpRequestParams();

    //rxCache
    private RxCache mRxCache;

    private ApiService mApiService;

    private Context mContext;

    private HttpsCertificateUtil.SSLParams mSSLParams;

    private HostnameVerifier mHostnameVerifier;

    private final List<Interceptor> mInterceptors = new ArrayList<>();

    //用来回调解析后的请求结果
    public IHttpCallBack mHttpCallBack;

    private LifecycleTransformer mTransformer;

    public RequestConfig(String requestUrl) {
        this.mRequestUrl = requestUrl;
        GlobalConfig globalConfig = DolphinHttp.getGlobalConfig();
        mContext = globalConfig.getContext();
        String requestBaseUrl = HttpUtil.checkAndSetBaseUrl(globalConfig.getBaseUrl(), mRequestUrl);
        mCacheKey = HttpUtil.getCacheKey(requestBaseUrl, mRequestUrl);
        mCacheMode = globalConfig.getCacheMode();
        mCacheTime = globalConfig.getCacheTime();
        mRetryCount = globalConfig.getRetryCount();
        mCache = globalConfig.getHttpCache();
        //添加公共请求参数
        if (globalConfig.getCommonParams() != null) {
            mHttpRequestParams.put(globalConfig.getCommonParams());
        }
        //添加公共Header
        if (globalConfig.getCommonHeaders() != null) {
            mHttpHeaders.put(globalConfig.getCommonHeaders());
        }
    }

    public HttpRequestParams getHttpRequestParams() {
        return this.mHttpRequestParams;
    }

    /**
     * 上传Json的方式请求,注意只有post生效
     */
    public R json(String json) {
        this.mJson = json;
        return (R) this;
    }

    /**
     * 设置超时,同时会给Rxjava设置一个相同的超时时间.<br>
     * 默认Rx不设置超时,只有当配置timeOut才去设置
     *
     * @param timeOut 超时时间,单位(秒)
     */
    public R timeOut(int timeOut) {
        if (timeOut <= 0) {
            throw new IllegalArgumentException("timeOut must > 0");
        }
        this.mTimeOut = timeOut;
        return (R) this;
    }

    public R okhttpCache(Cache cache) {
        this.mCache = cache;
        return (R) this;
    }

    public R cacheMode(CacheMode cacheMode) {
        this.mCacheMode = cacheMode;
        return (R) this;
    }

    public R cacheKey(String cacheKey) {
        this.mCacheKey = cacheKey;
        return (R) this;
    }

    public R cacheTime(long cacheTime) {
        if (cacheTime <= -1) {
            cacheTime = CacheMode.DEFAULT_CACHE_NEVER_EXPIRE;
        }
        this.mCacheTime = cacheTime;
        return (R) this;
    }

    public R retryCount(int retryCount) {
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must >= 0");
        }
        this.mRetryCount = retryCount;
        return (R) this;
    }

    public R addInterceptor(Interceptor interceptor) {
        mInterceptors.add(HttpUtil.checkNotNull(interceptor, "interceptor == null"));
        return (R) this;
    }

    public R addNetworkInterceptor(Interceptor interceptor) {
        mNetworkInterceptors.add(HttpUtil.checkNotNull(interceptor, "interceptor == null"));
        return (R) this;
    }

    public R addCookie(Cookie cookie) {
        this.mCookies.add(cookie);
        return (R) this;
    }

    public R addCookies(List<Cookie> cookies) {
        this.mCookies.addAll(cookies);
        return (R) this;
    }

    /**
     * https的访问规则
     */
    public R hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.mHostnameVerifier = hostnameVerifier;
        return (R) this;
    }

    public R lifeCycle(LifecycleTransformer transformer) {
        this.mTransformer = transformer;
        return (R) this;
    }

    public LifecycleTransformer getLifeCycle() {
        return mTransformer;
    }

    /**
     * https的自签名证书
     */
    public R certificates(InputStream... certificates) {
        this.mSSLParams = HttpsCertificateUtil.getSslSocketFactory(null, null, certificates);
        return (R) this;
    }

    /**
     * https双向认证证书
     */
    public R certificates(InputStream bksFile, String password, InputStream... certificates) {
        this.mSSLParams = HttpsCertificateUtil.getSslSocketFactory(bksFile, password, certificates);
        return (R) this;
    }

    /**
     * 添加Header
     */
    public R headers(HttpHeaders headers) {
        this.mHttpHeaders.put(headers);
        return (R) this;
    }

    /**
     * 添加Header
     */
    public R header(String key, String value) {
        mHttpHeaders.put(key, value);
        return (R) this;
    }

    /**
     * 移除全局Header
     */
    public R removeComonHeaders() {
        mHttpHeaders.remove(DolphinHttp.getGlobalConfig().getCommonHeaders());
        return (R) this;
    }

    /**
     * 移除所有的Header
     */
    public R removeAllHeader() {
        mHttpHeaders.clear();
        return (R) this;
    }

    /**
     * 设置参数
     */
    public R params(HttpRequestParams params) {
        this.mHttpRequestParams.put(params);
        return (R) this;
    }

    /**
     * 设置参数
     */
    public R params(Map<String, String> params) {
        this.mHttpRequestParams.put(params);
        return (R) this;
    }

    /**
     * 设置对象参数,先简单使用Gson进行转换
     */
    public R params(Object obj) {
        Map<String, String> map = JsonUtil.fromJsonToMaps(JsonUtil.toJson(obj));
        mHttpRequestParams.put(map);
        return (R) this;
    }

    /**
     * 设置参数
     */
    public R param(String key, String value) {
        mHttpRequestParams.put(key, value);
        return (R) this;
    }

    /**
     * 设置参数
     */
    public R param(String key, int value) {
        mHttpRequestParams.put(key, value);
        return (R) this;
    }

    /**
     * 设置参数
     */
    public R param(String key, long value) {
        mHttpRequestParams.put(key, value);
        return (R) this;
    }

    /**
     * 设置参数
     */
    public R param(String key, float value) {
        mHttpRequestParams.put(key, value);
        return (R) this;
    }

    public R param(String key, boolean value) {
        mHttpRequestParams.put(key, value);
        return (R) this;
    }

    /**
     * 移除参数,里面包含了全局的参数
     */
    public R removeParam(String key) {
        mHttpRequestParams.remove(key);
        return (R) this;
    }

    /**
     * 移除设置的全局Param
     */
    public R removeComonParams() {
        mHttpRequestParams.remove(DolphinHttp.getGlobalConfig().getCommonParams());
        return (R) this;
    }

    /**
     * 移除所有参数(包括全局的参数)
     */
    public R removeAllParam() {
        mHttpRequestParams.clear();
        return (R) this;
    }

    /**
     * 添加Http请求回调
     */
    public <T> R httpCallBack(IHttpCallBack<T> httpCallBack) {
        mHttpCallBack = httpCallBack;
        return (R) this;
    }

    public <T> IHttpCallBack<T> getHttpCallBack() {
        return mHttpCallBack;
    }

    /**
     * 根据当前的请求参数，生成对应的OkHttpClient.Builder
     */
    private OkHttpClient.Builder createOkClientBuilder() {
        //如果进行了设置,修改OkHttpClient.Builder的值
        final OkHttpClient.Builder builder = DolphinHttp.getGlobalConfig()
                .getOkHttpClientBuilder().build().newBuilder();
        if (mTimeOut > 0) {
            builder.readTimeout(mTimeOut, GlobalConfig.TIME_UNIT);
            builder.writeTimeout(mTimeOut, GlobalConfig.TIME_UNIT);
            builder.connectTimeout(mTimeOut, GlobalConfig.TIME_UNIT);
        }
        if (mHostnameVerifier != null) {
            builder.hostnameVerifier(mHostnameVerifier);
        }
        if (mSSLParams != null) {
            builder.sslSocketFactory(mSSLParams.mSSLSocketFactory, mSSLParams.mX509TrustManager);
        }
        if (mCookies.size() > 0) {
            CookieManger cookieManger = DolphinHttp.getGlobalConfig().getCookieManger();
            if (cookieManger != null) {
                cookieManger.addCookies(mCookies);
            } else {
                cookieManger = new CookieManger(mContext);
                cookieManger.addCookies(mCookies);
                builder.cookieJar(cookieManger);
            }
        }
        if (!mInterceptors.isEmpty()) {
            for (Interceptor interceptor : mInterceptors) {
                builder.addInterceptor(interceptor);
            }
        }
        if (!mNetworkInterceptors.isEmpty()) {
            for (Interceptor interceptor : mNetworkInterceptors) {
                builder.addNetworkInterceptor(interceptor);
            }
        }
        if (mCacheMode == CacheMode.DEFAULT) {
            builder.cache(mCache);
        }
        return builder;
    }

    /**
     * 根据配置的BaseUrl，生成对应的Retrofit.Builder
     */
    private Retrofit.Builder createRetrofitBuilder() {
        Retrofit.Builder retrofitBuilder = DolphinHttp.getGlobalConfig().getRetrofitBuilder();
        retrofitBuilder.baseUrl(DolphinHttp.getGlobalConfig().getBaseUrl());
        return retrofitBuilder;
    }

    /**
     * 用来判断是否需要重新构建Retrofit对象
     * 只要是默认的配置没有进行修改,就使用默认的请求对象
     */
    private boolean isPrimalOkHttpClientBuild() {
        return mTimeOut <= 0 && mSSLParams == null && mCookies.isEmpty() && mHostnameVerifier == null
                && mNetworkInterceptors.isEmpty() && mInterceptors.isEmpty() && mCacheMode == CacheMode.NO_CACHE;
    }

    /**
     * 构建Rx缓存
     */
    private RxCache.Builder createRxCacheBuilder() {
        RxCache.Builder rxCacheBuilder = DolphinHttp.getGlobalConfig().getRxCacheBuilder();
        switch (mCacheMode) {
            case NO_CACHE:
                //默认模式,已经处理了,不用处理
                break;
            case DEFAULT:
                //使用Okhttp的缓存
                if (this.mCache == null) {
                    File cacheDirectory = DolphinHttp.getGlobalConfig().getCacheDirectory();
                    if (cacheDirectory == null && mContext != null) {
                        cacheDirectory = new File(mContext.getCacheDir(), "okhttp-cache");
                    } else {
                        if (cacheDirectory.isDirectory() && !cacheDirectory.exists()) {
                            cacheDirectory.mkdirs();
                        }
                    }
                    this.mCache = new Cache(cacheDirectory,
                            Math.max(CACHE_MAX_SIZE, DolphinHttp.getGlobalConfig().getCacheMaxSize()));
                }
                String cacheControlValue = String.format("max-age=%d", Math.max(-1, mCacheTime));
                final CacheInterceptor rewriteCacheControlInterceptor =
                        new CacheInterceptor(mContext, cacheControlValue);
                final CacheInterceptorOffline rewriteCacheControlInterceptorOffline =
                        new CacheInterceptorOffline(mContext, cacheControlValue);
                mNetworkInterceptors.add(rewriteCacheControlInterceptor);
                mNetworkInterceptors.add(rewriteCacheControlInterceptorOffline);
                mInterceptors.add(rewriteCacheControlInterceptorOffline);
                break;
            case FIRST_REMOTE:
                rxCacheBuilder = rxCacheBuilder(rxCacheBuilder);
                break;
            case FIRST_CACHE:
                rxCacheBuilder = rxCacheBuilder(rxCacheBuilder);
                break;
            case ONLY_REMOTE:
                rxCacheBuilder = rxCacheBuilder(rxCacheBuilder);
                break;
            case ONLY_CACHE:
                rxCacheBuilder = rxCacheBuilder(rxCacheBuilder);
                break;
            case CACHE_AND_REMOTE:
                rxCacheBuilder = rxCacheBuilder(rxCacheBuilder);
                break;
            case CACHE_AND_REMOTE_DISTINCT:
                rxCacheBuilder = rxCacheBuilder(rxCacheBuilder);
                break;
            default:
                break;
        }
        return rxCacheBuilder;
    }

    private RxCache.Builder rxCacheBuilder(RxCache.Builder rxCacheBuilder) {
        rxCacheBuilder.cachekey(HttpUtil.checkNotNull(mCacheKey, "cacheKey == null"))
                .cacheTime(mCacheTime);
        return rxCacheBuilder;
    }

    /**
     * 构建Retrofit请求对象
     */
    public R build() {
        //先使用全局配置默认创建的ApiService
        mApiService = DolphinHttp.getGlobalConfig().getGlobalApiservice();
        //判断ApiService是否需要重新进行创建
        if (!isPrimalOkHttpClientBuild() || mApiService == null) {
            final OkHttpClient.Builder okHttpClientBuilder = createOkClientBuilder();
            final Retrofit.Builder retrofitBuilder = createRetrofitBuilder();
            OkHttpClient okHttpClient = okHttpClientBuilder.build();
            retrofitBuilder.client(okHttpClient);
            Retrofit retrofit = retrofitBuilder.build();
            mApiService = retrofit.create(ApiService.class);
        }
        HttpLogUtil.d("mApiService: " + mApiService);
        final RxCache.Builder rxCacheBuilder = createRxCacheBuilder();
        mRxCache = rxCacheBuilder.build();
        return (R) this;
    }

    /**
     * 添加原始数据回调,本质就是添加一个拦截器
     */
    public R httpResponseCallBack(IHttpResponseCallBack httpResponseCallBack) {
        //用来回调原始Response
        IHttpResponseCallBack httpResponseCallBack1 = httpResponseCallBack;
        if (httpResponseCallBack1 != null) {
            mInterceptors.add(new ResponseInterceptor(httpResponseCallBack1));
        }
        return (R) this;
    }

    public CacheMode getCacheMode() {
        return mCacheMode;
    }


    public int getTimeOut() {
        return mTimeOut;
    }

    public String getJson() {
        return mJson;
    }

    public RxCache getRxCache() {
        return mRxCache;
    }

    public int getRetryCount() {
        return mRetryCount;
    }

    public Cache getCache() {
        return mCache;
    }

    public Context getContext() {
        return HttpUtil.checkNotNull(mContext, "Context == null");
    }

    public String getRequestUrl() {
        return HttpUtil.checkNotNull(mRequestUrl, "RequestUrl == null");
    }

    public ApiService getApiService() {
        return HttpUtil.checkNotNull(mApiService, "ApiService == null");
    }

    public HttpHeaders getHttpHeaders() {
        return mHttpHeaders;
    }

}
