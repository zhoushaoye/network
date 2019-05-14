package com.midea.network.http.config;

import android.content.Context;
import android.support.annotation.NonNull;

import com.midea.network.http.DolphinHttp;
import com.midea.network.http.apiservice.ApiService;
import com.midea.network.http.cache.RxCache;
import com.midea.network.http.cache.converter.IDiskConverter;
import com.midea.network.http.cache.converter.SerializableDiskConverter;
import com.midea.network.http.cache.model.CacheMode;
import com.midea.network.http.cookie.CookieManger;
import com.midea.network.http.interceptor.NoCacheInterceptor;
import com.midea.network.http.model.HttpHeaders;
import com.midea.network.http.model.HttpRequestParams;
import com.midea.network.http.request.IRequestMethod;
import com.midea.network.http.request.RequestMethod;
import com.midea.network.http.utils.HttpLogUtil;
import com.midea.network.http.utils.HttpUtil;
import com.midea.network.http.utils.HttpsCertificateUtil;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 全局参数默认配置,配置的参数全局生效,会作用于所有的接口请求
 * 如果{@link RequestConfig}中有配置新的值,则当次请求会被覆盖(不会影响全局参数)
 *
 * @author zhoudingjun
 * @version 1.0
 * @since 2019/5/13
 */
public class GlobalConfig {

    private Context mContext;

    //默认的超时时间
    public static final  int DEFAULT_SECONDS = 30;

    //默认重试次数
    public static final int DEFAULT_RETRY_COUNT = 0;

    //默认各种超时时间单位(秒)
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    //通过Okhttp的缓存
    private Cache mCache = null;

    //默认缓存类型
    private CacheMode mCacheMode = CacheMode.NO_CACHE;

    //默认缓存时间
    private long mCacheTime = CacheMode.DEFAULT_CACHE_NEVER_EXPIRE;

    //默认缓存目录
    private File mCacheDirectory;

    //默认缓存大小
    private long mCacheMaxSize;

    //默认全局BaseUrl
    private String mRetrofitBaseUrl;

    private int mRetryCount = DEFAULT_RETRY_COUNT;

    //默认全局公共请求头
    private HttpHeaders mCommonHeaders;

    //默认全局公共请求参数
    private HttpRequestParams mCommonParams;

    private OkHttpClient.Builder mOkHttpClientBuilder;

    private Retrofit.Builder mRetrofitBuilder;

    private RxCache.Builder mRxCacheBuilder;

    //默认Cookie管理器
    private CookieManger mCookieManger;

    //默认使用RequestMethod实现请求
    private IRequestMethod mRequestMethod;

    private Authenticator mAuthenticator;

    private static ApiService apiservice = null;

    public GlobalConfig(Context context) {
        this.mContext = context;
        mRequestMethod = new RequestMethod();
        createOkHttpClientBuilder();
        createRetrofitBuilder();
        createRxCacheBuilder();
    }

    public Context getContext() {
        if (mContext == null) {
            HttpLogUtil.e("you need call init() at first ! ...");
            return null;
        }
        return mContext;
    }

    /**
     * 根据默认配置构建RxCacheBuilder
     */
    private void createRxCacheBuilder() {
        if (mRxCacheBuilder == null) {
            mRxCacheBuilder = new RxCache.Builder().init(getContext());
        }
        mRxCacheBuilder.diskConverter(new SerializableDiskConverter());
    }

    /**
     * 根据默认配置构建RetrofitBuilder
     */
    private void createRetrofitBuilder() {
        if (mRetrofitBuilder == null) {
            mRetrofitBuilder = new Retrofit.Builder();
        }
        mRetrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

    /**
     * 根据默认配置构建OkHttpClientBuilder
     */
    private void createOkHttpClientBuilder() {
        if (mOkHttpClientBuilder == null) {
            mOkHttpClientBuilder = new OkHttpClient.Builder();
        }
        mOkHttpClientBuilder.connectTimeout(GlobalConfig.DEFAULT_SECONDS, GlobalConfig.TIME_UNIT);
        mOkHttpClientBuilder.readTimeout(GlobalConfig.DEFAULT_SECONDS, GlobalConfig.TIME_UNIT);
        mOkHttpClientBuilder.writeTimeout(GlobalConfig.DEFAULT_SECONDS, GlobalConfig.TIME_UNIT);
        NoCacheInterceptor noCacheInterceptor = new NoCacheInterceptor();
        mOkHttpClientBuilder.addInterceptor(noCacheInterceptor);
        mOkHttpClientBuilder.addNetworkInterceptor(noCacheInterceptor);
    }

    /**
     * 如果需要自定义ApiResult,则实现IRequestMethod,初始化调用此方法
     */
    public GlobalConfig setRequestMethod(IRequestMethod requestMethod) {
        mRequestMethod = requestMethod;
        return this;
    }

    public IRequestMethod getRequestMethod() {
        return mRequestMethod;
    }

    public static class DefaultHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


    /**
     * https的访问规则
     */
    public GlobalConfig setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        mOkHttpClientBuilder.hostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * 设置https的全局自签名证书
     */
    public GlobalConfig setCertificates(InputStream... certificates) {
        HttpsCertificateUtil.SSLParams sslParams = HttpsCertificateUtil.getSslSocketFactory(null, null, certificates);
        mOkHttpClientBuilder.sslSocketFactory(sslParams.mSSLSocketFactory, sslParams.mX509TrustManager);
        return this;
    }

    /**
     * 设置https双向认证证书
     */
    public GlobalConfig setCertificates(InputStream bksFile, String password, InputStream... certificates) {
        HttpsCertificateUtil.SSLParams sslParams = HttpsCertificateUtil
                .getSslSocketFactory(bksFile, password, certificates);
        mOkHttpClientBuilder.sslSocketFactory(sslParams.mSSLSocketFactory, sslParams.mX509TrustManager);
        return this;
    }

    /**
     * 设置全局cookie存取规则
     */
    public GlobalConfig setCookieManger(CookieManger cookieManager) {
        mCookieManger = HttpUtil.checkNotNull(cookieManager, "cookieManager == null");
        mOkHttpClientBuilder.cookieJar(mCookieManger);
        return this;
    }

    /**
     * 设置全局的cookie实例
     */
    public CookieManger getCookieManger() {
        return mCookieManger;
    }

    /**
     * 设置全局读取超时时间
     *  超时时间,单位(秒)
     */
    public GlobalConfig setReadTimeOut(int readTimeOut) {
        mOkHttpClientBuilder.readTimeout(readTimeOut, TIME_UNIT);
        return this;
    }

    /**
     * 设置全局写入超时时间
     *  超时时间,单位(秒)
     */
    public GlobalConfig setWriteTimeOut(int writeTimeout) {
        mOkHttpClientBuilder.writeTimeout(writeTimeout, TIME_UNIT);
        return this;
    }

    /**
     * 设置全局连接超时时间
     *  超时时间,单位(秒)
     */
    public GlobalConfig setConnectTimeout(int connectTimeout) {
        mOkHttpClientBuilder.connectTimeout(connectTimeout, TIME_UNIT);
        return this;
    }

    /**
     * 设置全局超时重试次数
     */
    public GlobalConfig setRetryCount(int retryCount) {
        mRetryCount = retryCount;
        return this;
    }

    /**
     * 超时重试次数
     */
    public int getRetryCount() {
        return mRetryCount;
    }

    /**
     * 全局的缓存模式
     */
    public GlobalConfig setCacheMode(CacheMode cacheMode) {
        mCacheMode = cacheMode;
        return this;
    }

    /**
     * 获取全局的缓存模式
     */
    public CacheMode getCacheMode() {
        return mCacheMode;
    }

    /**
     * 全局的缓存过期时间
     */
    public GlobalConfig setCacheTime(long cacheTime) {
        if (cacheTime <= -1) {
            cacheTime = CacheMode.DEFAULT_CACHE_NEVER_EXPIRE;
        }
        mCacheTime = cacheTime;
        return this;
    }

    /**
     *  根据全局配置创建一个默认的ApiService对象,如果请求没有做任何设置更改就使用该对象,否则重新创建.</br>
     *  ApiService对象关联了Retrofit,OkHttpClient对象,这么处理是为了尽量减少对象创建.</br>
     *  当请求的配置有修改时,需要重新创建相关对象,否则配置会失效.
     */
    protected ApiService getGlobalApiservice() {
        if(apiservice == null){
            apiservice = mRetrofitBuilder
                    .baseUrl(getBaseUrl())
                    .client(DolphinHttp.getGlobalConfig().getOkHttpClientBuilder().build())
                    .build()
                    .create(ApiService.class);
        }
        return apiservice;
    }

    /**
     * 获取全局缓存过期时间
     */
    public long getCacheTime() {
        return mCacheTime;
    }

    /**
     * 全局的缓存大小,默认50M
     */
    public GlobalConfig setCacheMaxSize(long maxSize) {
        mCacheMaxSize = maxSize;
        return this;
    }

    /**
     * 获取全局的缓存大小
     */
    public long getCacheMaxSize() {
        return mCacheMaxSize;
    }

    /**
     * 全局设置缓存的版本，默认为1，缓存的版本号
     */
    public GlobalConfig setCacheVersion(int cacheersion) {
        if (cacheersion < 0) {
            throw new IllegalArgumentException("cacheersion must > 0");
        }
        mRxCacheBuilder.appVersion(cacheersion);
        return this;
    }

    /**
     * 设置全局缓存路径，默认是应用包下面的缓存
     */
    public GlobalConfig setCacheDirectory(@NonNull File directory) {
        mCacheDirectory = HttpUtil.checkNotNull(directory, "directory == null");
        mRxCacheBuilder.diskDir(directory);
        return this;
    }

    /**
     * 获取缓存的路径
     */
    public File getCacheDirectory() {
        return mCacheDirectory;
    }

    /**
     * 设置全局缓存的转换器
     */
    public GlobalConfig setCacheDiskConverter(@NonNull IDiskConverter converter) {
        mRxCacheBuilder.diskConverter(HttpUtil.checkNotNull(converter, "converter == null"));
        return this;
    }

    /**
     * 设置全局OkHttp的缓存,默认是3天
     */
    public GlobalConfig setOkHttpCache(Cache cache) {
        this.mCache = cache;
        return this;
    }

    /**
     * 获取OkHttp的缓存<br>
     */
    public Cache getHttpCache() {
        return mCache;
    }

    /**
     * 添加全局公共请求参数
     */
    public GlobalConfig addCommonParams(HttpRequestParams commonParams) {
        if (mCommonParams == null) {
            mCommonParams = new HttpRequestParams();
        }
        mCommonParams.put(commonParams);
        return this;
    }

    /**
     * 获取全局公共请求参数
     */
    public HttpRequestParams getCommonParams() {
        return mCommonParams;
    }


    /**
     * 添加全局公共Header
     */
    public GlobalConfig addCommonHeaders(HttpHeaders commonHeaders) {
        if (mCommonHeaders == null) {
            mCommonHeaders = new HttpHeaders();
        }
        mCommonHeaders.put(commonHeaders);
        return this;
    }

    /**
     * 获取全局公共请求头
     */
    public HttpHeaders getCommonHeaders() {
        return mCommonHeaders;
    }

    /**
     * 添加全局拦截器
     */
    public GlobalConfig addInterceptor(@NonNull Interceptor interceptor) {
        mOkHttpClientBuilder.addInterceptor(HttpUtil.checkNotNull(interceptor, "interceptor == null"));
        return this;
    }

    /**
     * 添加全局网络拦截器
     */
    public GlobalConfig addNetworkInterceptor(@NonNull Interceptor interceptor) {
        mOkHttpClientBuilder.addNetworkInterceptor(HttpUtil.checkNotNull(interceptor, "interceptor == null"));
        return this;
    }

    /**
     * 设置全局请求的连接池
     */
    public GlobalConfig setOkHttpConnectionPool(@NonNull ConnectionPool connectionPool) {
        mOkHttpClientBuilder.connectionPool(HttpUtil.checkNotNull(connectionPool, "connectionPool == null"));
        return this;
    }

    public Authenticator getAuthenticator() {
        return mAuthenticator;
    }

    public GlobalConfig setAuthenticator(@NonNull Authenticator authenticator) {
        mAuthenticator = authenticator;
        mOkHttpClientBuilder.authenticator(HttpUtil.checkNotNull(mAuthenticator,"authenticator == null"));
        return this;
    }

    /**
     * 设置全局baseurl
     */
    public GlobalConfig setBaseUrl(@NonNull String retrofitBaseUrl) {
        mRetrofitBaseUrl = HttpUtil.checkNotNull(retrofitBaseUrl, "baseUrl == null");
        return this;
    }

    public String getBaseUrl() {
        return HttpUtil.checkNotNull(mRetrofitBaseUrl, "baseUrl == null");
    }

    public Cache getCache() {
        return mCache;
    }

    public OkHttpClient.Builder getOkHttpClientBuilder() {
        return mOkHttpClientBuilder;
    }

    public Retrofit.Builder getRetrofitBuilder() {
        return mRetrofitBuilder;
    }

    public RxCache.Builder getRxCacheBuilder() {
        return mRxCacheBuilder;
    }

}
