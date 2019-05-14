package com.midea.dolphin.http.cache.model;

/**
 * <p>描述：网络请求策略</p>
 */
public enum CacheMode {
    /**
     * 不使用缓存,该模式下,cacheKey,cacheMaxAge 参数均无效
     **/
    NO_CACHE,
    /**
     * 完全按照HTTP协议的默认缓存规则，走OKhttp的Cache缓存
     **/
    DEFAULT,
    /**
     * 先请求网络，请求网络失败后再加载缓存
     */
    FIRST_REMOTE,

    /**
     * 先加载缓存，缓存没有再去请求网络
     */
    FIRST_CACHE,

    /**
     * 仅加载网络，但数据依然会被缓存
     */
    ONLY_REMOTE,

    /**
     * 只读取缓存
     */
    ONLY_CACHE,

    /**
     * 先使用缓存，不管是否存在，仍然请求网络，会回调两次
     */
    CACHE_AND_REMOTE,
    /**
     * 先使用缓存，不管是否存在，仍然请求网络，会先把缓存回调给你<br>
     * 等网络请求回来发现数据是一样的就不会再返回，否则再返回<br>
     * （这样做的目的是防止数据是一样的你也需要刷新界面）<br>
     * 该模式生效的先决条件是对象一样,需要重写toString()方法<br>
     * 不一致是通过distinctUntilChanged操作符实现的.
     */
    CACHE_AND_REMOTE_DISTINCT;

    //默认永久缓存
    public static final int DEFAULT_CACHE_NEVER_EXPIRE = -1;
}
