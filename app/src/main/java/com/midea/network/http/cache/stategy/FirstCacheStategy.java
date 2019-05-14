
package com.midea.network.http.cache.stategy;





import com.midea.network.http.cache.RxCache;
import com.midea.network.http.cache.model.CacheResult;

import java.lang.reflect.Type;

import io.reactivex.Observable;


/**
 * <p>描述：先显示缓存，缓存不存在，再请求网络</p>
 */
final public class FirstCacheStategy extends BaseStrategy {
    @Override
    public <T> Observable<CacheResult<T>> execute(RxCache rxCache, String key, long time, Observable<T> source, Type type) {
        Observable<CacheResult<T>> cache = loadCache(rxCache, type, key, time, true);
        Observable<CacheResult<T>> remote = loadRemoteAndSaveSync(rxCache, key, source, false);
        return cache.switchIfEmpty(remote);
    }
}
