
package com.midea.dolphin.http.cache.stategy;

import com.midea.dolphin.http.cache.RxCache;
import com.midea.dolphin.http.cache.model.CacheResult;

import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okio.ByteString;

/**
 * <p>描述：先显示缓存，再请求网络</p>
 */
public final class CacheAndRemoteDistinctStrategy extends BaseStrategy {
    @Override
    public <T> Observable<CacheResult<T>> execute(RxCache rxCache, String key, long time, Observable<T> source, Type type) {
        Observable<CacheResult<T>> cache = loadCache(rxCache, type, key, time,true);
        Observable<CacheResult<T>> remote = loadRemoteAndSaveSync(rxCache, key, source,false);
        return Observable.concat(cache, remote)
                .filter(new Predicate<CacheResult<T>>() {
                    @Override
                    public boolean test(@NonNull CacheResult<T> tCacheResult) throws Exception {
                        return tCacheResult != null && tCacheResult.data != null;
                    }
                }).distinctUntilChanged(new Function<CacheResult<T>, String>() {
                    @Override
                    public String apply(@NonNull CacheResult<T> tCacheResult) throws Exception {
                        return  ByteString.of(tCacheResult.data.toString().getBytes()).md5().hex();
                    }
                });
    }

}
